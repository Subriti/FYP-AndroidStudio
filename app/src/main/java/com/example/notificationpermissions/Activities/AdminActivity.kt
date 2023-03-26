package com.example.notificationpermissions.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.notificationpermissions.Fragments.ReportFragment
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.UserDataService
import com.example.notificationpermissions.Utilities.App
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminActivity : AppCompatActivity() {
    lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val reportFragment= ReportFragment()
/*
        val viewReports= findViewById<Button>(R.id.viewReports)
        viewReports.setOnClickListener {*/
        supportFragmentManager.beginTransaction().apply {
                replace(R.id.replaceLayout, reportFragment)
                addToBackStack(null)
                commit()
        }
    }

    override fun onBackPressed() {
        // Check if the current fragment is the report fragment
        val currentFragment = supportFragmentManager.findFragmentById(R.id.replaceLayout)
        if (currentFragment is ReportFragment) {
            // Exit the app if the back button is pressed in the report fragment
            finishAffinity()
        } else {
            // Otherwise, let the default behavior take place
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.admin_appbar, menu)
        val item = menu.findItem(R.id.nav_logout)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.nav_logout) {
            UserDataService.logout()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
    }
    return super.onOptionsItemSelected(item)
}
}