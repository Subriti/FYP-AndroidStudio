package com.example.notificationpermissions.Services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.example.notificationpermissions.Models.Clothes
import com.example.notificationpermissions.Models.User
import com.example.notificationpermissions.Utilities.*
import org.json.JSONException
import org.json.JSONObject


object AuthService {
    var isFound = false
    var recipientToken = ""
    var newPassword=""
    var resetPhone=""

    var userId = ""
    var userName = ""
    var email = ""
    var birthDate = ""
    var location = ""
    var phoneNumber = ""
    var profilePicture = ""
    var fcmToken = ""

    val userList = ArrayList<User>()

    fun registerUser(
        name: String,
        email: String,
        password: String,
        birth_date: String,
        phone_number: String,
        location: String,
        signup_date: String,
        profile_picture: String,
        hideEmail:Boolean,
        hidePhone: Boolean,
        isAdmin:Boolean,
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
        jsonBody.put("hide_email", hideEmail)
        jsonBody.put("hide_phone", hidePhone)
        jsonBody.put("is_admin", isAdmin)

        val requestBody = jsonBody.toString()
        val createRequest = object :
            JsonObjectRequest(Method.POST, URL_CREATE_USER, null, Response.Listener { response ->
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
        val requestBody = jsonBody.toString()
        //bc volley takes byte array so string is easier to be later changed into bytearray
        val loginRequest = object : JsonObjectRequest(Method.POST, URL_LOGIN, null, Response.Listener {
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
                    App.sharedPrefs.isAdmin= response.getString("is_admin")
                    App.sharedPrefs.hideUserEmail = response.getString("hide_email")
                    App.sharedPrefs.hideUserPhone = response.getString("hide_phone")
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

    fun findUserByID(userID: String, complete: (Boolean) -> Unit) {
        val findRequest = object : JsonObjectRequest(
            Method.GET,
            "$URL_FIND_USER_BY_ID$userID",
            null,
            Response.Listener { response ->
                println("Find User Response " + response)
                try {
                    userId = response.getString("user_id")
                    userName = response.getString("user_name")
                    email = response.getString("email")
                    birthDate = response.getString("birth_date")
                    location = response.getString("location")
                    phoneNumber = response.getString("phone_number")
                    profilePicture = response.getString("profile_picture")
                    fcmToken = response.getString("fcm_token")

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

    fun findUserByName(name: String, complete: (Boolean) -> Unit) {
        val jsonBody = JSONObject()
        jsonBody.put("user_name", name)

        val requestBody = jsonBody.toString()
        print(requestBody)

        val findRequest = object : JsonObjectRequest(
            Method.POST,
            "$URL_FIND_USER_BY_NAME",
            null,
            Response.Listener { response ->
                println("Find User Response " + response)
                try {
                    userId = response.getString("user_id")
                    userName = response.getString("user_name")
                    email = response.getString("email")
                    birthDate = response.getString("birth_date")
                    location = response.getString("location")
                    phoneNumber = response.getString("phone_number")
                    profilePicture = response.getString("profile_picture")
                    fcmToken = response.getString("fcm_token")

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
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }
        findRequest.retryPolicy = DefaultRetryPolicy(
            10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

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

    fun resetPassword(email: String, complete: (Boolean) -> Unit) {
        val resetPasswordRequest = object : JsonObjectRequest(Method.PUT,
            "$URL_FORGOT_PASSWORD$email",
            null,
            Response.Listener { response ->
                println("Reset Password Response $response")
                try {
                    newPassword = response.getString("Success Message")
                    resetPhone= response.getString("phone_number")
                    complete(true)
                } catch (e: JSONException) {
                    Log.d("JSON", "EXC: " + e.localizedMessage)
                    complete(false)
                }
            },
            Response.ErrorListener { error ->
                Log.d("ERROR", "Could not reset password: $error")
                complete(false)
            }) {}
        resetPasswordRequest.retryPolicy = DefaultRetryPolicy(
            10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(resetPasswordRequest)
    }

    fun updateUser(
        name: String,
        email: String,
        birth_date: String,
        phone_number: String,
        location: String,
        profile_picture: String,
        hide_email: Boolean,
        hide_phone: Boolean,
        complete: (Boolean) -> Unit
    ) {
        val jsonBody = JSONObject()
        jsonBody.put("user_name", name)
        jsonBody.put("email", email)
        jsonBody.put("birth_date", birth_date)
        jsonBody.put("phone_number", phone_number)
        jsonBody.put("location", location)
        jsonBody.put("profile_picture", profile_picture)
        jsonBody.put("hide_email", hide_email)
        jsonBody.put("hide_phone", hide_phone)

        val requestBody = jsonBody.toString()
        println(requestBody)

        val updateRequest = object : JsonObjectRequest(Method.PUT,
            "$URL_UPDATE_USER${App.sharedPrefs.userID}",
            null,
            Response.Listener { response ->
                println("Update User Response $response")
                val token = response.getString("token")
                val hideEmail = response.getString("hide_email")
                val hidePhone = response.getString("hide_phone")
                if (token != "token") {
                    App.sharedPrefs.authToken = token
                }
                App.sharedPrefs.hideUserEmail= hideEmail
                App.sharedPrefs.hideUserPhone= hidePhone
                println("Updated hide email ${App.sharedPrefs.hideUserEmail}")
                println("Updated hide phone ${App.sharedPrefs.hideUserPhone}")

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

    fun getAllUsers(complete: (Boolean) -> Unit) {
        userList.clear()
        val getUserRequest =
            object : JsonArrayRequest(Method.GET, "$URL_GET_ALL_USERS", null, Response.Listener {
                //this is where we parse the json object
                    response ->
                println(response)
                try {
                    for (x in 0 until response.length()) {
                        val user = response.getJSONObject(x)
                        val userId = user.getString("user_id")
                        val userName = user.getString("user_name")
                        val userProfile = user.getString("profile_picture")
                        val email= user.getString("email")
                        val phoneNumber= user.getString("phone_number")
                        val location= user.getString("location")
                        val fcmToken= user.getString("fcm_token")
                        val hideEmail= user.getString("hide_email")
                        val hidePhone= user.getString("hide_phone")

                        val newUser = User(
                            userId,
                            userName,
                            userProfile,
                            email,
                            phoneNumber,
                            location,
                            fcmToken,
                            hideEmail,
                            hidePhone
                        )
                        userList.add(newUser)
                    }
                    complete(true)
                } catch (e: JSONException) {
                    Log.d("JSON", "EXC: " + e.localizedMessage)
                    complete(false)
                }
            }, Response.ErrorListener {
                //this is where we deal with our error
                    error ->
                Log.d("ERROR", "Could not retrieve users: $error")
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
        App.sharedPrefs.requestQueue.add(getUserRequest)
    }
}
