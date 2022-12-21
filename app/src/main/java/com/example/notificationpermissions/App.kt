package com.example.notificationpermissions

import android.app.Application
import com.example.notificationpermissions.Utilities.SharedPrefs

class App: Application() {

    companion object{
        lateinit var sharedPrefs: SharedPrefs
    }
    override fun onCreate() {
        sharedPrefs= SharedPrefs(applicationContext)
        super.onCreate()
    }
}