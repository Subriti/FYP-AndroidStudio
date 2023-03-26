package com.example.notificationpermissions.Adapters

import android.content.Context
import android.database.DataSetObserver
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.notificationpermissions.Models.User
import com.example.notificationpermissions.R
import java.util.*

class UserAdapter(
    val context: Context,
    val userList: ArrayList<User>,
    private val itemClick: (User) -> Unit
) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>(), ListAdapter, Filterable {

    inner class ViewHolder(
        itemView: View,
        val itemClick: (User) -> Unit
    ) :
        RecyclerView.ViewHolder(itemView) {

        val userImage = itemView.findViewById<ImageView>(R.id.Userimage)
        val userName = itemView.findViewById<TextView>(R.id.messageUserName)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bindMessage(context: Context, user: User) {
            Glide.with(context).load(user.user_profile).into(userImage)
            userName.text = user.user_name

            itemView.setOnClickListener { itemClick(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.chat_room_list_view, parent, false)
        return ViewHolder(view, itemClick)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindMessage(context, userList[position])
    }

    override fun registerDataSetObserver(observer: DataSetObserver) {
        notifyDataSetChanged()
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver) {
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return userList.size
    }

    override fun getItem(position: Int): Any {
        return userList[position].user_name
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val itemView: View = convertView ?: LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_room_list_view, parent, false)

        val user = userList[position]
        val nameTextView = itemView.findViewById<TextView>(R.id.messageUserName)
        nameTextView.text = user.user_name

        val userImage = itemView.findViewById<ImageView>(R.id.Userimage)
        Glide.with(context).load(user.user_profile).into(userImage)

        return itemView
    }

    override fun getViewTypeCount(): Int {
        return count
    }

    override fun isEmpty(): Boolean {
        return userList.isEmpty()
    }

    override fun areAllItemsEnabled(): Boolean {
        return false
    }

    override fun isEnabled(position: Int): Boolean {
        return true
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                if (constraint.isNullOrEmpty()) {
                    results.values = userList
                    results.count = userList.size
                } else {
                    val filteredUsers = userList.filter {
                        it.user_name.contains(constraint, ignoreCase = true)
                    }
                    results.values = filteredUsers
                    results.count = filteredUsers.size
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                userList.clear()
                if (results != null && results.count > 0) {
                    userList.addAll(results.values as List<User>)
                }else{
                    userList.addAll(userList)
                }
                notifyDataSetChanged()
            }
        }
    }
}
