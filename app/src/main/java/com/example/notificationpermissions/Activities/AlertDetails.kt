package com.example.notificationpermissions.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RatingBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Services.PostService
import java.text.SimpleDateFormat
import java.util.*

class AlertDetails : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard_activity)

        val builder = this.let { it1 -> AlertDialog.Builder(it1) }
        val dialogView = layoutInflater.inflate(R.layout.confirmation_prompt, null)

        if (builder != null) {
            builder.setView(dialogView)
                .setPositiveButton("Send") { _, _ ->

                    val radioGroup =
                        dialogView.findViewById<RadioGroup>(R.id.confirmation_group)
                    val selection = radioGroup.checkedRadioButtonId

                    try {
                        var confirmation = dialogView.findViewById<RadioButton>(selection)
                        if (confirmation.text == "Yes") {

                            //show rating bar
                            val builder = this.let { it1 -> AlertDialog.Builder(it1) }
                            val dialogView = layoutInflater.inflate(R.layout.rating_prompt, null)

                            val ratingBar= dialogView.findViewById<RatingBar>(R.id.ratingBar)

                            if (builder != null) {
                                builder.setView(dialogView)
                                    .setPositiveButton("Rate") { _, _ ->
                                        //access the rating and use PostService.updateTrasaction rating to update the rating
                                        val rating= ratingBar.rating
                                        println(rating)
                                        println(ratingBar.numStars)

                                        //how to uniquely identify which notification belongs to which post
                                        PostService.updateRating("", rating, SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(
                                            Calendar.getInstance().time)){
                                                updateSuccess ->
                                            println("Update Transaction Rating success: $updateSuccess")
                                            if (updateSuccess) {
                                                PostService.updateDonationStatus(
                                                    "", "3"    // 3 -> completed status
                                                ) { updateSuccess ->
                                                    println("Update Donation status success: $updateSuccess")
                                                    if (updateSuccess) {
                                                        Toast.makeText(
                                                            this,
                                                            "Thank you for your time and effort",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    .setNegativeButton("Cancel") { _, _ ->
                                        //cancel and close the dialog
                                    }
                                    .show()
                            }

                        }
                        else{
                            val intent = Intent(this, DashboardActivity::class.java)
                            startActivity(intent)

                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            this,
                            "Please input one choice",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                .setNegativeButton("Cancel") { _, _ -> //cancel and close the dialog
                }
                .show()
        }
    }
    }