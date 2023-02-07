package com.example.notificationpermissions.Services

import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.example.notificationpermissions.Models.ChatRoom
import com.example.notificationpermissions.Models.Message
import com.example.notificationpermissions.Utilities.App
import com.example.notificationpermissions.Utilities.URL_FIND_USER_BY_ID
import com.example.notificationpermissions.Utilities.URL_GET_USER_CHAT_ROOMS
import com.example.notificationpermissions.Utilities.URL_GET_USER_CHAT_ROOM_MESSAGES
import org.json.JSONException
import org.json.JSONObject

object MessageService {
    val userChatRooms = ArrayList<ChatRoom>()
    val messages = ArrayList<Message>()
    val map = mutableMapOf<String, String>()

    fun getChatRooms(complete: (Boolean) -> Unit) {
        userChatRooms.clear()
        val chatRoomRequest = object :
            JsonArrayRequest(Method.GET, "$URL_GET_USER_CHAT_ROOMS${App.sharedPrefs.userID}/?userName=${App.sharedPrefs.userName}", null, Response.Listener { response ->
                try {
                    println(response)
                    val id= response.toString().split("[\"","\",\"",",","\"]")
                    println(id)

                    val arrayList= ArrayList(id)

                    if(arrayList.size>1) {
                        arrayList.removeAt(0)
                        arrayList.removeAt(arrayList.lastIndex)

                        println(arrayList)

                        for (i in 0 until arrayList.size step 2) {
                            map[arrayList[i]] = arrayList[i + 1]
                        }
                    }
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
        val url = "${URL_GET_USER_CHAT_ROOM_MESSAGES}/$chatRoomId"
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
                        val timeStamp= message.getString("timestamp")

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

    fun getChatRoomMessages(chatRoomId: String, complete: (Boolean) -> Unit) {
        val jsonBody = JSONObject()
        jsonBody.put("chat_room_id", chatRoomId)

        val requestBody = jsonBody.toString()
        println(requestBody)

        val getMessageRequest = object : JsonArrayRequest(Method.POST,
            "$URL_GET_USER_CHAT_ROOM_MESSAGES",
            null,
            Response.Listener { response ->
                clearMessages()
                try {
                    for (x in 0 until response.length()) {
                        val message = response.getJSONObject(x)
                        val id= message.getString("message_id")
                        val messsageBody= message.getString("message_body")
                        val timeStamp= message.getString("timestamp")
                        val senderUserId= message.getString("sender_user_id")
                        val sender= JSONObject(senderUserId)
                        val senderId= sender.getString("user_id")
                        val senderName= sender.getString("user_name")
                        val senderProfile= sender.getString("profile_picture")
                        val senderFCMtoken= sender.getString("fcm_token")

                        val recieverUserId= message.getString("reciever_user_id")
                        val chatRoomId= message.getString("chat_room_id")

                        val reciever= JSONObject(recieverUserId)
                        val recieverId= reciever.getString("user_id")
                        val recieverName= reciever.getString("user_name")
                        val recieverProfile= reciever.getString("profile_picture")
                        val recieverFCMtoken= reciever.getString("fcm_token")

                        //val newMessage = Message(id,messsageBody,timeStamp,senderId,recieverId,recieverName,recieverProfile,recieverFCMtoken,chatRoomId)
                        val newMessage = Message(id,messsageBody,timeStamp,recieverId,senderId,senderName,senderProfile,senderFCMtoken,chatRoomId)

                        this.messages.add(newMessage)
                    }
                    complete(true)

                } catch (e: JSONException) {
                    Log.d("JSON", "EXC:" + e.localizedMessage)
                    complete(false)
                }
            },
            Response.ErrorListener {
                //this is where we deal with our error
                    error ->
                Log.d("ERROR", "Could not get fcm_token: $error")
                complete(false)
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer ${App.sharedPrefs.authToken}"
                return headers
            }
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }
            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }
        getMessageRequest.retryPolicy = DefaultRetryPolicy(
            10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(getMessageRequest)
    }

    fun clearMessages(){
        messages.clear()
    }

    fun clearChatRooms(){
        userChatRooms.clear()
    }
}