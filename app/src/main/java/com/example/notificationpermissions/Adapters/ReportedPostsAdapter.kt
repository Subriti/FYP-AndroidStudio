package com.example.notificationpermissions.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.notificationpermissions.Models.Post
import com.example.notificationpermissions.Models.Reports
import com.example.notificationpermissions.Models.Transaction
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.AuthService
import com.example.notificationpermissions.Services.PostService
import com.example.notificationpermissions.Services.TransactionService
import org.json.JSONObject
import java.text.SimpleDateFormat

class ReportedPostsAdapter(
    private val context: Context, private val reports: List<Reports>,
    private val itemClick: (Reports) -> Unit
) : RecyclerView.Adapter<ReportedPostsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.report_list_view, parent, false)
        return ViewHolder(view, itemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindPost(reports[position])
    }

    override fun getItemCount(): Int {
        return reports.size
    }

    inner class ViewHolder(itemView: View, val itemClick: (Reports) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        val postImage: ImageView = itemView.findViewById(R.id.postImg)
        val feedback: TextView = itemView.findViewById(R.id.Feedback)
        val date: TextView = itemView.findViewById(R.id.date)
        val userName: TextView= itemView.findViewById(R.id.userName)
        val userImg: ImageView = itemView.findViewById(R.id.Userimage)

        fun bindPost(reports: Reports) {
            val json = JSONObject(reports.reported_by)
            val sender = json.getString("user_id")

            AuthService.findUserByID(sender) { findSuccess ->
                println("Find User: " + findSuccess)
                if (findSuccess) {
                    Glide.with(context).load(AuthService.profilePicture).into(userImg)
                    userName.text= AuthService.userName
                }
            }
            feedback.text = reports.feedback

            date.text = SimpleDateFormat("dd-MM-yyyy")
                .format(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(reports.report_date)!!)

            try {
                val json = JSONObject(reports.post_id)
                val postId = json.getString("post_id")
                PostService.findPost(postId) { findSuccess ->
                    println("Find Post: " + findSuccess)
                    if (findSuccess) {
                        Glide.with(context).load(PostService.notificationPost?.media_file)
                            .into(postImage)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error in Post Id", Toast.LENGTH_SHORT).show()
            }

            itemView.setOnClickListener {
                itemClick(reports)
            }
        }
    }
}