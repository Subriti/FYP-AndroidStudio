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

class PostRecycleAdapter(
    private val context: Context,
    private val imageUrls: List<String>,
    val itemClick: (Post) -> Unit
) :
    RecyclerView.Adapter<PostRecycleAdapter.ViewHolder>() {

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

        holder.bindPost(PostService.posts[position], context)
    }

    override fun getItemCount(): Int {
        return imageUrls.size
    }

    inner class ViewHolder(itemView: View, val itemClick: (Post) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.findViewById<ImageView>(R.id.postImage)
        //private val postOptions= itemView.findViewById<ImageView>(R.id.postOptions)

        fun bindPost(post: Post, context: Context) {
            /*postOptions?.setOnClickListener {
                val popupMenu = PopupMenu(context, postOptions)

                // Inflating popup menu from popup_menu.xml file
                popupMenu.menuInflater.inflate(R.menu.post_menu, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener { menuItem ->
                    println(menuItem.title)
                    if (menuItem.title?.equals("Edit Post") == true) {
                        val editPostFragment = EditPostFragment().apply {
                            arguments=Bundle().apply { putSerializable(EXTRA_POST,post) }
                        }
                        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
                        transaction.replace(R.id.profile_fragment, editPostFragment)
                        transaction.addToBackStack("profileFragment")
                        //transaction.addToBackStack(null)
                        transaction.setReorderingAllowed(true)
                        transaction.commit()



                        true
                    }

                    if (menuItem.title=="Mark as Donated"){

                    }
                    if (menuItem.title=="Delete Post"){

                    }
                    // Toast message on menu item clicked
                    Toast.makeText(context, "You Clicked " + menuItem.title, Toast.LENGTH_SHORT)
                        .show()
                    true

                }
                // Showing the popup menu
                popupMenu.show()
            }*/

            itemView.setOnClickListener {
                itemClick(post)
            }
        }
    }
}

