package com.example.notificationpermissions

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView


class DashboardActivity : AppCompatActivity() {
    //assigned in each fragment to know the current fragment and to manage the appBar accordingly
    lateinit var currentFragment: Fragment
    lateinit var toolbar: androidx.appcompat.widget.Toolbar
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
        /* val currentFragment= supportFragmentManager.fragments.last()?.childFragmentManager?.fragments?.get(0)


             println(AddPostFragment::class.java)*/
        /*if (currentFragment == AddPostFragment::class || shareVisible == SocFragment::class.java.toString() || shareVisible == DevFragment::class.java.toString()) {
*/      println(currentFragment)
        if (currentFragment::class.java == AddPostFragment::class.java) {
            val item = menu.findItem(R.id.nav_search)
            val item1=menu.findItem(R.id.nav_notifications)
            item.isVisible = false
            item1.isVisible=false

            /* //removing existing appBar and adding new
             val frag= AddPostFragment()
             val toolbar: Toolbar = frag.requireView().findViewById(R.id.addPost_toolbar)
                 supportActionBar!!.hide()
                 setSupportActionBar(toolbar)*/
        }
        if (currentFragment::class.java != AddPostFragment::class.java) {
            val item = menu.findItem(R.id.nav_search)
            val item1=menu.findItem(R.id.nav_notifications)
            item.isVisible = true
            item1.isVisible=true

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
        }
        return super.onOptionsItemSelected(item);
    }


}