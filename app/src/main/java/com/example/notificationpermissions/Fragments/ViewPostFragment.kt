package com.example.notificationpermissions.Fragments

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.notificationpermissions.Models.Post
import com.example.notificationpermissions.Notifications.NotificationData
import com.example.notificationpermissions.Notifications.PushNotification
import com.example.notificationpermissions.Notifications.RetrofitInstance
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.AuthService
import com.example.notificationpermissions.Services.NotificationService
import com.example.notificationpermissions.Services.PostService
import com.example.notificationpermissions.Services.TransactionService
import com.example.notificationpermissions.Utilities.App
import com.example.notificationpermissions.Utilities.EXTRA_POST
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*


class ViewPostFragment : Fragment() {
    var recieverId= ""
    var recieverName = ""

    lateinit var postDetails: Post

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.view_user_post_item, container, false)
       // (activity as DashboardActivity?)!!.currentFragment = this

        val postImage = view.findViewById<ImageView>(R.id.postImage)
        val username = view.findViewById<TextView>(R.id.username)
        val userProfile = view.findViewById<ImageView>(R.id.user_profile)
        val description = view.findViewById<TextView>(R.id.feed_description)
        val markInterested = view.findViewById<ImageView>(R.id.markInterested)
        val interestedUsers = view.findViewById<TextView>(R.id.countInterested)
        val createdDatetime = view.findViewById<TextView>(R.id.createdDatetime)

        postDetails = arguments?.getSerializable(EXTRA_POST) as Post

        context?.let {
            Glide.with(it).load(postDetails.media_file).into(postImage)
        }

        val userJSONObject = JSONObject(postDetails.post_by)
        val name = userJSONObject.getString("user_name")
        username?.text = name

        val profilePicture = userJSONObject.getString("profile_picture")
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
                            markInterested.setImageResource(R.drawable.interest)
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
                    val dialogView: View = inflater.inflate(R.layout.dialog_interested_users, null)
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
                //markInterested.setImageResource(R.drawable.liked)
                markInterested.setImageResource(R.drawable.interest)
                isLiked = true

                //check if the user already liked, or is newly liked
                if (!alreadyLiked) {
                    println("view post: postby "+postDetails.post_by)
                    val json=JSONObject(postDetails.post_by)
                    val postBy= json.getString("user_name")
                    println(postBy)
                    if (postBy != App.sharedPrefs.userName) {
                        PostService.addInterestedUser(
                            App.sharedPrefs.userID, postDetails.post_id
                        ) { addUserSuccess ->
                            println("Add Interested User success: $addUserSuccess")
                            if (addUserSuccess) {
                                //on success display users ig
                                getUsers(postDetails)
                            }
                        }
                    }
                }
            } else {
               // markInterested.setImageResource(R.drawable.unliked)
                markInterested.setImageResource(R.drawable.notinterested)
                isLiked = false
                //else check if the photo is liked, if yes dislike it
                PostService.deleteInterestedUserByPosts(
                    postDetails.post_id, App.sharedPrefs.userID
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
        val post = JSONObject(postDetails.post_by)
        val postOwner = post.getString("user_name")
        val postOptions = view.findViewById<ImageView>(R.id.postOptions2)

        postOptions.isVisible = postOwner == App.sharedPrefs.userName
        postOptions?.setOnClickListener {
            val popupMenu = PopupMenu(context, postOptions)
            popupMenu.menuInflater.inflate(R.menu.post_menu, popupMenu.menu)

            val markAsDonatedItem = popupMenu.menu.findItem(R.id.markDonated)
            val editPostItem= popupMenu.menu.findItem(R.id.editPost)

            val donation = JSONObject(postDetails.donation_status)
            when (donation.getString("donation_status")) {
                "Ongoing" -> {
                    markAsDonatedItem.setTitle("Mark as Available")
                }
                "Donated" -> {
                    markAsDonatedItem.isVisible = false
                    editPostItem.isVisible=false
                }
                else -> {
                    markAsDonatedItem.setTitle("Mark as Donated")
                }
            }

            // Enable options menu handling for the fragment
            //setHasOptionsMenu(true)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                if (menuItem.title?.equals("Edit Post") == true) {
                    view.findNavController()
                        .navigate(R.id.action_viewPostFragment_to_editPostFragment,
                            Bundle().apply { putSerializable(EXTRA_POST, postDetails) })
                }

                if (menuItem.title == "Mark as Available"){
                    TransactionService.findOngoingTransactions {complete ->
                        if (complete){
                            for (transaction in TransactionService.onGoingTransactions){
                                val post= JSONObject(transaction.post_id)
                                val postId= post.getString("post_id")
                                if (postDetails.post_id==postId){
                                    TransactionService.updateTransactionStatus(transaction.transaction_id){complete->
                                        if (complete){
                                            markAsDonatedItem.setTitle("Mark as Donated")
                                            Toast.makeText(requireContext(),"Post is now available for donation",Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (menuItem.title == "Mark as Donated") {
                    val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    val dialogView = layoutInflater.inflate(R.layout.donation_ongoing_layout, null)

                    val recieverNameSpinner = dialogView.findViewById<Spinner>(R.id.receiverNames)

                    fun View?.removeSelf() {
                        this ?: return
                        val parentView = parent as? ViewGroup ?: return
                        parentView.removeView(this)
                    }

                    println(postDetails.post_id)
                    println(PostService.InterestedUsersMapList[postDetails.post_id])
                    //get Interested Users of the Post
                    val items = arrayListOf<String>()
                    val idReceiver = arrayListOf<String>()
                    if (PostService.InterestedUsersMapList.containsKey(postDetails.post_id)) {
                        for (i in PostService.InterestedUsersMapList[postDetails.post_id]!!) {
                            items.add(i.user_name)
                            idReceiver.add(i.user_id)
                        }

                        println(items.size)
                        if (items.size == 0) {
                            Toast.makeText(
                                requireContext(),
                                "The post does not have any interested receiver",
                                Toast.LENGTH_LONG
                            ).show()
                            dialogView.removeSelf()
                        }
                        if (items.size > 0) {
                            val adapter = ArrayAdapter(
                                requireContext(), android.R.layout.simple_spinner_item, items
                            )
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            recieverNameSpinner.adapter = adapter
                            recieverNameSpinner.setSelection(0)
                            recieverNameSpinner.onItemSelectedListener =
                                object : AdapterView.OnItemSelectedListener {
                                    override fun onItemSelected(
                                        parent: AdapterView<*>, view: View?, position: Int, id: Long
                                    ) {
                                        recieverNameSpinner.setSelection(position)
                                        recieverId = idReceiver[position]
                                        recieverName= items[position]
                                        println("Selected reciever : $recieverId")
                                        println("Selected reciever name : $recieverName")
                                    }
                                    override fun onNothingSelected(parent: AdapterView<*>?) {
                                        // do nothing
                                    }
                                }
                        }
                    }

                    if (builder != null) {
                        builder.setView(dialogView).setPositiveButton("Verify") { _, i ->
                            //call service to update donation status; write backend code for transactions and record it
                            PostService.updateDonationStatus(
                                postDetails.post_id, "2"
                            ) {// 2 -> ongoing status
                                    updateSuccess ->
                                println("Update Donation status success: $updateSuccess")
                                if (updateSuccess) {
                                    PostService.createTransaction(
                                        postDetails.post_id,
                                        recieverId,
                                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(Calendar.getInstance().time)
                                    ) { createSuccess ->
                                        println("Create Transaction success: $createSuccess")
                                        if (createSuccess) {
                                            //send Notification to the reciever of the clothes for rating
                                            val title = "Please Rate the Donor"
                                            val message =
                                                "Share your experience on the donation process with ${App.sharedPrefs.userName} :)"

                                            // Pass the ID along with the notification payload
                                            val data = mapOf("post_id" to postDetails.post_id)

                                            println("Reciever Id $recieverId")
                                            println("RecieverName: $recieverName")        

                                            AuthService.getFCMToken(recieverName) { response ->
                                                println("Get FCM Token success: $response")
                                                println("Recipient Token during notification sending is:${AuthService.recipientToken}")
                                                PushNotification(
                                                    NotificationData(title, message, data),
                                                    AuthService.recipientToken
                                                ) .also { sendNotification(it) }

                                                //add notification to the database
                                                NotificationService.addNotification(
                                                    title,
                                                    message,
                                                    postDetails.post_id,
                                                    App.sharedPrefs.userID,
                                                    AuthService.recipientToken,
                                                    recieverId
                                                ) { createSuccess ->
                                                    println("Create Notification success: $createSuccess")
                                                    if (createSuccess) {
                                                       println("Notification added")
                                                    }
                                                }
                                                //go to profile
                                                view.findNavController()
                                                    .navigate(R.id.action_viewPostFragment_to_profileFragment) }
                                        }
                                    }
                                }
                            }
                        }
                        builder.setNegativeButton("Cancel") { dialog, which ->
                            dialog.dismiss()
                        }

                        builder.setView(dialogView).show()
                    }
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
                                    context, "Post was deleted successfully", Toast.LENGTH_SHORT
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
                //Toast.makeText(context, "You Clicked " + menuItem.title, Toast.LENGTH_SHORT).show()
                true
            }
            // Showing the popup menu
            popupMenu.show()
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        // Invalidate the options menu to force onPrepareOptionsMenu to be called again
        activity?.invalidateOptionsMenu()
    }

    val TAG = "DonationProcess"
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
