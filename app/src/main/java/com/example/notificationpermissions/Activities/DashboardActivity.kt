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
import com.example.notificationpermissions.Services.PostService
import com.example.notificationpermissions.Services.UserDataService
import com.example.notificationpermissions.Utilities.App
import com.example.notificationpermissions.Utilities.EXTRA_POST
import com.example.notificationpermissions.Utilities.EXTRA_USER
import com.google.android.material.bottomnavigation.BottomNavigationView


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
/*
    lateinit var webSocketClient: WebSocketClient*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        //createWebSocketClient()

        /*socket = IO.socket("http://localhost:9090")
        socket.connect()
        socket.on("message") {
            val data = it[0] as String
            Log.d("SocketIO", "received message: $data")
        }
        socket.emit("message", "Connected to a client")
*/

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

        /*val networkTask = NetworkTask()
        networkTask.execute()*/

        /*runOnUiThread {
            val SERVER_IP = "localhost"
            val SERVER_PORT = 9090

            val clientSocket = java.net.Socket(SERVER_IP, SERVER_PORT)
            val outToServer = DataOutputStream(clientSocket.getOutputStream())
            val inFromServer = BufferedReader(InputStreamReader(clientSocket.getInputStream()))

            // Send the message to the server
            val message = "client_1: Hello from Android Studio!"
            outToServer.writeBytes(
                """
                $message
                """.trimIndent()
            )
            // Receive a response from the server
            val response: String = inFromServer.readLine()
            println("FROM SERVER: $response")
            clientSocket.close()
        }*/

        try {
            val notificationDetails = intent.getSerializableExtra(EXTRA_POST)
            if (notificationDetails != null) {
                val navController = Navigation.findNavController(this, R.id.nav_fragment)
                navController.navigate(R.id.viewPostFragment, Bundle().apply {
                    putSerializable(
                        EXTRA_POST,
                        PostService.notificationPost
                    )
                })
            }
        } catch (e: Exception) {
            Log.d("Notification Intent", "EXC: " + e.localizedMessage)
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
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        println(currentFragment)
        val item = menu.findItem(R.id.nav_search)
        val item1 = menu.findItem(R.id.nav_notifications)
        val item2 = menu.findItem(R.id.nav_logout)

        if (currentFragment == "Chat Rooms" || currentFragment == "Donation History" || currentFragment== "User Profile" || currentFragment=="Notifications" || currentFragment=="Post") {
            item.isVisible = false
            item1.isVisible = false
            item2.isVisible = false
            supportActionBar!!.show()
        }

       /* if (currentFragment == "Donation History") {
            item.isVisible = false
            item1.isVisible = false
            item2.isVisible = false
            supportActionBar!!.show()
        }

        if (currentFragment== "User Profile") {
            item.isVisible = false
            item1.isVisible = false
            item2.isVisible = false
            supportActionBar!!.show()
        }*/


        if (currentFragment == App.sharedPrefs.userName) {
            item.isVisible = false
            item1.isVisible = false
            item2.isVisible = true

            supportActionBar!!.show()
        }

        if (currentFragment != "Add New Post" && currentFragment != App.sharedPrefs.userName && currentFragment != "Individual Chat Room" && currentFragment != App.sharedPrefs.userName && currentFragment != "Chat Rooms" && currentFragment != "Donation History" && currentFragment!= "User Profile" && currentFragment!="Notifications" && currentFragment !="Post") {
            item.isVisible = true
            item1.isVisible = true
            item2.isVisible = false

            supportActionBar!!.show()
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.nav_search) {
            Toast.makeText(this, "Search Icon was clicked.", Toast.LENGTH_SHORT).show()

            AuthService.getAllUsers { complete ->
                println("Get users success--> $complete")
                if (complete) {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                    val inflater = LayoutInflater.from(this)
                    val dialogView: View =
                        inflater.inflate(R.layout.search_user_dialog, null)
                    builder.setView(dialogView)

                    val autoCompleteTextView: AutoCompleteTextView =
                        dialogView.findViewById(R.id.spinner_search2)
                    val userRV: RecyclerView = dialogView.findViewById(R.id.usersRV)

                    val dialog: AlertDialog = builder.create()
                    dialog.show()

                    val userAdapter = UserAdapter(this, AuthService.userList) { user ->
                        val navController =
                            Navigation.findNavController(this, R.id.nav_fragment)
                        navController.navigate(R.id.userViewProfileFragment2, Bundle().apply {
                            putSerializable(
                                EXTRA_USER,
                                user
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
                                        EXTRA_USER,
                                        user
                                    )
                                }
                            )
                        }

                        dialog.dismiss()
                    }
                    userRV.adapter = adapter


                    autoCompleteTextView.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                            //on Text change, keep searching for users
                            println("On Text Change")
                            onUserSearch(s.toString(), dialog, userRV)
                        }

                        override fun afterTextChanged(s: Editable?) {
                            val selectedUser = s.toString()
                            println("After Text Change")

                            // do something with searchText
                            onUserSearch(selectedUser, dialog, userRV)
                        }
                    })
                }
            }

        } else if (item.itemId == R.id.nav_notifications) {
            Toast.makeText(this, "Clicked Notifications Icon..", Toast.LENGTH_SHORT).show()
            //ShowNotification()

            //open notification fragment
            // Initialize NavController
            val navController = Navigation.findNavController(this, R.id.nav_fragment)
            navController.navigate(R.id.notificationFragment)
        }
        //when logout pressed
        else if (item.itemId == R.id.nav_logout) {
            UserDataService.logout()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    fun onUserSearch(selectedUser: String, dialog: AlertDialog, userRV: RecyclerView) {
        if (selectedUser != "") {
            var newList = mutableListOf<User>()
            for (user in AuthService.userList) {
                //when multiple user names matches with the typed name; show a list of matching names
                if (user.user_name.contains(selectedUser)) {
                    println("Name contains substring")
                    newList.add(user)
                    println(newList.size)
                    println(user.user_name)
                    adapter = UserAdapter(applicationContext, newList as ArrayList<User>) { user ->
                        println(user.user_name)
                        println(newList.size)
                        //on Click do something--> open individual user's profile
                        val navController =
                            Navigation.findNavController(
                                this@DashboardActivity,
                                R.id.nav_fragment
                            )
                        navController.navigate(
                            R.id.userViewProfileFragment2,
                            Bundle().apply {
                                putSerializable(
                                    EXTRA_USER,
                                    user
                                )
                            })
                        dialog.dismiss()

                        //when a specific user's name is typed, redirect to their profile
                        if (user.user_name == selectedUser) {
                            println("Name matches completely")
                            //on Click do something--> open individual user's profile
                            val navController =
                                Navigation.findNavController(
                                    this@DashboardActivity,
                                    R.id.nav_fragment
                                )

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
                                            EXTRA_USER,
                                            user
                                        )
                                    }
                                )
                            }

                            dialog.dismiss()
                        }
                    }
                    userRV.adapter = adapter
                }


            }
        }
    }
}