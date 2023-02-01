package com.example.notificationpermissions.Utilities

import android.app.Application

class App: Application() {

    companion object{
        lateinit var sharedPrefs: SharedPrefs
    }
    override fun onCreate() {
        sharedPrefs = SharedPrefs(applicationContext)
        super.onCreate()
    }
}