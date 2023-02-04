package com.example.notificationpermissions.Services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.example.notificationpermissions.Adapters.ChatRoomAdapter
import com.example.notificationpermissions.Models.ChatRoom
import com.example.notificationpermissions.Models.Message
import com.example.notificationpermissions.Models.Post
import com.example.notificationpermissions.Utilities.*
import org.json.JSONException
import org.json.JSONObject

object MessageService {
    val userChatRooms = ArrayList<ChatRoom>()
    val messages = ArrayList<Message>()
    val map = mutableMapOf<String, String>()

    fun getChatRooms(complete: (Boolean) -> Unit) {
        val chatRoomRequest = object :
            JsonArrayRequest(Method.GET, "$URL_GET_USER_CHAT_ROOMS${App.sharedPrefs.userID}", null, Response.Listener { response ->
                try {
                    println(response)
                    val id= response.toString().split("[\"","\",\"",",","\"]")
                    println(id)

                    val arrayList= ArrayList(id)

                    arrayList.removeAt(0)
                    arrayList.removeAt(arrayList.lastIndex)

                    println(arrayList)

                    for (i in 0 until arrayList.size step 2) {
                        map[arrayList[i]] = arrayList[i + 1]
                    }
                    println(map)
                    println(map["Subriti+Shamba"])
                      /* for (x in 0 until response.length()) {
                        val chatRooms = response.getJSONObject(x)
                        println(chatRooms)
                        val chats= chatRooms.getString("[]")
                        println(chats)
                        val id= chats.split(",")
                        println(id)*/
                        //val chats= chatRooms.getString("chat_room_id")
                        //this.userChatRooms.add(chats.toString())}
                    complete(true)
                } catch (e: JSONException) {
                    Log.d("JSON", "EXC:" + e.localizedMessage)
                    complete(false)
                }
            }, Response.ErrorListener { error ->
                Log.d("ERROR", "Could not retrieve chatRooms")
                complete(false)
            }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.sharedPrefs.authToken}")
                return headers
            }
        }
        App.sharedPrefs.requestQueue.add(chatRoomRequest)
    }

    fun findUser(chatRoomId: String, userId:String, complete: (Boolean) -> Unit) {
        val findRequest = object : JsonObjectRequest(
            Method.GET,
            "$URL_FIND_USER_BY_ID$userId",
            null,
            Response.Listener {
                    response ->
                println("Find User Response " + response)
                try {
                    val userId = response.getString("user_id")
                    val userName = response.getString("user_name")
                    val email = response.getString("email")
                    val birthDate = response.getString("birth_date")
                    val location = response.getString("location")
                    val phoneNumber = response.getString("phone_number")
                    val profilePicture = response.getString("profile_picture")
                    val fcmToken = response.getString("fcm_token")

                    val newChatRoom = ChatRoom(
                        chatRoomId,
                        userId,
                        userName,
                        profilePicture,
                        fcmToken
                    )
                    userChatRooms.add(newChatRoom)
                    println(userChatRooms)
                    complete(true)
                } catch (e: JSONException) {
                    Log.d("JSON", "EXC: " + e.localizedMessage)
                    complete(false)
                }
            },
            Response.ErrorListener {
                //this is where we deal with our error
                    error ->
                Log.d("ERROR", "Could not find user: $error")
                complete(false)
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer ${App.sharedPrefs.authToken}"
                return headers
            }
        }
        App.sharedPrefs.requestQueue.add(findRequest)
    }


    fun getMessages(chatRoomId: String, complete: (Boolean) -> Unit) {
        //channel to uniquely define the chatroom, userId to get the messages as a sender
        val url = "${URL_GET_USER_CHAT_ROOM_MESSAGES}$chatRoomId"
        val messagesRequest = object : JsonArrayRequest(
            Method.GET,
            url,
            null,
            Response.Listener { response ->
                clearMessages()
                try {
                    for (x in 0 until response.length()) {
                        val message = response.getJSONObject(x)
                        val id= message.getString("message_id")
                        val messsageBody= message.getString("message_body")
                        val timeStamp= message.getString("time_stamp")

                        val senderUserId= message.getString("sender_user_id")
                        val sender= JSONObject(senderUserId)
                        val senderId= sender.getString("user_id")

                        val recieverUserId= message.getString("reciever_user_id")
                        val chatRoomId= message.getString("chat_room_id")

                        val reciever= JSONObject(recieverUserId)
                        val recieverId= reciever.getString("user_id")
                        val recieverName= reciever.getString("user_name")
                        val recieverProfile= reciever.getString("profile_picture")
                        val recieverFCMtoken= reciever.getString("fcm_token")

                        val newMessage = Message(id,messsageBody,timeStamp,senderId,recieverId,recieverName,recieverProfile,recieverFCMtoken,chatRoomId)
                        this.messages.add(newMessage)
                    }
                    complete(true)

                } catch (e: JSONException) {
                    Log.d("JSON", "EXC:" + e.localizedMessage)
                    complete(false)
                }
            },
            Response.ErrorListener { error ->
                Log.d("ERROR", "Could not retrieve messages")
                complete(false)
            }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers.put("Authorization", "Bearer ${App.sharedPrefs.authToken}")
                return headers
            }
        }
        App.sharedPrefs.requestQueue.add(messagesRequest)
    }

    fun clearMessages(){
        messages.clear()
    }

    fun clearChatRooms(){
        userChatRooms.clear()
    }
}