package com.example.notificationpermissions.Fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
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
import com.example.notificationpermissions.Adapters.PostRecycleAdapter
import com.example.notificationpermissions.Activities.DashboardActivity
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.PostService
import com.example.notificationpermissions.Utilities.App
import com.example.notificationpermissions.Utilities.EXTRA_POST


class ProfileFragment : Fragment() {
    private var galleryRequestCode = 1000
    private lateinit var imgGallery: ImageView
    private lateinit var imgButton: Button

    private lateinit var imgView: ImageView

    lateinit var adapter: PostRecycleAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        (activity as DashboardActivity?)!!.currentFragment = this

        //user display picture
        imgGallery = view.findViewById(R.id.profile_image)
        context?.let {
            Glide.with(it).load(App.sharedPrefs.profilePicture).into(imgGallery)
        }

        val location = view.findViewById<TextView>(R.id.UserLocation)
        val phoneNumber = view.findViewById<TextView>(R.id.UserPhone)
        val email = view.findViewById<TextView>(R.id.UserEmail)

        email.text = "  ${App.sharedPrefs.userEmail}"
        phoneNumber.text = "  ${App.sharedPrefs.phoneNumber}"
        location.text = "  ${App.sharedPrefs.location}"


        imgButton = view.findViewById<Button>(R.id.editProfile)
        imgButton.setOnClickListener {
            view.findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        PostService.getUserPosts(App.sharedPrefs.userID) { complete ->
            if (complete) {
                var imageUrlsList = mutableListOf<String>()
                for (url in PostService.posts) {
                    imageUrlsList.add(url.media_file)
                }

                adapter = PostRecycleAdapter(
                    requireContext(), imageUrlsList
                ) { post ->
                    //do something on click; open full post details
                    view.findNavController()
                        .navigate(
                            R.id.action_profileFragment_to_viewPostFragment,
                            Bundle().apply { putSerializable(EXTRA_POST, post) })
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
        }
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {

            if (requestCode == galleryRequestCode) {
                //for gallery
                if (data != null) {
                    imgGallery.setImageURI(data.data)
                }
            }
        }
    }
}