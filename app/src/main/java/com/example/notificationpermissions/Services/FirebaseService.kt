package com.example.notificationpermissions.Services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.core.app.NotificationCompat
import com.example.notificationpermissions.Activities.AlertDetails
import com.example.notificationpermissions.Activities.DashboardActivity
import com.example.notificationpermissions.Models.ChatRoom
import com.example.notificationpermissions.Models.Post
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Utilities.App
import com.example.notificationpermissions.Utilities.EXTRA_CHAT_ROOM
import com.example.notificationpermissions.Utilities.EXTRA_POST
import com.example.notificationpermissions.Utilities.POST_ID_EXTRA
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import kotlin.random.Random

private const val CHANNEL_ID = "my_channel"

class FirebaseService : FirebaseMessagingService() {
    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        App.sharedPrefs.token = newToken
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        println("Firebase Service onMessage Received ${message.data}")
        showNotification(message)
    }

    private fun showNotification(message: RemoteMessage) {
        //intent actually should be the current post liked ko viewPostFragment
        var intent = Intent(this, DashboardActivity::class.java)
        println(message.data["data"])
        val post = JSONObject(message.data["data"])
        val id = post.optString("post_id", null)
        val chatRoomId = post.optString("chat_room_id", null)
        val userId = post.optString("user_id", null)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Since android Oreo notification channel is needed.
            createNotificationChannel(notificationManager)
        }

        if(id!=null){
            var postDetails: Post? = null
            PostService.findPost(id) { success ->
                if (success) {
                    postDetails = PostService.notificationPost
                    if (message.data["title"] == "Please Rate the Donor") {
                        //sending the post Id with each notification intent to recognize notifications
                        intent = Intent(this, AlertDetails::class.java)
                        intent.putExtra(POST_ID_EXTRA, id)
                    }

                    if (message.data["title"] == "Someone was interested on your post") {
                        //get message.data, then go to viewPostFragment
                        intent = Intent(this, DashboardActivity::class.java)
                        if (postDetails != null) {
                            intent.putExtra(EXTRA_POST, postDetails)
                        }
                    }
                    println(intent)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    val pendingIntent = PendingIntent.getActivity(
                        this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                    )
                    val notification =
                        NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle(message.data["title"])
                            .setContentText(message.data["message"])
                            .setSmallIcon(R.drawable.ic_baseline_circle_notifications_24).setAutoCancel(true)
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            .setContentIntent(pendingIntent).build()

                    notificationManager.notify(notificationID, notification)
                }
            }
        }

        if (chatRoomId!=null && userId!=null){
            var chatDetails: ChatRoom? = null
            MessageService.findUser(chatRoomId, userId) { success ->
                if (success) {
                    chatDetails = MessageService.userChatRooms[0]
                    if (chatDetails != null) {
                        intent = Intent(this, DashboardActivity::class.java)
                        intent.putExtra(EXTRA_CHAT_ROOM, chatDetails)

                        println(intent)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        val pendingIntent = PendingIntent.getActivity(
                            this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                        )
                        val notification =
                            NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle(message.data["title"])
                                .setContentText(message.data["message"])
                                .setSmallIcon(R.drawable.ic_baseline_circle_notifications_24).setAutoCancel(true)
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setContentIntent(pendingIntent).build()

                        notificationManager.notify(notificationID, notification)
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun createNotificationChannel(notificationManager: NotificationManager) {
    val channelName = "channelName"
    val channel = NotificationChannel(
        CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH
    ).apply {
        description = "My channel description"
        enableLights(true)
        lightColor = Color.Green.green.toInt()
    }
    notificationManager.createNotificationChannel(channel)
}