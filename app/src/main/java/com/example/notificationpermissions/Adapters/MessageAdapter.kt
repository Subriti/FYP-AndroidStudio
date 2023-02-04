package com.example.notificationpermissions.Adapters

import android.content.Context
import android.os.Build
import android.util.Log
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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(val context: Context, val messages: ArrayList<Message> ): RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val userImage= itemView?.findViewById<ImageView>(R.id.messageUserimage)
        val timeStamp= itemView?.findViewById<TextView>(R.id.timeStampLabel)
        val userName= itemView?.findViewById<TextView>(R.id.messageUserName)
        val messageBody= itemView?.findViewById<TextView>(R.id.messageBodyLabel)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bindMessage(context: Context, message: Message){
            Glide.with(context).load(message.recieverProfilePicture).into(userImage!!)
            userName?.text= message.recieverUserName
            timeStamp?.text= returnDateString(message.timeStamp)
            messageBody?.text=message.message
        }

        fun returnDateString(isoString: String): String{
            val isoFormatter= SimpleDateFormat("yyyy-mm-dd'T'hh:mm:ss.SSS", Locale.getDefault())

            isoFormatter.timeZone= TimeZone.getTimeZone("UTC")
            var convertedDate= Date()
            try{
                convertedDate= isoFormatter.parse(isoString)
            }
            catch (e: ParseException){
                Log.d("PARSE","Failed to parse timeStamp")
            }

            val outDateString= SimpleDateFormat("E, h:mm a", Locale.getDefault())
            return outDateString.format(convertedDate)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(context).inflate(R.layout.message_list_view, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.bindMessage(context, messages[position])
    }

    override fun getItemCount(): Int {
        return  messages.count()
    }


}