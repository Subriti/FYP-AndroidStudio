package com.example.notificationpermissions.Notifications

data class NotificationData(
    val title: String,
    val message: String,
    val data: Map<String, String>
)