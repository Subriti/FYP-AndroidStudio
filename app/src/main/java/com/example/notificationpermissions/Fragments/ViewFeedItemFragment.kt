package com.example.notificationpermissions.Fragments

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.notificationpermissions.Activities.DashboardActivity
import com.example.notificationpermissions.Models.Post
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.PostService
import com.example.notificationpermissions.Utilities.App
import com.example.notificationpermissions.Utilities.EXTRA_POST
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*

class ViewFeedItemFragment : Fragment() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.view_user_post_item, container, false)
        (activity as DashboardActivity?)!!.currentFragment = this

        val postImage = view.findViewById<ImageView>(R.id.postImage)
        val username = view.findViewById<TextView>(R.id.username)
        val userProfile = view.findViewById<ImageView>(R.id.user_profile)
        val description = view.findViewById<TextView>(R.id.feed_description)
        val markInterested = view.findViewById<ImageView>(R.id.markInterested)
        val interestedUsers = view.findViewById<TextView>(R.id.countInterested)
        val createdDatetime = view.findViewById<TextView>(R.id.createdDatetime)

        val postDetails = arguments?.getSerializable(EXTRA_POST) as Post

        context?.let {
            Glide.with(it).load(postDetails.media_file).into(postImage)
        }

        username?.text = postDetails.post_by

        val profilePicture = postDetails.cloth_id
        context?.let {
            Glide.with(it).load(profilePicture).into(userProfile)
        }

        description.text = postDetails.description

        val dateString = postDetails.created_datetime
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

        var alreadyLiked = false
        fun getUsers(post: Post): Boolean {
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
        getUsers(postDetails)

        interestedUsers.setOnClickListener {
            val items = arrayListOf<String>()
            if (PostService.InterestedUsersMapList.containsKey(postDetails.post_id)) {
                for (i in PostService.InterestedUsersMapList[postDetails.post_id]!!) {
                    items.add(i.user_name)
                }
                if (items.size > 0) {
                    val adapter =
                        ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
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

        markInterested.setOnClickListener {
            if (!isLiked) {
                //markInterested.setImageResource(R.drawable.ic_baseline_star_24)
                markInterested.setImageResource(R.drawable.liked)
                isLiked = true

                //check if the user already liked, or is newly liked
                if (!alreadyLiked) {
                    PostService.addInterestedUser(
                        App.sharedPrefs.userID,
                        postDetails.post_id
                    ) { addUserSuccess ->
                        println("Add Interested User success: $addUserSuccess")
                        if (addUserSuccess) {
                            //on success display users ig
                            getUsers(postDetails)
                        }
                    }
                }
            } else {
                markInterested.setImageResource(R.drawable.unliked)
                isLiked = false
                //else check if the photo is liked, if yes dislike it
                PostService.deleteInterestedUserByPosts(
                    postDetails.post_id,
                    App.sharedPrefs.userID
                ) { deleteUserSuccess ->
                    println("Delete Interested User success: $deleteUserSuccess")
                    if (deleteUserSuccess) {
                        //on success refresh interested users
                        getUsers(postDetails)
                    }
                }
            }
        }

        //hiding the edit menu when viewing from the grid feed
        val postOptions = view.findViewById<ImageView>(R.id.postOptions2)
        postOptions.isVisible = false
        return view
    }
}