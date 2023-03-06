package com.example.notificationpermissions.Utilities

import android.content.Context
import android.content.SharedPreferences
import com.android.volley.toolbox.Volley

class SharedPrefs(context: Context) {

    val PREFS_FILENAME = "prefs"
    //filename, and mode=0 is content private
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)

    val IS_LOGGED_IN = "isLoggedIn"
    val AUTH_TOKEN= "authToken"
    val USER_EMAIL= "userEmail"
    val USER_ID= "userID"
    val USER_NAME= "userName"
    val PROFILE_PICTURE= "profilePicture"
    val LOCATION= "location"
    val PHONE_NUMBER= "phoneNumber"
    val DATE_OF_BIRTH= "dateOfBirth"
    val RATING= 1.0
    val CLOTH_DONATED= 0

    val FCM_TOKEN= "fcmToken"
    var token: String
        get()= prefs.getString(FCM_TOKEN,"")!!
        set(value)= prefs.edit().putString(FCM_TOKEN, value).apply()

    var isLoggedIn: Boolean
        get() = prefs.getBoolean(IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(IS_LOGGED_IN, value).apply()

    var authToken: String
        get() = prefs.getString(AUTH_TOKEN, "")!!
        set(value) = prefs.edit().putString(AUTH_TOKEN, value).apply()

    var userEmail: String
        get() = prefs.getString(USER_EMAIL, "")!!
        set(value) = prefs.edit().putString(USER_EMAIL, value).apply()

    var userID: String
        get() = prefs.getString(USER_ID, "")!!
        set(value) = prefs.edit().putString(USER_ID, value).apply()

    var userName: String
        get() = prefs.getString(USER_NAME, "")!!
        set(value) = prefs.edit().putString(USER_NAME, value).apply()

    var profilePicture: String
        get() = prefs.getString(PROFILE_PICTURE, "")!!
        set(value) = prefs.edit().putString(PROFILE_PICTURE, value).apply()

    var location: String
        get() = prefs.getString(LOCATION, "")!!
        set(value) = prefs.edit().putString(LOCATION, value).apply()

    var phoneNumber: String
        get() = prefs.getString(PHONE_NUMBER, "")!!
        set(value) = prefs.edit().putString(PHONE_NUMBER, value).apply()

    var dateOfBirth: String
        get() = prefs.getString(DATE_OF_BIRTH, "")!!
        set(value) = prefs.edit().putString(DATE_OF_BIRTH, value).apply()

    var rating: Float
        get() = prefs.getFloat(RATING.toString(), 0.0F)!!
        set(value) = prefs.edit().putFloat(RATING.toString(), value).apply()

    var clothDonated: Int
        get() = prefs.getInt(CLOTH_DONATED.toString(), 0)!!
        set(value) = prefs.edit().putInt(CLOTH_DONATED.toString(), value).apply()

    val requestQueue= Volley.newRequestQueue(context)
}