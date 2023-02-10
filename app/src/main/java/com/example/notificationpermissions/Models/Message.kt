package com.example.notificationpermissions.Models

class Message (val messageId: String, val message: String, val timeStamp: String, val senderUserId:String, val receiverUserId:String, val recieverUserName: String, val recieverProfilePicture: String, val recieverFCMtoken: String, val chatRoomId: String, val recieverPhone: String): java.io.Serializable{
}