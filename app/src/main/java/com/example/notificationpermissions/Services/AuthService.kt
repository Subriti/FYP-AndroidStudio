package com.example.notificationpermissions.Services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.example.notificationpermissions.Utilities.App
import com.example.notificationpermissions.Utilities.*
import org.json.JSONException
import org.json.JSONObject


object AuthService {

    var isFound = false
    var recipientToken=""

    fun registerUser(
        name: String,
        email: String,
        password: String,
        birth_date: String,
        phone_number: String,
        location: String,
        signup_date: String,
        profile_picture: String,
        complete: (Boolean) -> Unit
    ) {
        val jsonBody = JSONObject()
        jsonBody.put("user_name", name)
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        jsonBody.put("birth_date", birth_date)
        jsonBody.put("phone_number", phone_number)
        jsonBody.put("location", location)
        jsonBody.put("signup_date", signup_date)
        jsonBody.put("profile_picture", profile_picture)

        val requestBody = jsonBody.toString()
        print(requestBody)

        val createRequest = object :
            JsonObjectRequest(Method.POST, URL_CREATE_USER, null, Response.Listener { response ->
                println("Create User Response $response")
                complete(true)
            }, Response.ErrorListener { error ->
                Log.d("ERROR", "Could not register User: $error")
                complete(false)
            }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }
        createRequest.retryPolicy = DefaultRetryPolicy(
            10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(createRequest)
    }


    fun loginUser(email: String, password: String, complete: (Boolean) -> Unit) {
        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        val requestBody =
            jsonBody.toString()       //bc volley takes byte array so string is easier to be later changed into bytearray
        val loginRequest =
            object : JsonObjectRequest(Method.POST, URL_LOGIN, null, Response.Listener {
                //this is where we parse the json object
                    response ->
                try {
                    App.sharedPrefs.userEmail = email
                    App.sharedPrefs.userID = response.getString("user_id")
                    App.sharedPrefs.userName = response.getString("user_name")
                    App.sharedPrefs.profilePicture = response.getString("profile_picture")
                    App.sharedPrefs.location = response.getString("location")
                    App.sharedPrefs.phoneNumber = response.getString("phone_number")
                    App.sharedPrefs.dateOfBirth = response.getString("birth_date")
                    App.sharedPrefs.authToken = response.getString("token")
                    App.sharedPrefs.isLoggedIn = true
                    complete(true)
                } catch (e: JSONException) {
                    Log.d("JSON", "EXC: " + e.localizedMessage)
                    complete(false)
                }
            }, Response.ErrorListener {
                //this is where we deal with our error
                    error ->
                Log.d("ERROR", "Could not login user: $error")
                complete(false)
            }) {
                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }

                override fun getBody(): ByteArray {
                    return requestBody.toByteArray()
                }
            }
        loginRequest.retryPolicy = DefaultRetryPolicy(
            30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(loginRequest)
    }

    fun findUser(context: Context, complete: (Boolean) -> Unit) {
        val findRequest = object : JsonObjectRequest(Method.GET,
            "$URL_FIND_USER${App.sharedPrefs.userEmail}",
            null,
            Response.Listener {
                //this is where we parse the json object
                //response -> nalekhe vane make it it.getString("name")
                    response ->
                println("Find User Response " + response)
                try {
                    println(response.getString("user_id"))
                    println(response.getString("email"))

                    UserDataService.userId = response.getString("user_id")
                    UserDataService.userName = response.getString("user_name")
                    UserDataService.email = response.getString("email")
                    UserDataService.birthDate = response.getString("birth_date")
                    UserDataService.location = response.getString("location")
                    UserDataService.phoneNumber = response.getString("phone_number")
                    UserDataService.profilePicture = response.getString("profile_picture")
                    UserDataService.signupDate = response.getString("signup_date")

                    val userDataChange = Intent(BROADCAST_USER_DATA_CHANGE)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(userDataChange)

                    isFound = true
                    println("is found: $isFound")
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

    fun getFCMToken(postOwner: String, complete: (Boolean) -> Unit) {
        val jsonBody = JSONObject()
        jsonBody.put("user_name", postOwner)

        val requestBody = jsonBody.toString()
        print(requestBody)

        val getFCMTokenRequest = object : JsonObjectRequest(Method.POST,
            "$URL_GET_FCM_TOKEN",
            null,
            Response.Listener {
                //this is where we parse the json object
                    response ->
                println("GET FCM TOKEN Response " + response)
                try {
                    recipientToken = response.getString("fcm_token")
                    println("Recipient Token is: $recipientToken")
                    complete(true)
                } catch (e: JSONException) {
                    Log.d("JSON", "EXC: " + e.localizedMessage)
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
        getFCMTokenRequest.retryPolicy = DefaultRetryPolicy(
                10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(getFCMTokenRequest)
    }

    fun updateFCMToken(
        fcmToken: String,
        complete: (Boolean) -> Unit
    ) {
        val jsonBody = JSONObject()
        jsonBody.put("fcm_token", fcmToken)

        val requestBody = jsonBody.toString()
        print(requestBody)

        val updateRequest = object : JsonObjectRequest(Method.PUT,
            "$URL_UPDATE_FCM_TOKEN${App.sharedPrefs.userID}",
            null,
            Response.Listener { response ->
                println("Update FCM Token Response $response")
                complete(true)
            },
            Response.ErrorListener { error ->
                Log.d("ERROR", "Could not update FCM Token: $error")
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
        updateRequest.retryPolicy = DefaultRetryPolicy(
            10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(updateRequest)
    }

    fun changePassword(
        oldPassword: String, newPassword: String, complete: (Boolean) -> Unit
    ) {
        val jsonBody = JSONObject()
        jsonBody.put("old_password", oldPassword)
        jsonBody.put("new_password", newPassword)

        val requestBody = jsonBody.toString()
        print(requestBody)

        val changePasswordRequest = object : JsonObjectRequest(Method.PUT,
            "$URL_RESET_PASSWORD${App.sharedPrefs.userID}",
            null,
            Response.Listener { response ->
                println("Change Password Response $response")
                complete(true)
            },
            Response.ErrorListener { error ->
                Log.d("ERROR", "Could not change password: $error")
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
        changePasswordRequest.retryPolicy = DefaultRetryPolicy(
            10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(changePasswordRequest)
    }

    fun updateUser(
        name: String,
        email: String,
        birth_date: String,
        phone_number: String,
        location: String,
        profile_picture: String,
        complete: (Boolean) -> Unit
    ) {
        val jsonBody = JSONObject()
        jsonBody.put("user_name", name)
        jsonBody.put("email", email)
        jsonBody.put("birth_date", birth_date)
        jsonBody.put("phone_number", phone_number)
        jsonBody.put("location", location)
        jsonBody.put("profile_picture", profile_picture)

        val requestBody = jsonBody.toString()
        print(requestBody)

        val updateRequest = object : JsonObjectRequest(Method.PUT,
            "$URL_UPDATE_USER${App.sharedPrefs.userID}",
            null,
            Response.Listener { response ->
                println("Update User Response $response")
                val token=response.getString("token")
                if (token!="token") {
                    App.sharedPrefs.authToken=token
                }
                complete(true)
            },
            Response.ErrorListener { error ->
                Log.d("ERROR", "Could not update User: $error")
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
        updateRequest.retryPolicy = DefaultRetryPolicy(
            10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(updateRequest)
    }

}
