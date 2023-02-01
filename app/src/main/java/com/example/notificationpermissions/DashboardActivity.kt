package com.example.notificationpermissions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView


class DashboardActivity : AppCompatActivity() {
    //assigned in each fragment to know the current fragment and to manage the appBar accordingly
    lateinit var currentFragment: Fragment
    lateinit var toolbar: Toolbar
    var destination: NavDestination? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        //bottom navigation bar
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)

        //fragment view that would change every time
        val navController = findNavController(R.id.nav_fragment)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.chatFragment, R.id.addPostFragment, R.id.historyFragment, R.id.profileFragment
            )
        )

        //changing label to user name in profile
        destination = findNavController(R.id.nav_fragment).graph.findNode(R.id.profileFragment)!!
        destination!!.label = "${App.sharedPrefs.userName}"

        setupActionBarWithNavController(navController, appBarConfiguration)
        //mapping ig nav view with fragments
        bottomNavigationView.setupWithNavController(navController)

        //gives back arrow
        toolbar.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.appbar_nav, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
       println(currentFragment)
        val item = menu.findItem(R.id.nav_search)
        val item1=menu.findItem(R.id.nav_notifications)
        val item2= menu.findItem(R.id.nav_logout)

        if (currentFragment::class.java == AddPostFragment::class.java) {
            item.isVisible = false
            item1.isVisible=false
            item2.isVisible=false
        }
        if (currentFragment::class.java == ProfileFragment::class.java) {
            item.isVisible = false
            item1.isVisible=false
            item2.isVisible=true

            supportActionBar!!.show()
        }

        if (currentFragment::class.java != AddPostFragment::class.java && currentFragment::class.java != ProfileFragment::class.java) {
            item.isVisible = true
            item1.isVisible=true
            item2.isVisible=false

            supportActionBar!!.show()
        }

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.getItemId() == R.id.nav_search){
            Toast.makeText(this, "Click Search Icon.", Toast.LENGTH_SHORT).show();
        }
        else if (item.getItemId() == R.id.nav_notifications){
            Toast.makeText(this, "Clicked Notifications Icon..", Toast.LENGTH_SHORT).show();
            ShowNotification()
        }
        else if(item.itemId==R.id.nav_logout){
            UserDataService.logout()
            val intent= Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item);
    }

    private fun ShowNotification(/*messageBody: String*/) {
        val intent = Intent(this, AlertDetails::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder =
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_baseline_circle_notifications_24)
                .setContentTitle("Test Message")
                /* .setContentText(messageBody)*/

                .setContentText("This is test. First Notification")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .build()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(1 /* ID of notification */, notificationBuilder)
    }
}