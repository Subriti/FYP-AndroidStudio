package com.example.notificationpermissions

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.notificationpermissions.Utilities.EXTRA_POST
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*


class ViewPostFragment : Fragment() {
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

        println(postDetails.media_file)
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

        //hiding and displaying the edit menu
        println("Post Owner: " + postDetails.post_by)
        val post = JSONObject(postDetails.post_by)
        val postOwner = post.getString("user_name")
        println("Logged in User: " + App.sharedPrefs.userName)
        val postOptions = view.findViewById<ImageView>(R.id.postOptions2)

        postOptions.isVisible = postOwner == App.sharedPrefs.userName
        postOptions?.setOnClickListener {
            val popupMenu = PopupMenu(context, postOptions)

            // Inflating popup menu from popup_menu.xml file
            popupMenu.menuInflater.inflate(R.menu.post_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                println(menuItem.title)
                if (menuItem.title?.equals("Edit Post") == true) {
                    view.findNavController().navigate(
                        R.id.action_viewPostFragment_to_editPostFragment,
                        Bundle().apply { putSerializable(EXTRA_POST, postDetails) })
                }
                if (menuItem.title == "Mark as Donated") {

                }
                if (menuItem.title == "Delete Post") {
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("Confirm")
                    builder.setMessage("Are you sure you want to delete this post?")
                    builder.setPositiveButton("Yes") { dialog, which ->
                        // Perform the deletion of the post
                        PostService.deletePost(
                            postDetails.post_id,
                        ) { deleteUserSuccess ->
                            println("Delete Post success: $deleteUserSuccess")
                            if (deleteUserSuccess) {
                                PostService.posts.remove(postDetails)
                                PostService.AllPosts.remove(postDetails)
                                println(PostService.InterestedUsersMapList)
                                println(PostService.InterestedUsersMapList[postDetails.post_id])
                                PostService.InterestedUsersMapList.remove(postDetails.post_id)
                                Toast.makeText(
                                    context,
                                    "Post was deleted successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                view.findNavController()
                                    .navigate(R.id.action_viewPostFragment_to_profileFragment)
                            }

                        }
                    }
                    builder.setNegativeButton("No") { dialog, which ->
                        dialog.dismiss()
                    }
                    val dialog = builder.create()
                    dialog.show()
                }
                // Toast message on menu item clicked
                Toast.makeText(context, "You Clicked " + menuItem.title, Toast.LENGTH_SHORT)
                    .show()
                true
            }
            // Showing the popup menu
            popupMenu.show()
        }
        return view
    }
}
