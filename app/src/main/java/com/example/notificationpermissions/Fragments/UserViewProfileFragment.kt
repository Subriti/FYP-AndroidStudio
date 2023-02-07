package com.example.notificationpermissions.Fragments

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.notificationpermissions.Activities.DashboardActivity
import com.example.notificationpermissions.Adapters.PostRecycleAdapter
import com.example.notificationpermissions.Models.ChatRoom
import com.example.notificationpermissions.Models.Post
import com.example.notificationpermissions.Models.PostDetails
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.PostService
import com.example.notificationpermissions.Utilities.App
import com.example.notificationpermissions.Utilities.EXTRA_CHAT_ROOM
import com.example.notificationpermissions.Utilities.EXTRA_POST


class UserViewProfileFragment : Fragment() {
    private lateinit var imgButton: Button
    private lateinit var imgGallery: ImageView

    lateinit var adapter: PostRecycleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        (activity as DashboardActivity?)!!.currentFragment = this

        var postDetails = arguments?.getSerializable(EXTRA_POST) as Post
        var newPostDetails: PostDetails? = null
        for (details in PostService.DetailedPosts) {
            if (details.post_by == postDetails.post_by) {
                //using the full post details to build the user profile
                newPostDetails = details
            }
        }

        //user display picture
        imgGallery = view.findViewById(R.id.profile_image)
        context?.let {
            Glide.with(it).load(newPostDetails?.user_profile).into(imgGallery)
        }

        val location = view.findViewById<TextView>(R.id.UserLocation)
        val phoneNumber = view.findViewById<TextView>(R.id.UserPhone)
        val email = view.findViewById<TextView>(R.id.UserEmail)

        email.text = "  ${newPostDetails?.user_email}"
        phoneNumber.text = "  ${newPostDetails?.user_phone}"
        location.text = "  ${newPostDetails?.location}"

        imgButton = view.findViewById(R.id.editProfile)

        println(newPostDetails?.user_id)
        println(App.sharedPrefs.userID)

        //if opened own's profile, open profile fragment
        if (newPostDetails?.user_id== App.sharedPrefs.userID){
            imgButton.text = "Edit Profile"
            imgButton.setOnClickListener {
                view.findNavController().navigate(R.id.action_userViewProfileFragment2_to_profileFragment)
            }
        }else {
            imgButton.text = "Message"
            imgButton.setOnClickListener {
                val chatRoomId = "${App.sharedPrefs.userName}+ ${newPostDetails?.post_by}"
                val recieverUserId = newPostDetails?.user_id
                val recieverFCMtoken = newPostDetails?.fcm_token
                val recieverProfilePicture = newPostDetails?.user_profile
                val recieverUserName = newPostDetails?.post_by

                val newChatRoom = ChatRoom(
                    chatRoomId,
                    recieverUserId!!,
                    recieverUserName!!,
                    recieverProfilePicture!!,
                    recieverFCMtoken!!
                )
                view.findNavController()
                    .navigate(R.id.action_userViewProfileFragment2_to_individualChatRoomFragment,
                        Bundle().apply { putSerializable(EXTRA_CHAT_ROOM, newChatRoom) })
                //message thichdaa create chatroom for user if not already created, send a first message "Hi"?
            }
        }

        PostService.getOtherUserPosts(newPostDetails?.user_id.toString()) { complete ->
            if (complete) {
                var imageUrlsList = mutableListOf<String>()
                for (url in PostService.posts) {
                    imageUrlsList.add(url.media_file)
                }

                adapter = PostRecycleAdapter(
                    requireContext(),
                    imageUrlsList) { post ->
                    //do something on click; open full post details
                    view.findNavController().navigate(
                        R.id.action_userViewProfileFragment2_to_viewPostFragment,
                        Bundle().apply { putSerializable(EXTRA_POST, post) })
                }
            }
            var spanCount = 2
            val orientation = resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                spanCount = 3
            }

            val layoutManager = GridLayoutManager(context, spanCount)
            val postRV = view.findViewById<RecyclerView>(R.id.userPostsRecyclerView)
            postRV.layoutManager = layoutManager
            postRV.adapter = adapter
        }
        return view
    }
}