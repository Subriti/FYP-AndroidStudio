package com.example.notificationpermissions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController


class ChangePasswordFragment : Fragment() {
    private lateinit var progressBar: ProgressBar
    private lateinit var saveChanges: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_change_password, container, false)
        (activity as DashboardActivity?)!!.currentFragment = this

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
                            "Something went wrong. Password Change Unsuccessful",
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
            //reset password by sending OTP?
            //sendVerificationCode(number)
        }
        return view

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