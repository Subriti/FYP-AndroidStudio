package com.example.notificationpermissions.Notifications

data class PushNotification(
    val data: NotificationData,
    val to:String   //recipient or can be a topic; one or several tokens
)