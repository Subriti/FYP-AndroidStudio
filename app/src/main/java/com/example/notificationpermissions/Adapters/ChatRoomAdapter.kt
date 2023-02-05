package com.example.notificationpermissions.Adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.notificationpermissions.Models.ChatRoom
import com.example.notificationpermissions.R
import java.util.*

class ChatRoomAdapter(val context: Context, val chatRoom: ArrayList<ChatRoom>,
                      private val itemClick: (ChatRoom) -> Unit ): RecyclerView.Adapter<ChatRoomAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View, val itemClick: (ChatRoom) -> Unit): RecyclerView.ViewHolder(itemView){
        val userImage= itemView?.findViewById<ImageView>(R.id.messageUserimage)
        val userName= itemView?.findViewById<TextView>(R.id.messageUserName)
        val messageBody= itemView?.findViewById<TextView>(R.id.messageBodyLabel)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bindMessage(context: Context, chatRoom: ChatRoom){
            Glide.with(context).load(chatRoom.recieverProfilePicture).into(userImage!!)
            userName?.text= chatRoom.recieverUserName
            /*messageBody?.text=chatRoom.message*/

            itemView.setOnClickListener { itemClick(chatRoom) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(context).inflate(R.layout.chat_room_list_view, parent, false)
        return ViewHolder(view,itemClick)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.bindMessage(context, chatRoom[position])
    }

    override fun getItemCount(): Int {
        return  chatRoom.count()
    }


}