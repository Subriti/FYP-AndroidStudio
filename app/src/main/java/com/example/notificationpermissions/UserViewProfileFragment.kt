package com.example.notificationpermissions

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
        imgButton.text = "Message"
        imgButton.setOnClickListener {
            view.findNavController().navigate(R.id.action_userViewProfileFragment2_to_chatFragment)
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