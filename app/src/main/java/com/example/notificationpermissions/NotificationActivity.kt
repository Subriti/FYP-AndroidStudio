package com.example.notificationpermissions

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class NotificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        var hasNotificationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_NOTIFICATION_POLICY
        ) == PackageManager.PERMISSION_GRANTED /*-> {

        }
        shouldShowRequestPermissionRationale()->{

        }else ->{
            requestPermissionLauncher.launch(
                Manifest.permission.REQUESTED_PERMISSION)
        }*/

        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {

                    hasNotificationPermission = isGranted
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            }

        var permission= findViewById<Button>(R.id.requestPermission)
        var notif= findViewById<Button>(R.id.showNotifications)

        permission.setOnClickListener {

            requestPermissionLauncher.launch(
                Manifest.permission.ACCESS_NOTIFICATION_POLICY)
        }

        notif.setOnClickListener {
            showNotification()
        }
    }


    private fun showNotification() {
        val intent = Intent(this, AlertDetails::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)


        val notification = NotificationCompat.Builder(this, "channel_id")
            .setSmallIcon(R.drawable.ic_baseline_circle_notifications_24)
            .setContentTitle("Notification")
            .setContentText("This is test. First Notification")
            /*.setStyle(NotificationCompat.BigTextStyle()
                .bigText("Much longer text that cannot fit one line..."))
            */.setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)
    }
}