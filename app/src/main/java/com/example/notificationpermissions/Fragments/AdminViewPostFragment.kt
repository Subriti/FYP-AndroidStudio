package com.example.notificationpermissions.Fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.notificationpermissions.Activities.DashboardActivity
import com.example.notificationpermissions.Models.Post
import com.example.notificationpermissions.Notifications.NotificationData
import com.example.notificationpermissions.Notifications.PushNotification
import com.example.notificationpermissions.Notifications.RetrofitInstance
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.AuthService
import com.example.notificationpermissions.Services.NotificationService
import com.example.notificationpermissions.Services.PostService
import com.example.notificationpermissions.Services.ReportService
import com.example.notificationpermissions.Utilities.App
import com.example.notificationpermissions.Utilities.EXTRA_POST
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*


class AdminViewPostFragment : Fragment() {
    var recieverId= ""
    var recieverName = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.admin_view_post_item, container, false)
       // (activity as DashboardActivity?)!!.currentFragment = this

        val postImage = view.findViewById<ImageView>(R.id.postImage)
        val username = view.findViewById<TextView>(R.id.username)
        val userProfile = view.findViewById<ImageView>(R.id.user_profile)
        val description = view.findViewById<TextView>(R.id.feed_description)
        val interestedUsers = view.findViewById<TextView>(R.id.countInterested)
        val createdDatetime = view.findViewById<TextView>(R.id.createdDatetime)

        val postDetails = arguments?.getSerializable(EXTRA_POST) as Post
        println("Post Id: ${postDetails.post_id}")

        context?.let {
            Glide.with(it).load(postDetails.media_file).into(postImage)
        }
        val userJSONObject = JSONObject(postDetails.post_by)
        val name = userJSONObject.getString("user_name")
        username?.text = name

        val profilePicture = userJSONObject.getString("profile_picture")
        println(postDetails.cloth_id)
        context?.let {
            Glide.with(it).load(profilePicture).into(userProfile)
        }
        val location = postDetails.location
        val clothId = postDetails.cloth_id

        val clothJSONObject = JSONObject(clothId)
        val clothSize = clothJSONObject.getString("cloth_size")
        val clothCondition = clothJSONObject.getString("cloth_condition")
        val clothSeason = clothJSONObject.getString("cloth_season")

        val clothCategory = clothJSONObject.getString("clothes_category_id")
        val categoryJSONObject = JSONObject(clothCategory)
        val category = categoryJSONObject.getString("category_name")

        val itemCategoryId = clothJSONObject.getString("item_category_id")
        val itemCategoryJSONObject = JSONObject(itemCategoryId)
        val itemCategory = itemCategoryJSONObject.getString("category_name")

        val donationStatus = postDetails.donation_status
        val donationJSONObject = JSONObject(donationStatus)
        val status = donationJSONObject.getString("donation_status")

        val customDescription =
            "${postDetails.description}\nCloth Category: $category \nItem Category: $itemCategory \nCloth Size: $clothSize \nCloth Condition: $clothCondition \nCloth Season: $clothSeason \nDonation Status: $status \nLocation: $location"

        description.text = customDescription

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
                   /* for (i in PostService.InterestedUsers) {
                        if (i.user_id == App.sharedPrefs.userID) {
                            alreadyLiked = true
                            markInterested.setImageResource(R.drawable.interest)
                        }
                    }*/
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
                    val dialogView: View = inflater.inflate(R.layout.dialog_interested_users, null)
                    builder.setView(dialogView)

                    val listView: ListView = dialogView.findViewById(R.id.list_view)
                    listView.adapter = adapter

                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }
            }
        }

        val deleteBtn = view.findViewById<ImageView>(R.id.deleteBtn)
        deleteBtn.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Confirm")
            builder.setMessage("Are you sure you want to delete this post?")
            builder.setPositiveButton("Yes") { dialog, which ->
               /* //also update the reports status to postDeleted(true)
                ReportService.updateDeletionStatus(postDetails.post_id){ setPostDeletedSuccess ->
                    println("Set Post Deleted status success: $setPostDeletedSuccess")
                }*/

                // Perform the deletion of the post
                PostService.deletePost(postDetails.post_id) { deletePostSuccess ->
                    println("Delete Post success: $deletePostSuccess")
                    if (deletePostSuccess) {
                        PostService.posts.remove(postDetails)
                        PostService.AllPosts.remove(postDetails)
                        PostService.InterestedUsersMapList.remove(postDetails.post_id)

                        Toast.makeText(context, "Post was deleted successfully", Toast.LENGTH_SHORT).show()

                        val reportFragment= ReportFragment()
                        activity?.supportFragmentManager?.beginTransaction()?.apply {
                            replace(R.id.replaceLayout, reportFragment)
                            addToBackStack(null)
                            commit()
                        }
                    }
                }
            }
            builder.setNegativeButton("No") { dialog, which ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        // Invalidate the options menu to force onPrepareOptionsMenu to be called again
        activity?.invalidateOptionsMenu()
    }
}
