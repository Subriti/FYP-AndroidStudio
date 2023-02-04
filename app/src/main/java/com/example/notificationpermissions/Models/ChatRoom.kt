package com.example.notificationpermissions.Models

class ChatRoom (val chatRoomId: String, val receiverUserId:String, val recieverUserName: String, val recieverProfilePicture: String, val recieverFCMtoken: String): java.io.Serializable
{
  /*  override fun toString(): String {
        return "$recieverUserName"
    }*/
}