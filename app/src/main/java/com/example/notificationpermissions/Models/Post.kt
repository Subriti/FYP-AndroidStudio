package com.example.notificationpermissions.Models

import java.io.Serializable

class Post (val post_id:String, val post_by:String, val media_file: String, val description: String, val created_datetime:String, val location: String, val cloth_id: String, val donation_status: String) :
    Serializable {
    override fun toString(): String {
        return media_file
    }
}
