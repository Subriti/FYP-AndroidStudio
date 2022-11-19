package com.example.notificationpermissions

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class AlertDetails : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val builder = this?.let { it1 -> AlertDialog.Builder(it1) }
        val dialogView = layoutInflater.inflate(R.layout.confirmation_prompt, null)

        if (builder != null) {
            builder.setView(dialogView)
                .setPositiveButton("Send") { _, i ->

                    val radioGroup =
                        dialogView.findViewById<RadioGroup>(R.id.confirmation_group)
                    val selection = radioGroup.checkedRadioButtonId

                    try {
                        var confirmation = dialogView.findViewById<RadioButton>(selection)
                        if (confirmation.text == "Yes") {
                            Toast.makeText(
                                this,
                                "Thankyou for your time and effort",
                                Toast.LENGTH_LONG
                            ).show()

                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            this,
                            "Please input one choice",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                .setNegativeButton("Cancel") { _, i -> //cancel and close the dialog
                }
                .show()
        }
    }
    }