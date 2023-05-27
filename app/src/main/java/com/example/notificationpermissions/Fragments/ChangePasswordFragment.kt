package com.example.notificationpermissions.Fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.AuthService
import com.example.notificationpermissions.Utilities.App


class ChangePasswordFragment : Fragment() {
    private lateinit var progressBar: ProgressBar
    private lateinit var saveChanges: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_change_password, container, false)
        //(activity as DashboardActivity?)!!.currentFragment = this

        progressBar = view.findViewById(R.id.progressBar)
        progressBar.visibility = View.INVISIBLE

        val currentPassword = view.findViewById<TextView>(R.id.currentPasswordText)
        val password = view.findViewById<TextView>(R.id.passwordText)
        val repassword = view.findViewById<TextView>(R.id.repasswordText)
        val passwordIndicator = view.findViewById<TextView>(R.id.passwordIndicatorText)
        passwordIndicator.visibility = View.GONE

        val resetPassword = view.findViewById<TextView>(R.id.resetPassword)
        saveChanges = view.findViewById(R.id.saveChanges)


        saveChanges.setOnClickListener {
            if (currentPassword.text.toString() != "" && password.text.toString() != "" && currentPassword.text.toString() == password.text.toString()) {
                Toast.makeText(
                    requireContext(),
                    "Current Password and New Password cannot be the same",
                    Toast.LENGTH_LONG
                ).show()
                enableSpinner(false)
            } else if (currentPassword.text.toString() != "" && password.text.toString() != "" && repassword.text.toString() != "" && password.text.toString() == repassword.text.toString()) {
                passwordIndicator.visibility = View.VISIBLE
                passwordIndicator.text = "Password Matched"
                passwordIndicator.setTextColor(getColor(requireContext(), R.color.darkgreen))

                enableSpinner(true)
                //save updated details to the database
                AuthService.changePassword(
                    currentPassword.text.toString(), password.text.toString()
                ) { changeSuccess ->
                    println("Change Password success: $changeSuccess")
                    if (changeSuccess) {
                        view.findNavController()
                            .navigate(R.id.action_changePasswordFragment2_to_profileFragment)
                        enableSpinner(false)
                        Toast.makeText(
                            requireContext(), "Password was changed successfully", Toast.LENGTH_LONG
                        ).show()
                        //saveChanges.isVisible = false

                    } else {
                        Toast.makeText(
                            requireContext(),
                            AuthService.errorMessage,
                            Toast.LENGTH_LONG
                        ).show()
                        enableSpinner(false)
                    }
                }
            } else if (password.text.toString() != "" && repassword.text.toString() != "" && password.text.toString() != repassword.text.toString()) {
                passwordIndicator.visibility = View.VISIBLE
                passwordIndicator.text = "Password didn't match"
                passwordIndicator.setTextColor(getColor(requireContext(), R.color.falured))
            } else {
                Toast.makeText(
                    requireContext(), "Make sure all the fields are filled in.", Toast.LENGTH_LONG
                ).show()
                enableSpinner(false)
            }
        }

        resetPassword.setOnClickListener {
            //reset password by sendingSMS
            AuthService.resetPassword(App.sharedPrefs.userEmail) { resetPasswordSuccess ->
                println("Reset Password Success: "+resetPasswordSuccess)
                if (resetPasswordSuccess) {
                    checkSmsPermission()
                }
            }
        }
        return view
    }

    private val PERMISSION_REQUEST_SEND_SMS = 123

    private fun checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.SEND_SMS),
                PERMISSION_REQUEST_SEND_SMS
            )
        } else {
            // Permission already granted. Send SMS.
            sendCustomMessage("${App.sharedPrefs.phoneNumber}")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_SEND_SMS) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted. Send SMS.
                sendCustomMessage("${App.sharedPrefs.phoneNumber}")
            } else {
                // Permission denied. Show a message and don't send SMS.
                Toast.makeText(requireContext(), "SMS permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendCustomMessage(phone: String) {
        val message =
            "Your newly generated password is: \n${AuthService.newPassword}\nYou are requested to change your password again." // create a custom message to send
        try {
            val smsManager = SmsManager.getDefault() // get the default SMS manager
            smsManager.sendTextMessage(
                phone,
                null,
                message,
                null,
                null
            ) // send the message to the user's phone number
            Toast.makeText(requireContext(),"Message has been sent to your registered Mobile No.",Toast.LENGTH_SHORT).show()
        } catch (ex: SecurityException) {
            // handle SecurityException when the app does not have permission to send SMS
            ex.printStackTrace()
        } catch (ex: Exception) {
            // handle other exceptions that may occur
            ex.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        // Invalidate the options menu to force onPrepareOptionsMenu to be called again
        activity?.invalidateOptionsMenu()
    }

    private fun enableSpinner(enable: Boolean) {
        if (enable) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.INVISIBLE
        }
        saveChanges.isEnabled = !enable
    }
}