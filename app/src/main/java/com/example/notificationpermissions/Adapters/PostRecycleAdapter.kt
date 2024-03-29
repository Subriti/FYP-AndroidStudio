package com.example.notificationpermissions.Adapters

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.notificationpermissions.Models.Post
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.PostService
import org.json.JSONObject

class PostRecycleAdapter(
    private val context: Context,
    private val imageUrls: List<String>,
    val itemClick: (Post) -> Unit
) :
    RecyclerView.Adapter<PostRecycleAdapter.ViewHolder>() {

    lateinit var post_status: ImageView

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.user_post_item, parent, false)
        post_status= view.findViewById(R.id.post_status)
        return ViewHolder(view, itemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .into(holder.imageView)

        val post= PostService.posts[position]
        val donation = JSONObject(post.donation_status)
        val donationId = donation.getString("donation_status_id")
        val donationStatus = donation.getString("donation_status")

        //if donation ongoing; show post with grey tint
        if (donationId =="2" || donationStatus=="Ongoing"){
            val greyColor = Color.argb(150, 128, 128, 128)
            holder.imageView.setColorFilter(greyColor, PorterDuff.Mode.SRC_OVER)
            post_status.setImageResource(R.drawable.ongoingg)
            post_status.isVisible=true
        }

        //if donation completed; show post with red tint
        if (donationId =="3" || donationStatus=="Donated"){
            //val redColor = Color.argb(120, 255, 0, 0)
            //holder.imageView.setColorFilter(redColor, PorterDuff.Mode.SRC_OVER)
            val greyColor = Color.argb(200, 128, 128, 128)
            holder.imageView.setColorFilter(greyColor, PorterDuff.Mode.SRC_OVER)
            post_status.setImageResource(R.drawable.completed)
            post_status.isVisible=true
        }
        holder.bindPost(post)
    }

    override fun getItemCount(): Int {
        return imageUrls.size
    }

    inner class ViewHolder(itemView: View, val itemClick: (Post) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById<ImageView>(R.id.postImage)

        fun bindPost(post: Post) {
            itemView.setOnClickListener {
                itemClick(post)
            }
        }
    }
}

