package com.example.notificationpermissions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging


class LoginActivity : AppCompatActivity() {
    lateinit var loginBtn: Button
    lateinit var loginSpinner: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginSpinner= findViewById(R.id.loginSpinner)
        loginSpinner.visibility= View.INVISIBLE

        loginBtn= findViewById(R.id.loginBtn)

        loginBtn.setOnClickListener{
            enableSpinner(true)

            val email = findViewById<TextView>(R.id.emailText).text.toString()
            val password = findViewById<TextView>(R.id.passwordText).text.toString()

            hideKeyboard()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                AuthService.loginUser(email, password) { loginSuccess ->
                    if (loginSuccess) {
                        AuthService.findUser(this) { findSuccess ->
                            println(findSuccess)
                            if (findSuccess) {
                                //When success, it broadcasts to other activities as well that user was found and is logged in
                                //this is done in authUser
                                val intent= Intent(this, DashboardActivity::class.java)
                                startActivity(intent)

                                //generate FirebaseCloudMessaging Token and save to firebase

                                // realtime database reference
                                val realtimeDatabase = Firebase.database

                                // Retreving the FCM token
                                    FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                                        if (!task.isSuccessful) {
                                            return@OnCompleteListener
                                        }

                                        // fetching the token
                                        val token = task.result
                                        println(token)
                                        // directory reference
                                        val tokenDirRef = realtimeDatabase.getReference("Tokens")

                                        // storing the value
                                        tokenDirRef.setValue(token.toString())

                                        // toast to show  message
                                        Toast.makeText(
                                            baseContext,
                                            "Firebase Generated Successfully and saved to realtime database",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    })

                                enableSpinner(false)
                                finish()

                            }else{
                                errorToast()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_LONG).show()
                        enableSpinner(false)
                    }
                }
            } else {
                Toast.makeText(this, "Please fill in both email and password", Toast.LENGTH_LONG).show()
                enableSpinner(false)
            }
        }
    }
    private fun errorToast() {
        Toast.makeText(this, "Something went wrong, please try again.", Toast.LENGTH_LONG).show()
        enableSpinner(false)
    }

    private fun enableSpinner(enable: Boolean) {
        if (enable) {
            loginSpinner.visibility = View.VISIBLE
        } else {
            loginSpinner.visibility = View.INVISIBLE
        }
        loginBtn.isEnabled = !enable
    }

    fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }

    }

        //works
        fun ShowNotification(/*messageBody: String*/) {
            val intent = Intent(this, AlertDetails::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            val pendingIntent = PendingIntent.getActivity(
                this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
            val channelId = getString(R.string.default_notification_channel_id)
            val defaultSoundUri: Uri =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
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
