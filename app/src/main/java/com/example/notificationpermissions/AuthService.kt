package com.example.notificationpermissions

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.example.notificationpermissions.Utilities.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*


object AuthService {

    var isFound= false

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
        jsonBody.put("password",password)
        jsonBody.put("birth_date", birth_date)
        jsonBody.put("phone_number", phone_number)
        jsonBody.put("location", location)
        jsonBody.put("signup_date", signup_date)
        jsonBody.put("profile_picture", profile_picture)

        println(jsonBody)

        val requestBody = jsonBody.toString()
        print(requestBody)

        val createRequest = object :
            JsonObjectRequest(Method.POST, URL_CREATE_USER, null, Response.Listener { response ->
                println("Create User Response $response")
                complete(true)
            }, Response.ErrorListener { error ->
                Log.d("ERROR", "Could not register User: $error")
                complete(false)
            })

            {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }
        createRequest.retryPolicy = DefaultRetryPolicy(
            10000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        App.sharedPrefs.requestQueue.add(createRequest)
    }


    fun loginUser(email: String, password: String, complete: (Boolean) -> Unit) {
        val jsonBody = JSONObject()
        jsonBody.put("email", email)
        jsonBody.put("password", password)
        val requestBody = jsonBody.toString()       //bc volley takes byte array so string is easier to be later changed into bytearray
        val loginRequest = object : JsonObjectRequest(Method.POST, URL_LOGIN, null, Response.Listener {
                    //this is where we parse the json object
                    response ->
                try {
                    App.sharedPrefs.userEmail= email
                    App.sharedPrefs.userID = response.getString("user_id")
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
        App.sharedPrefs.requestQueue.add(loginRequest)
    }

    fun findUser(context: Context, complete: (Boolean) -> Unit) {
        val findRequest =
            object : JsonObjectRequest(Method.GET, "$URL_FIND_USER${App.sharedPrefs.userEmail}", null, Response.Listener {
                //this is where we parse the json object

                //response -> nalekhe vane make it it.getString("name")
                    response ->
                println("Find User Response " +response)
                try {
                    println(response.getString("user_id"))
                    println(response.getString("email"))

                    UserDataService.userId= response.getString("user_id")
                    UserDataService.userName = response.getString("user_name")
                    UserDataService.email = response.getString("email")
                    UserDataService.birthDate = response.getString("birth_date")
                    UserDataService.location = response.getString("location")
                    UserDataService.phoneNumber = response.getString("phone_number")
                    UserDataService.profilePicture = response.getString("profile_picture")
                    UserDataService.signupDate = response.getString("signup_date")

                    val userDataChange= Intent(BROADCAST_USER_DATA_CHANGE)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(userDataChange)

                    isFound = true
                    println("is found: $isFound")
                    complete(true)

                } catch (e: JSONException) {
                    Log.d("JSON", "EXC: " + e.localizedMessage)
                    complete(false)
                }

            }, Response.ErrorListener {
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
}
