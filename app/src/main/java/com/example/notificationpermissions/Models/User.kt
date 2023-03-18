package com.example.notificationpermissions.Models

import java.io.Serializable

class User(
    val user_id: String,
    val user_name: String,
    val user_profile: String,
    val email: String,
    val phone_number: String,
    val location: String,
    val fcm_token: String,
    //val is_admin: Boolean,
    val hide_email: String,
    val hide_phone: String
) :
    Serializable
