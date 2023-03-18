package com.example.notificationpermissions.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.notificationpermissions.Models.Post
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.PostService
import java.util.ArrayList

class FeedGridRecyclerAdapter (
    private val context: Context,
    private val imageUrls: List<String>,
    private val postList: ArrayList<Post>,
    val itemClick: (Post) -> Unit
) :
    RecyclerView.Adapter<FeedGridRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.user_post_item, parent, false)
        return ViewHolder(view, itemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .into(holder.imageView)

        holder.bindPost(postList[position])
    }

    override fun getItemCount(): Int {
        return imageUrls.size
    }

    inner class ViewHolder(itemView: View, val itemClick: (Post) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.postImage)

        fun bindPost(post: Post) {
            itemView.setOnClickListener {
                itemClick(post)
            }
        }
    }
}