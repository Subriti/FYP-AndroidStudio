package com.example.notificationpermissions.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notificationpermissions.Activities.AlertDetails
import com.example.notificationpermissions.Activities.DashboardActivity
import com.example.notificationpermissions.Adapters.NotificationAdapter
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.NotificationService
import com.example.notificationpermissions.Services.PostService
import com.example.notificationpermissions.Utilities.App
import com.example.notificationpermissions.Utilities.EXTRA_POST
import com.example.notificationpermissions.Utilities.POST_ID_EXTRA
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class NotificationFragment : Fragment() {
    lateinit var adapter: NotificationAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_notification, container, false)
        (activity as DashboardActivity?)!!.currentFragment = this

        val notificationRV = view.findViewById<RecyclerView>(R.id.notificationRV)
        println("in notification fragment")

        val noDataText= view.findViewById<TextView>(R.id.noDataTextView)
        NotificationService.getUserNotifications(App.sharedPrefs.userID) { complete ->
            if (complete) {
                if (NotificationService.notifications.isNotEmpty()) {
                    noDataText.visibility = View.GONE
                    adapter = NotificationAdapter(
                        requireContext(),
                        NotificationService.notifications
                    ) { notification ->
                        //open post details (eta pachi conditions launa parcha for if post likes ho ki rating ko notif)
                        println(notification.data)
                        val json = JSONObject(notification.data)
                        val postId = json.getString("post_id")

                        if (notification.title == "Please Rate the Donor") {
                            //create rating Dialog Box
                            ratingDialog(postId)
                        } else {
                            PostService.findPost(postId) { success ->
                                println(success)
                                if (success) {
                                    view.findNavController()
                                        .navigate(
                                            R.id.action_notificationFragment_to_viewPostFragment,
                                            Bundle().apply {
                                                putSerializable(
                                                    EXTRA_POST,
                                                    PostService.notificationPost
                                                )
                                            })
                                }
                            }
                        }
                    }
                    val layoutManager = LinearLayoutManager(context)
                    notificationRV.layoutManager = layoutManager
                    notificationRV.adapter = adapter
                } else if (NotificationService.notifications.isEmpty()) {
                    noDataText.visibility = View.VISIBLE
                }
            } else {
                noDataText.visibility = View.VISIBLE
                noDataText.text= "Notifications could not be loaded"
            }
        }
        return view
    }
    private fun ratingDialog(postId: String) {
        val builder = AlertDialog.Builder(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.confirmation_prompt, null)


        if (builder != null) {
            builder.setView(dialogView)
                .setPositiveButton("Send") { _, _ ->

                    val radioGroup =
                        dialogView.findViewById<RadioGroup>(R.id.confirmation_group)
                    val selection = radioGroup.checkedRadioButtonId

                    try {
                        var confirmation = dialogView.findViewById<RadioButton>(selection)
                        if (confirmation.text == "Yes") {

                            //show rating bar
                            val builder = AlertDialog.Builder(requireContext())
                            val dialogView = layoutInflater.inflate(R.layout.rating_prompt, null)

                            val ratingBar= dialogView.findViewById<RatingBar>(R.id.ratingBar)

                            if (builder != null) {
                                builder.setView(dialogView)
                                    .setPositiveButton("Rate") { _, _ ->
                                        //access the rating and use PostService.updateTrasaction rating to update the rating
                                        val rating= ratingBar.rating
                                        println(rating)
                                        println(ratingBar.numStars)

                                        //how to uniquely identify which notification belongs to which post
                                        if (postId != null) {
                                            PostService.updateRating(
                                                postId, rating, SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(
                                                    Calendar.getInstance().time)){ updateSuccess ->
                                                println("Update Transaction Rating success: $updateSuccess")
                                                if (updateSuccess) {
                                                    PostService.updateDonationStatus(
                                                        postId, "3"    // 3 -> completed status
                                                    ) { updateSuccess ->
                                                        println("Update Donation status success: $updateSuccess")
                                                        if (updateSuccess) {
                                                            Toast.makeText(
                                                                requireContext(),
                                                                "Thank you for your time and effort",
                                                                Toast.LENGTH_LONG
                                                            ).show()

                                                            //remove post from feed
                                                            for (i in PostService.AllPosts){
                                                                if (i.post_id== postId){
                                                                    PostService.AllPosts.remove(i)
                                                                    println("AllPost size is "+PostService.AllPosts.size)
                                                                }
                                                            }
                                                            val intent = Intent(requireContext(), DashboardActivity::class.java)
                                                            startActivity(intent)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    .setNegativeButton("Cancel") { _, _ ->
                                        //cancel and close the dialog
                                    }
                                    .show()
                            }
                        }
                        else{
                            val intent = Intent(requireContext(), DashboardActivity::class.java)
                            startActivity(intent)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            "Please input one choice",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                .setNegativeButton("Cancel") { _, _ -> //cancel and close the dialog
                }
                .show()
        }
    }
}