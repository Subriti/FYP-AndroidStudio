package com.example.notificationpermissions
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

open class FirebaseMessagingService : com.google.firebase.messaging.FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title: String? = message.notification!!.title
        val message: String? = message.notification!!.body
        // Use the data to display a notification
        // ...

      /*  var token: String = FirebaseInstanceId.getInstance().getToken()
        val fm: FirebaseMessaging = FirebaseMessaging.getInstance()
        fm.send(
            Builder(token)
                .setNotification(RemoteMessage.Notification(title,message))
                .build()
        )*/
    }
}