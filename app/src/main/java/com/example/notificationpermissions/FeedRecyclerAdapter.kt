package com.example.notificationpermissions

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.notificationpermissions.Utilities.EXTRA_POST
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*


class FeedRecyclerAdapter(
    private val context: Context,
    private val imageUrls: List<String>,
    private val fragmentManager: FragmentManager,
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
        private val interestedUsers= itemView.findViewById<TextView>(R.id.countInterested)
        private val createdDatetime= itemView.findViewById<TextView>(R.id.createdDatetime)


        var alreadyLiked= false
        private fun getUsers(post:Post): Boolean{
            PostService.getInterestedUserByPosts(post.post_id){
                    getInterestedUsers -> println("Get Interested User success: $getInterestedUsers")
                if (getInterestedUsers) {
                    //on success display users ig
                    interestedUsers.text= "Interested Users: ${PostService.InterestedUsers.size}"
                    for(i in PostService.InterestedUsers){
                        if (i.user_id==App.sharedPrefs.userID){
                            alreadyLiked= true
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
           // val millis = date.time
            val currentDate = Date() // current date and time

            //val date2= OffsetDateTime.now()
            //val durations= Duration.between(date,date2)

            //val now = Instant.now()
            //val pastInstant = now.minus(millis, ChronoUnit.DAYS)
            //val duration = Duration.between(pastInstant, now)

            val diffInMillis: Long = currentDate.time - date.time
            val duration = Duration.ofMillis(diffInMillis)

            val seconds = duration.seconds
            val minutes = duration.toMinutes()
            val hours = duration.toHours()
            val days = duration.toDays()

            if (days > 0) {
                createdDatetime?.text="$days days ago"
            } else if (hours > 0) {
                createdDatetime?.text="$hours hours ago"
            } else if (minutes > 0) {
                createdDatetime?.text="$minutes minutes ago"
            } else {
                createdDatetime?.text="$seconds seconds ago"
            }

            userProfile.setOnClickListener {
                    //open user profile with posts
                val profileFragment = UserViewProfileFragment().apply {
                    arguments= Bundle().apply { putSerializable(EXTRA_POST,post) }
                }
                val transaction: FragmentTransaction = fragmentManager.beginTransaction()
                transaction.replace(R.id.home_fragment, profileFragment)
                transaction.addToBackStack("profileFragment")
                //transaction.addToBackStack(null)
                transaction.setReorderingAllowed(true)
                transaction.commit()
            }

            interestedUsers.setOnClickListener {
                val items = arrayListOf<String>()
                if (PostService.InterestedUsersMapList.containsKey(post.post_id)) {
                    /*println(PostService.InterestedUsersMapList)
                    println(PostService.InterestedUsersMapList[post.post_id])*/

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

            //getUsers(post)

            var isLiked = false;

            //var isLiked = false;
            alreadyLiked = getUsers(post);
            if (alreadyLiked){
                isLiked=true
            }

            markInterested.setOnClickListener {
                if (!isLiked) {
                    //markInterested.setImageResource(R.drawable.ic_baseline_star_24)
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
                            }
                        }
                    }
                } else {
                    //markInterested.setImageResource(R.drawable.ic_baseline_star_border_24)
                    markInterested.setImageResource(R.drawable.unliked)
                    isLiked = false
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
}

