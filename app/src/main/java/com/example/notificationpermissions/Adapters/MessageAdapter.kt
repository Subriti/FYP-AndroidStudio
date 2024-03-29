package com.example.notificationpermissions.Adapters

import android.annotation.SuppressLint
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
import com.example.notificationpermissions.Models.Message
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Utilities.App
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MessageAdapter(
    context: Context, private val messageList: List<Message>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val context = context
    private val SENDER_MESSAGE = 1
    private val RECEIVER_MESSAGE = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View

        return if (viewType == SENDER_MESSAGE) {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.my_message_list_view, parent, false)
            SenderMessageViewHolder(view)
        } else {
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.message_list_view, parent, false)
            ReceiverMessageViewHolder(view)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]

        when (holder.itemViewType) {
            SENDER_MESSAGE -> {
                val senderMessageHolder = holder as SenderMessageViewHolder

                senderMessageHolder.timeStamp?.text = returnDateString(message.timeStamp)
                senderMessageHolder.messageBody?.text = message.message
            }
            RECEIVER_MESSAGE -> {
                val receiverMessageHolder = holder as ReceiverMessageViewHolder
                Glide.with(context).load(message.recieverProfilePicture)
                    .into(receiverMessageHolder.userImage!!)

                receiverMessageHolder.timeStamp?.text = returnDateString(message.timeStamp)
                receiverMessageHolder.messageBody?.text = message.message
            }
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        return if (message.receiverUserId == App.sharedPrefs.userID) SENDER_MESSAGE else RECEIVER_MESSAGE
    }

}

class SenderMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val timeStamp = itemView.findViewById<TextView>(R.id.myTimeStamp)
    val messageBody = itemView.findViewById<TextView>(R.id.myMessageBody)
}

class ReceiverMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val userImage = itemView.findViewById<ImageView>(R.id.Userimage)
    val timeStamp = itemView.findViewById<TextView>(R.id.timeStampLabel)
    //val userName = itemView.findViewById<TextView>(R.id.messageUserName)
    val messageBody = itemView.findViewById<TextView>(R.id.message)
}

@SuppressLint("NewApi")
@RequiresApi(Build.VERSION_CODES.N)
fun returnDateString(isoString: String): String {

    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ENGLISH)
    val outputFormatTime = SimpleDateFormat("h:mm a", Locale.ENGLISH)
    val outputFormatDate = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)

    val date = inputFormat.parse(isoString)
    val now = Calendar.getInstance().time
    val diff = TimeUnit.MILLISECONDS.toDays(date.time - now.time)

    val outputFormat = if (diff == 0L) {
        "Today, " + outputFormatTime.format(date)
    } else if (diff == -1L) {
        "Yesterday, " + outputFormatTime.format(date)
    } else if (diff >= -6L && diff <= 0L) {
        SimpleDateFormat("EEEE, h:mm a", Locale.ENGLISH).format(date)
    } else {
        outputFormatDate.format(date)
    }
    return outputFormat
}