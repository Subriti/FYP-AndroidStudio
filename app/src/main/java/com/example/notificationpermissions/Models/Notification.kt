package com.example.notificationpermissions.Models

data class Notification(
    val id: String,
    val message: String,
    val data: String,
    val senderId:String,
    val recipientToken: String,
    val recieverId: String
)