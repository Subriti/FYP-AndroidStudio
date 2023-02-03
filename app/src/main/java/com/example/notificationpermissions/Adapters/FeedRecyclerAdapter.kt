package com.example.notificationpermissions.Adapters

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.notificationpermissions.Models.Post
import com.example.notificationpermissions.Notifications.NotificationData
import com.example.notificationpermissions.Notifications.PushNotification
import com.example.notificationpermissions.Notifications.RetrofitInstance
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.AuthService
import com.example.notificationpermissions.Services.PostService
import com.example.notificationpermissions.Utilities.App
import com.example.notificationpermissions.Utilities.EXTRA_POST
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*


//for sending notification
const val TOPIC = "/topics/interestedUser"

class FeedRecyclerAdapter(
    private val context: Context,
    private val imageUrls: List<String>,
    val itemClick: (Post) -> Unit
) :
    RecyclerView.Adapter<FeedRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.post_feed_item, parent, false)
        return ViewHolder(view, itemClick)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .into(holder.postImage)

        holder.bindPost(PostService.AllPosts[position], context)
    }

    override fun getItemCount(): Int {
        return imageUrls.size
    }

    inner class ViewHolder(itemView: View, val itemClick: (Post) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        val postImage = itemView.findViewById<ImageView>(R.id.postImage)
        private val username = itemView.findViewById<TextView>(R.id.username)
        private val userProfile = itemView.findViewById<ImageView>(R.id.user_profile)
        private val description = itemView.findViewById<TextView>(R.id.feed_description)
        private val markInterested = itemView.findViewById<ImageView>(R.id.markInterested)
        private val interestedUsers = itemView.findViewById<TextView>(R.id.countInterested)
        private val createdDatetime = itemView.findViewById<TextView>(R.id.createdDatetime)


        var alreadyLiked = false
        private fun getUsers(post: Post): Boolean {
            PostService.getInterestedUserByPosts(post.post_id) { getInterestedUsers ->
                println("Get Interested User success: $getInterestedUsers")
                if (getInterestedUsers) {
                    //on success display users ig
                    interestedUsers.text = "Interested Users: ${PostService.InterestedUsers.size}"
                    for (i in PostService.InterestedUsers) {
                        if (i.user_id == App.sharedPrefs.userID) {
                            alreadyLiked = true
                            markInterested.setImageResource(R.drawable.liked)
                        }
                    }
                }
            }
            return alreadyLiked
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun bindPost(post: Post, context: Context) {
            username?.text = post.post_by
            description?.text = post.description
            Glide.with(context).load(post.cloth_id).into(userProfile)

            println(post.created_datetime)
            val dateString = post.created_datetime
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
            val date = format.parse(dateString)
            val currentDate = Date() // current date and time

            val diffInMillis: Long = currentDate.time - date.time
            val duration = Duration.ofMillis(diffInMillis)

            val seconds = duration.seconds
            val minutes = duration.toMinutes()
            val hours = duration.toHours()
            val days = duration.toDays()

            if (days > 0) {
                createdDatetime?.text = "$days days ago"
            } else if (hours > 0) {
                createdDatetime?.text = "$hours hours ago"
            } else if (minutes > 0) {
                createdDatetime?.text = "$minutes minutes ago"
            } else {
                createdDatetime?.text = "$seconds seconds ago"
            }

            userProfile.setOnClickListener {
                //open user profile with posts
                itemView.findNavController().navigate(
                    R.id.action_homeFragment_to_userViewProfileFragment2,
                    Bundle().apply { putSerializable(EXTRA_POST, post) })
            }

            interestedUsers.setOnClickListener {
                val items = arrayListOf<String>()
                if (PostService.InterestedUsersMapList.containsKey(post.post_id)) {

                    for (i in PostService.InterestedUsersMapList[post.post_id]!!) {
                        items.add(i.user_name)
                    }
                    if (items.size > 0) {
                        val adapter =
                            ArrayAdapter(context, android.R.layout.simple_list_item_1, items)
                        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                        val inflater = LayoutInflater.from(context)
                        val dialogView: View =
                            inflater.inflate(R.layout.dialog_interested_users, null)
                        builder.setView(dialogView)

                        val listView: ListView = dialogView.findViewById(R.id.list_view)
                        listView.adapter = adapter

                        val dialog: AlertDialog = builder.create()
                        dialog.show()
                    }
                }
            }


            var isLiked = false
            alreadyLiked = getUsers(post)
            if (alreadyLiked) {
                isLiked = true
            }

            markInterested.setOnClickListener {
                if (!isLiked) {
                    markInterested.setImageResource(R.drawable.liked)
                    isLiked = true

                    //check if the user already liked, or is newly liked
                    if (!alreadyLiked) {
                        PostService.addInterestedUser(
                            App.sharedPrefs.userID,
                            post.post_id
                        ) { addUserSuccess ->
                            println("Add Interested User success: $addUserSuccess")
                            if (addUserSuccess) {
                                //on success display users ig
                                getUsers(post)

                                //send Notification to the owner when liked by someone
                                val title = "Your post was liked by someone"
                                val message =
                                    "${App.sharedPrefs.userName} was interested on your post"

                                AuthService.getFCMToken(post.post_by) { response ->
                                    println("Get FCM Token success: $response")

                                    //to specific post owners; TOPIC ko satta post.postowner ko token: get from database
                                    println("Recipient Token during notification sending is:${AuthService.recipientToken}")
                                    PushNotification(
                                        NotificationData(title, message),
                                        AuthService.recipientToken
                                    )
                                        .also { sendNotification(it) }

                                    //for sending to multiple recipients
                                    /*val recipientTokens = listOf("token1", "token2", "token3")
                                    for (token in recipientTokens) {
                                        PushNotification(NotificationData(title, message), token)
                                            .also { sendNotification(it) }
                                    }*/
                                }
                                //to all people subscribed to the topic; like general announcements
                                /* PushNotification(NotificationData(title,message), TOPIC)
                                     .also { sendNotification(it) }*/
                            }
                        }
                    }
                } else {
                    markInterested.setImageResource(R.drawable.unliked)
                    isLiked = false
                    alreadyLiked = false
                    //else check if the photo is liked, if yes dislike it
                    PostService.deleteInterestedUserByPosts(
                        post.post_id,
                        App.sharedPrefs.userID
                    ) { deleteUserSuccess ->
                        println("Delete Interested User success: $deleteUserSuccess")
                        if (deleteUserSuccess) {
                            //on success refresh interested users
                            getUsers(post)
                        }
                    }
                }
            }

            itemView.setOnClickListener { itemClick(post) }
        }
    }

    val TAG = "InterestedUser"
    private fun sendNotification(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.postNotification(notification)
                if (response.isSuccessful) {
                    println("Notification successfully sent")
                    println(response.message().toString())
                } else {
                    println("Notification could not be sent")
                    Log.e(TAG, response.errorBody().toString())
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
}
