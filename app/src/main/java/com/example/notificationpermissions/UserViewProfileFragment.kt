package com.example.notificationpermissions

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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.notificationpermissions.Utilities.EXTRA_POST
import org.json.JSONObject


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

        val postDetails = arguments?.getSerializable(EXTRA_POST) as Post
        println(postDetails)
        println(postDetails.post_by)

        val userJSONObject = JSONObject(postDetails.post_by)
        val userId = userJSONObject.getString("user_id")
        val emailadd= userJSONObject.getString("email")
        val phone= userJSONObject.getString("phone_number")
        val profilePicture = userJSONObject.getString("profile_picture")
        //user display picture
        imgGallery = view.findViewById(R.id.profile_image)
        context?.let {
            Glide.with(it).load(profilePicture).into(imgGallery)
        }

        val location = view.findViewById<TextView>(R.id.UserLocation)
        val phoneNumber = view.findViewById<TextView>(R.id.UserPhone)
        val email = view.findViewById<TextView>(R.id.UserEmail)

        email.text = "  ${emailadd}"
        phoneNumber.text = "  ${phone}"
        location.text = "  ${postDetails.location}"

        imgButton = view.findViewById<Button>(R.id.editProfile)
        imgButton.isVisible=false


        PostService.getUserPosts(userId) { complete ->
            if (complete) {
                var imageUrlsList = mutableListOf<String>()
                for (url in PostService.posts) {
                    imageUrlsList.add(url.media_file)
                }

                adapter = PostRecycleAdapter(requireContext(), imageUrlsList, requireFragmentManager()) {post ->
                    //do something on click; open full post details
                    val viewPostFragment = ViewPostFragment().apply {
                        arguments=Bundle().apply { putSerializable(EXTRA_POST,post) }
                    }
                    val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
                    transaction.replace(R.id.profile_fragment, viewPostFragment)
                    transaction.addToBackStack("profileFragment")
                    //transaction.addToBackStack(null)
                    transaction.setReorderingAllowed(true)
                    transaction.commit()
                    imgButton.isVisible=false
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
}