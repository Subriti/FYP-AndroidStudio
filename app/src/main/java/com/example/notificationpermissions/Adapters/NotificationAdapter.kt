package com.example.notificationpermissions.Adapters

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.notificationpermissions.Activities.DashboardActivity
import com.example.notificationpermissions.Models.Notification
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.AuthService
import com.example.notificationpermissions.Services.PostService
import org.json.JSONObject
import java.util.*

class NotificationAdapter(val context: Context, val notification: ArrayList<Notification>,
                          private val itemClick: (Notification) -> Unit ): RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View, val itemClick: (Notification) -> Unit): RecyclerView.ViewHolder(itemView){
        val userImage= itemView?.findViewById<ImageView>(R.id.Userimage)
        val messageBody= itemView?.findViewById<TextView>(R.id.message)
        val postImage= itemView?.findViewById<ImageView>(R.id.postImg)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bindMessage(context: Context, notification: Notification){

                val json = JSONObject(notification.senderId)
                val sender = json.getString("user_id")

            AuthService.findUserByID(sender) { findSuccess ->
                println("Find User: "+findSuccess)
                if (findSuccess) {
                    Glide.with(context).load(AuthService.profilePicture).into(userImage!!)
                }
            }
            messageBody?.text=notification.message
            try {
                val json = JSONObject(notification.data)
                val postId = json.getString("post_id")
                PostService.findPost(postId) { findSuccess ->
                    println("Find Post: "+findSuccess)
                    if (findSuccess) {
                        Glide.with(context).load(PostService.notificationPost?.media_file).into(postImage!!)
                    }
                }
            }
            catch (e:Exception){
                Toast.makeText(context,"data doesn't contain post_id",Toast.LENGTH_SHORT).show()
            }
            itemView.setOnClickListener { itemClick(notification) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(context).inflate(R.layout.notification_list_view, parent, false)
        return ViewHolder(view,itemClick)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.bindMessage(context, notification[position])
    }

    override fun getItemCount(): Int {
        return notification.count()
    }


}