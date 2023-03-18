package com.example.notificationpermissions.Models

import java.io.Serializable

class Transaction(
    val transaction_id: String,
    val rating: String,
    val transaction_date: String,
    val post_id: String,
    val recieved_user_id: String
) :
    Serializable
