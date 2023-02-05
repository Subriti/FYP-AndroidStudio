package com.example.notificationpermissions.Activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.notificationpermissions.Adapters.TOPIC
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.AuthService
import com.example.notificationpermissions.Utilities.App
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

                                /*  FirebaseService.sharedPref= getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
  */
                                //get registration token:
                                FirebaseMessaging.getInstance().token.addOnCompleteListener {
                                    if (it.isComplete) {
                                        val firebaseToken = it.result.toString()
                                        App.sharedPrefs.token=it.result.toString()
                                        //store this token to the database it is device specific.
                                        AuthService.updateFCMToken(firebaseToken){
                                                response-> println("Update response: $response")

                                        }
                                    }
                                }
                                //subscribe to the topic:
                                FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)

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
}


