package com.example.notificationpermissions.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notificationpermissions.Activities.DashboardActivity
import com.example.notificationpermissions.Adapters.NotificationAdapter
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.NotificationService
import com.example.notificationpermissions.Services.PostService
import com.example.notificationpermissions.Utilities.App
import com.example.notificationpermissions.Utilities.EXTRA_POST
import org.json.JSONObject

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

        NotificationService.getUserNotifications(App.sharedPrefs.userID) { complete ->
            if (complete) {
                adapter = NotificationAdapter(
                    requireContext(),
                    NotificationService.notifications
                ) { notification ->
                    //open post details (eta pachi conditions launa parcha for if post likes ho ki rating ko notif)
                    println(notification.data)
                    val json = JSONObject(notification.data)
                    val post_id = json.getString("post_id")

                    PostService.findPost(post_id) { success ->
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
                val layoutManager = LinearLayoutManager(context)
                notificationRV.layoutManager = layoutManager
                notificationRV.adapter = adapter
            }
        }
        return view
    }
}