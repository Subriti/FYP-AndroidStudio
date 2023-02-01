package com.example.notificationpermissions

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.smarteist.autoimageslider.SliderAnimations
import com.smarteist.autoimageslider.SliderView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //getting user State
        println("isLogged in : ${App.sharedPrefs.isLoggedIn}")
        if (App.sharedPrefs.isLoggedIn) {
            // Do something for the logged user
            // AuthService.findUser(this){}
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        } else {
            // Do something for the unlogged user
            setContentView(R.layout.activity_main)

            var sliderView = findViewById<SliderView>(R.id.slider)
            var images = arrayOf(
                R.drawable.donate2,
                R.drawable.donate3,
                R.drawable.donate4
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