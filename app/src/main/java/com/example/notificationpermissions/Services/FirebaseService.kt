package com.example.notificationpermissions.Services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import com.example.notificationpermissions.Activities.AlertDetails
import com.example.notificationpermissions.Activities.DashboardActivity
import com.example.notificationpermissions.Fragments.ProfileFragment
import com.example.notificationpermissions.Fragments.ViewPostFragment
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Utilities.App
import com.example.notificationpermissions.Utilities.EXTRA_POST
import com.example.notificationpermissions.Utilities.POST_ID_EXTRA
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import kotlin.random.Random


private const val CHANNEL_ID = "my_channel"

class FirebaseService: FirebaseMessagingService() {
    override fun onNewToken(newToken: String) {
        super.onNewToken(newToken)
        App.sharedPrefs.token= newToken
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        println("Firebase Service onMessage Received ${message.data}")
        showNotification(message)
    }

    private fun showNotification(message: RemoteMessage) {

        //intent actually should be the current post liked ko viewPostFragment
        var intent = Intent(this, DashboardActivity::class.java)

        if (message.data["title"]=="Please Rate the Donor"){
            println(message.data["data"])
            val post= JSONObject(message.data["data"])
            val id= post.getString("post_id")
            println(id)
            //sending the post Id with each notification intent to recognize notifications
            intent = Intent(this, AlertDetails::class.java)
            intent.putExtra(POST_ID_EXTRA, id)
        }

        if (message.data["title"]=="Someone was interested on your post"){
            //get message.data, then go to viewPostFragment

            println(message.data["data"])
            val json = JSONObject(message.data["data"])
            val post_id = json.getString("post_id")

            PostService.findPost(post_id) { success ->
                println(success)
                if (success) {
                    intent = Intent(this, DashboardActivity::class.java)

                    println(PostService.notificationPost?.post_id)

                    if (PostService.notificationPost != null) {
                        intent.putExtra(EXTRA_POST, PostService.notificationPost)
                    }

                    println(intent)
                }
            }
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // Since android Oreo notification channel is needed.
            createNotificationChannel(notificationManager)
        }

        println(intent)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["message"])
            .setSmallIcon(R.drawable.ic_baseline_circle_notifications_24)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channelName= "channelName"
        val channel = NotificationChannel(
            CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description="My channel description"
            enableLights(true)
            lightColor= Color.Green.green.toInt()
        }
        notificationManager.createNotificationChannel(channel)
    }
}