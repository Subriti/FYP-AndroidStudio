package com.example.notificationpermissions

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class FeedRecyclerAdapter(
    private val context: Context,
    private val imageUrls: List<String>,
    val itemClick: (Post) -> Unit
) :
    RecyclerView.Adapter<FeedRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.post_feed_item, parent, false)
        return ViewHolder(view, itemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .into(holder.postImage)

        holder.bindPost(PostService.AllPosts[position], context)
    }

    override fun getItemCount(): Int {
        return imageUrls.size
    }

    inner class ViewHolder(itemView: View, val itemClick: (Post) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        val postImage = itemView.findViewById<ImageView>(R.id.postImage)
        private val username = itemView.findViewById<TextView>(R.id.username)
        private val userProfile = itemView.findViewById<ImageView>(R.id.user_profile)
        private val description = itemView.findViewById<TextView>(R.id.feed_description)
        private val markInterested = itemView.findViewById<ImageView>(R.id.markInterested)


        fun bindPost(post: Post, context: Context) {
            username?.text = post.post_by
            description?.text = post.description
            Glide.with(context).load(post.created_datetime).into(userProfile)

            userProfile.setOnClickListener {
                    //open user profile with posts
            }

            var isLiked = false;
            markInterested.setOnClickListener {
                if (!isLiked) {
                    markInterested.setImageResource(R.drawable.ic_baseline_star_24)
                    isLiked = true
                } else {
                    markInterested.setImageResource(R.drawable.ic_baseline_star_border_24)
                    isLiked = false
                }
            }

            itemView.setOnClickListener { itemClick(post) }
        }
    }
}

