package com.example.notificationpermissions.Services

import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.example.notificationpermissions.Models.Notification
import com.example.notificationpermissions.Utilities.App
import com.example.notificationpermissions.Utilities.URL_CREATE_NOTIFICATIONS
import com.example.notificationpermissions.Utilities.URL_GET_USER_NOTIFICATIONS
import org.json.JSONException
import org.json.JSONObject

object NotificationService {
    val notifications = ArrayList<Notification>()

    fun getUserNotifications(userId: String, complete: (Boolean) -> Unit) {
        notifications.clear()
        val getNotificationRequest = object :
            JsonArrayRequest(Method.GET, "$URL_GET_USER_NOTIFICATIONS$userId", null, Response.Listener {
                //this is where we parse the json object
                    response ->
                try {
                    for (x in 0 until response.length()) {
                        println("Notification response: $response")
                        val notification = response.getJSONObject(x)
                        val notificationId = notification.getString("notification_id")
                        val title = notification.getString("title")
                        val message = notification.getString("message")
                        val data = notification.getString("data")

                        val senderId = notification.getString("senderId")
/*
                        val json= JSONObject(data)
                        val postId= json.getString("post_id")*/

                        val token = notification.getString("recipientToken")
                        val recieverId = notification.getString("recieverId")

                        val newNotification = Notification(
                           notificationId, title, message, data, senderId,  token, recieverId)
                        notifications.add(newNotification)
                    }
                    println("Notification Array Size ${notifications.size}")
                    complete(true)
                } catch (e: JSONException) {
                    Log.d("JSON", "EXC: " + e.localizedMessage)
                    complete(false)
                }
            }, Response.ErrorListener {
                //this is where we deal with our error
                    error ->
                Log.d("ERROR", "Could not retrieve notifications: $error")
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
        getNotificationRequest.retryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(getNotificationRequest)
    }

    fun addNotification(
        title: String,
        message: String,
        postData: String,
        senderId: String,
        recipientToken: String,
        recieverId: String,
        complete: (Boolean) -> Unit
    ) {
        val jsonBody = JSONObject()
        jsonBody.put("title",title)
        jsonBody.put("message",message)

        //bc it takes object of data
        val data = JSONObject()
        data.put("post_id", postData)
        jsonBody.put("data", data)

        //bc it takes object of senderUser
        val senderUser = JSONObject()
        senderUser.put("user_id", senderId)
        jsonBody.put("senderId", senderUser)

        jsonBody.put("recipientToken",recipientToken)

        //bc it takes object of User
        val user = JSONObject()
        user.put("user_id", recieverId)
        jsonBody.put("recieverId", user)

        val requestBody = jsonBody.toString()
        print(requestBody)

        val createRequest = object :
            JsonObjectRequest(Method.POST, URL_CREATE_NOTIFICATIONS, null, Response.Listener { response ->
                println("Create Notification Response $response")
                complete(true)

            }, Response.ErrorListener { error ->
                Log.d("ERROR", "Could not add Notification: $error")
                complete(false)
            }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer ${App.sharedPrefs.authToken}"
                return headers
            }

        }
        createRequest.retryPolicy = DefaultRetryPolicy(
            10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(createRequest)
    }
}