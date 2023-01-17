package com.example.notificationpermissions

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

/*

class PostRecycleAdapter(private val context: Context, private val posts: List<Post>, val itemClick: (Post) -> Unit): RecyclerView.Adapter<PostRecycleAdapter.PostHolder>() {

    inner class PostHolder(itemView: View, val itemClick: (Post) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val postImage = itemView?.findViewById<ImageView>(R.id.postImage)
        private val postOptions = itemView?.findViewById<ImageView>(R.id.postOptions)

        fun bindProduct(post: Post, context: Context) {
            //val resourceId= context.resources.getIdentifier(post.image, "drawable", context.packageName)
            //productImage?.setImageResource(resourceId)

            //postImage?.set(post.media_file, )     bind imagee but how?


            postOptions?.setOnClickListener {
                val popupMenu = PopupMenu(context, postOptions)

                // Inflating popup menu from popup_menu.xml file
                popupMenu.menuInflater.inflate(R.menu.post_menu, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener { menuItem ->
                    // Toast message on menu item clicked
                    Toast.makeText(context, "You Clicked " + menuItem.title, Toast.LENGTH_SHORT)
                        .show()
                    true
                }
                // Showing the popup menu
                popupMenu.show()
            }

            itemView.setOnClickListener { itemClick(post) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_post_item, parent, false)
        return PostHolder(view, itemClick)
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        var media_file = PostService.posts[position].media_file
        */
/*for( url in PostService.posts ){
            var media_file= url.media_file
        }*//*

        Glide.with(holder.itemView.context).load(media_file).into(holder)
        holder?.bindProduct(posts[position], context)
    }

    override fun getItemCount(): Int {
        return posts.count()
    }
}
*/


class PostRecycleAdapter(private val context: Context, private val imageUrls: List<String>,val itemClick: (Post) -> Unit) :
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

        holder.bindPost(PostService.posts[position],context)
    }

    override fun getItemCount(): Int {
        return imageUrls.size
    }

    inner class ViewHolder(itemView: View,val itemClick: (Post) -> Unit) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.findViewById<ImageView>(R.id.postImage)
        private val postOptions= itemView.findViewById<ImageView>(R.id.postOptions)

        fun bindPost(post:Post, context: Context){
            postOptions?.setOnClickListener {
                val popupMenu = PopupMenu(context, postOptions)

                // Inflating popup menu from popup_menu.xml file
                popupMenu.menuInflater.inflate(R.menu.post_menu, popupMenu.menu)

                popupMenu.setOnMenuItemClickListener { menuItem ->
                    // Toast message on menu item clicked
                    Toast.makeText(context, "You Clicked " + menuItem.title, Toast.LENGTH_SHORT)
                        .show()
                    true
                }
                // Showing the popup menu
                popupMenu.show()
            }

            itemView.setOnClickListener { itemClick(post) }
        }
        }
    }

