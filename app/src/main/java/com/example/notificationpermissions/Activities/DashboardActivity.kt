package com.example.notificationpermissions.Activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.navigation.NavDestination
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notificationpermissions.Adapters.UserAdapter
import com.example.notificationpermissions.Fragments.HomeFragment
import com.example.notificationpermissions.Models.User
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.AuthService
import com.example.notificationpermissions.Services.BlockService
import com.example.notificationpermissions.Services.PostService
import com.example.notificationpermissions.Services.UserDataService
import com.example.notificationpermissions.Utilities.App
import com.example.notificationpermissions.Utilities.EXTRA_POST
import com.example.notificationpermissions.Utilities.EXTRA_USER
import com.example.notificationpermissions.Utilities.OnCardVisibilityChangeListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONObject


class DashboardActivity : AppCompatActivity() {
    //assigned in each fragment to know the current fragment and to manage the appBar accordingly
    //var currentFragment: Fragment= HomeFragment()

    var currentFragment: String = "Donation Feed"

    lateinit var toolbar: Toolbar
    var destination: NavDestination? = null

    var adapter: UserAdapter? = null

    lateinit var item: MenuItem
    lateinit var item1: MenuItem
    lateinit var item2: MenuItem
    lateinit var item3: MenuItem

    var count=0
    var listener: OnCardVisibilityChangeListener? = null

