package com.example.notificationpermissions.Activities

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

        loginSpinner = findViewById(R.id.loginSpinner)
        loginSpinner.visibility = View.INVISIBLE

        val forgotPasswordBtn = findViewById<TextView>(R.id.forgotPassword)
        forgotPasswordBtn.setOnClickListener {
            //reset password by sending OTP? then redirect to change password fragment
            //sendVerificationCode(number)
            val email = findViewById<TextView>(R.id.emailText).text.toString()

            println(email)
            if (email.isNotEmpty()) {
                println("email not empty")
                AuthService.resetPassword(email) { resetPasswordSuccess ->
                    println("Reset Password Success: " + resetPasswordSuccess)
                    if (resetPasswordSuccess) {
                        checkSmsPermission()
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    "Please fill in the email for password reset",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        loginBtn = findViewById(R.id.loginBtn)

        loginBtn.setOnClickListener {
            enableSpinner(true)
            hideKeyboard()
            val email = findViewById<TextView>(R.id.emailText).text.toString()
            val password = findViewById<TextView>(R.id.passwordText).text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                AuthService.loginUser(email, password) { loginSuccess ->
                    if (loginSuccess) {
                        AuthService.findUser(this) { findSuccess ->
                            if (findSuccess) {
                                //if user is admin, redirect to admin activity; else dashboard activity
                                if (App.sharedPrefs.isAdmin == "true") {
                                    val intent = Intent(this, AdminActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    val intent = Intent(this, DashboardActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                    finish()
                                }
                                //get registration token:
                                FirebaseMessaging.getInstance().token.addOnCompleteListener {
                                    if (it.isComplete) {
                                        val firebaseToken = it.result.toString()
                                        App.sharedPrefs.token = it.result.toString()
                                        //store this token to the database it is device specific.
                                        AuthService.updateFCMToken(firebaseToken) {}
                                    }
                                }
                                FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
                                enableSpinner(false)
                                finish()
                            } else {
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

    private val PERMISSION_REQUEST_SEND_SMS = 113

    private fun checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                PERMISSION_REQUEST_SEND_SMS
            )
        } else {
            // Permission already granted. Send SMS.
            sendCustomMessage("${AuthService.resetPhone}")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_SEND_SMS) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted. Send SMS.
                sendCustomMessage("${AuthService.resetPhone}")
            } else {
                // Permission denied. Show a message and don't send SMS.
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendCustomMessage(phone: String) {
        val message =
            "Your newly generated password is: ${AuthService.newPassword}\nYou are requested to change your password again." // create a custom message to send
        try {
            val smsManager = SmsManager.getDefault() // get the default SMS manager
            smsManager.sendTextMessage(
                phone,
                null,
                message,
                null,
                null
            )
            // send the message to the user's phone number
            Toast.makeText(this,"Password has been sent to your registered mobile number.",Toast.LENGTH_SHORT).show()
        } catch (ex: SecurityException) {
            // handle SecurityException when the app does not have permission to send SMS
            ex.printStackTrace()
        } catch (ex: Exception) {
            // handle other exceptions that may occur
            ex.printStackTrace()
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

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if (inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }
}


