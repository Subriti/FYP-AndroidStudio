package com.example.notificationpermissions.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.notificationpermissions.Adapters.SliderAdapter
import com.example.notificationpermissions.R
import com.example.notificationpermissions.Utilities.App
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //getting user State
        println("isLogged in : ${App.sharedPrefs.isLoggedIn}")
        if (App.sharedPrefs.isLoggedIn) {
            // Do something for the logged user; open Dashboard Activity
            if (App.sharedPrefs.isAdmin=="false") {
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            }
            else if (App.sharedPrefs.isAdmin=="true") {
                //if admin, open adminActivity
                val intent = Intent(this, AdminActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else {
            // Do something for the unlogged user
            setContentView(R.layout.activity_main)

            var sliderView = findViewById<SliderView>(R.id.slider)
            var images = arrayOf(
                R.drawable.donate4,
                R.drawable.donate,
                R.drawable.donate5
            )

            val sliderAdapter = SliderAdapter(images)
            sliderView.setSliderAdapter(sliderAdapter)
            sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM)
            sliderView.setSliderTransformAnimation(SliderAnimations.DEPTHTRANSFORMATION)
            sliderView.startAutoCycle()

            val register = findViewById<Button>(R.id.registerBtn)
            register.setOnClickListener {
                val intent = Intent(this, SignUpPageActivity::class.java)
                startActivity(intent)
            }

            val login = findViewById<TextView>(R.id.loginBtn)
            login.setOnClickListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }
}