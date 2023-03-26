package com.example.notificationpermissions.Models

import java.io.Serializable

class Reports(
    val report_id: String,
    val reported_by: String,
    val post_id:String,
    val feedback: String,
    val report_date: String
) :
    Serializable