    var isBlocked: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        //bottom navigation bar
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)

        //fragment view that would change every time
        val navController = findNavController(R.id.nav_fragment)

        //for finding the current fragment view

        // Set a listener for when the selected destination changes
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Get the fragment class name from the destination's label
            currentFragment = destination.label.toString()
            Log.d("Fragment", "Selected fragment: $currentFragment")
            println("Selected fragment: $currentFragment")
        }


        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.chatFragment,
                R.id.addPostFragment,
                R.id.historyFragment,
                R.id.profileFragment
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


        //for filter card
        listener = supportFragmentManager.findFragmentById(R.id.home_fragment) as? OnCardVisibilityChangeListener

        //preparing the blockeduserList from blockservice
        BlockService.getUserBlockList { }

        try {
            val notificationDetails = intent.getSerializableExtra(EXTRA_POST)
            if (notificationDetails != null) {
                val navController = Navigation.findNavController(this, R.id.nav_fragment)
                navController.navigate(R.id.viewPostFragment, Bundle().apply {
                    putSerializable(
                        EXTRA_POST, PostService.notificationPost
                    )
                })
            }
        } catch (e: Exception) {
            Log.d("Notification Intent", "EXC: " + e.localizedMessage)
        }

        try{
            if(AuthService.newPassword!=""){
                navController.navigate(R.id.changePasswordFragment2)
                }
            }
        catch (e:Exception){
            Log.d("Change Password Intent", "EXC: " + e.localizedMessage)
        }
    }

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_fragment)
        val currentFragment = navHostFragment?.childFragmentManager?.fragments?.get(0)
        println("Current Fragment when backpressed is $currentFragment")

        if (currentFragment is HomeFragment) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }
            doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()

            Handler(Looper.getMainLooper()).postDelayed({
                doubleBackToExitPressedOnce = false
            }, 2000)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.appbar_nav, menu)
        item = menu.findItem(R.id.nav_search)
        item1 = menu.findItem(R.id.nav_notifications)
        item2 = menu.findItem(R.id.nav_logout)
        item3= menu.findItem(R.id.nav_filter)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        println(currentFragment)
        val item = menu.findItem(R.id.nav_search)
        val item1 = menu.findItem(R.id.nav_notifications)
        val item2 = menu.findItem(R.id.nav_logout)
        val item3= menu.findItem(R.id.nav_filter)

        if (currentFragment == "Chat Rooms" || currentFragment == "Donation History" || currentFragment == "User Profile" || currentFragment == "Notifications" || currentFragment == "Post") {
            item.isVisible = false
            item1.isVisible = false
            item2.isVisible = false
            item3.isVisible=false
            supportActionBar!!.show()
        }

        if (currentFragment == App.sharedPrefs.userName) {
            item.isVisible = false
            item1.isVisible = false
            item2.isVisible = true
            item3.isVisible=false

            supportActionBar!!.show()
        }

        if (currentFragment != "Add New Post" && currentFragment != App.sharedPrefs.userName && currentFragment != "Individual Chat Room" && currentFragment != App.sharedPrefs.userName && currentFragment != "Chat Rooms" && currentFragment != "Donation History" && currentFragment != "User Profile" && currentFragment != "Notifications" && currentFragment != "Post") {
            item.isVisible = true
            item1.isVisible = true
            item2.isVisible = false
            item3.isVisible=true

            supportActionBar!!.show()
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_search -> {
                val blockedFrom = ArrayList<String>()
                //if user is blocked, hide their profiles from search list
                BlockService.getBlockedList { complete ->
                    if (complete) {
                        for (username in BlockService.blockedList) {
                            val userJSONObject = JSONObject(username.blocked_by_id)
                            val username = userJSONObject.getString("user_name")
                            blockedFrom.add(username)
                        }
                    }
                }
                AuthService.getAllUsers { complete ->
                    if (complete) {
                        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                        val inflater = LayoutInflater.from(this)
                        val dialogView: View = inflater.inflate(R.layout.search_user_dialog, null)
                        builder.setView(dialogView)

                        val autoCompleteTextView: AutoCompleteTextView =
                            dialogView.findViewById(R.id.spinner_search2)
                        val userRV: RecyclerView = dialogView.findViewById(R.id.usersRV)

                        val dialog: AlertDialog = builder.create()
                        dialog.show()

                        val usersToRemove = mutableListOf<User>()
                        for (user in AuthService.userList) {
                            // if the user is blocked; remove the user from userList
                            if (blockedFrom.isNotEmpty()) {
                                for (blockedFrom in blockedFrom) {
                                    if (user.user_name == blockedFrom) {
                                        usersToRemove.add(user)
                                    }
                                }
                            }
                        }
                        AuthService.userList.removeAll(usersToRemove.toSet())

                        val userAdapter = UserAdapter(this, AuthService.userList) { user ->
                            val navController =
                                Navigation.findNavController(this, R.id.nav_fragment)
                            navController.navigate(R.id.userViewProfileFragment2, Bundle().apply {
                                putSerializable(
                                    EXTRA_USER, user
                                )
                            })
                            dialog.dismiss()
                        }
                        autoCompleteTextView.setAdapter(userAdapter)

                        val layoutManager = LinearLayoutManager(this)
                        userRV.layoutManager = layoutManager

                        adapter = UserAdapter(this, AuthService.userList) { user ->
                            //on Click do something--> open individual user's profile
                            val navController =
                                Navigation.findNavController(this, R.id.nav_fragment)

                            //if opened own's profile, open profile fragment
                            if (user.user_name == App.sharedPrefs.userName) {
                                navController.navigate(
                                    R.id.profileFragment
                                )
                            } else {
                                //open user profile
                                navController.navigate(
                                    R.id.userViewProfileFragment2,
                                    Bundle().apply {
                                        putSerializable(
                                            EXTRA_USER, user
                                        )
                                    })
                            }
                            dialog.dismiss()
                        }
                        userRV.adapter = adapter

                        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence?, start: Int, count: Int, after: Int
                            ) {
                            }

                            override fun onTextChanged(
                                s: CharSequence?, start: Int, before: Int, count: Int
                            ) {
                                //on Text change, keep searching for users
                                onUserSearch(s.toString(), dialog, userRV)
                            }

                            override fun afterTextChanged(s: Editable?) {
                                val selectedUser = s.toString()
                                // do something with searchText
                                //onUserSearch(selectedUser, dialog, userRV)
                                autoCompleteTextView.setAdapter(userAdapter)
                            }
                        })
                    }
                }
            }
            R.id.nav_notifications -> {
                //open notification fragment; Initialize NavController
                val navController = Navigation.findNavController(this, R.id.nav_fragment)
                navController.navigate(R.id.notificationFragment)
            }

            R.id.nav_filter -> {
                count+=1
                Toast.makeText(this,"Filter clicked",Toast.LENGTH_SHORT).show()
                val filterCard= findViewById<CardView>(R.id.filterCard)
                filterCard.isVisible = count%2 != 0
                println(count%2 != 0)
                listener?.onCardVisibilityChanged(count%2 != 0)
/*
                if (!filterCard.isVisible) {
                    println("invisible")
                    //(supportFragmentManager.findFragmentById(R.id.home_fragment) as HomeFragment?)?.getAllPost()
                    //println((supportFragmentManager.findFragmentById(R.id.home_fragment) as HomeFragment?)?.getAllPost())
                    HomeFragment().GetInstance()?.getAllPost()
                    println(HomeFragment().GetInstance()?.getAllPost())
                    println("reference accessed of home frag")
                    *//*fragmentH.getAllPost()
                    fragmentH.selected_filter="1"
                    fragmentH.initialLoad=true*//*
                }*/
            }

            //when logout pressed
            R.id.nav_logout -> {
                UserDataService.logout()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun onUserSearch(selectedUser: String, dialog: AlertDialog, userRV: RecyclerView) {
        if (selectedUser != "") {
            var newList = mutableListOf<User>()
            val navController =
                Navigation.findNavController(this@DashboardActivity, R.id.nav_fragment)
            for (user in AuthService.userList) {
                //when multiple user names matches with the typed name; show a list of matching names
                if (user.user_name.contains(selectedUser)) {
                    newList.add(user)
                    adapter = UserAdapter(applicationContext, newList as ArrayList<User>) { user ->
                        //on Click do something--> open individual user's profile
                        navController.navigate(R.id.userViewProfileFragment2, Bundle().apply {
                            putSerializable(EXTRA_USER, user)
                        })
                        dialog.dismiss()
                    }
                    userRV.adapter = adapter
                }
                //when a specific user's name is typed, redirect to their profile
                if (user.user_name == selectedUser) {
                    //if opened own's profile, open profile fragment
                    if (user.user_name == App.sharedPrefs.userName) {
                        navController.navigate(R.id.profileFragment)
                    } else {
                        //open user profile
                        navController.navigate(R.id.userViewProfileFragment2, Bundle().apply {
                            putSerializable(EXTRA_USER, user)
                        })
                    }
                    dialog.dismiss()
                } else {
                    newList.clear()
                }
            }
        }
    }
}