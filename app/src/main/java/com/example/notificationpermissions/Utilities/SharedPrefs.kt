package com.example.notificationpermissions.Utilities

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.android.volley.toolbox.Volley


class SharedPrefs(context: Context) {

    val PREFS_FILENAME = "prefs"
    //filename, and mode=0 is content private
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)

    val IS_LOGGED_IN = "isLoggedIn"
    val AUTH_TOKEN= "authToken"
    val USER_EMAIL= "userEmail"

    val USER_ID= "userID"

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

    val requestQueue= Volley.newRequestQueue(context)
}