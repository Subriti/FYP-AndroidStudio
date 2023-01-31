package com.example.notificationpermissions

import java.io.Serializable

class PostDetails (val post_id:String, val post_by:String, val user_id:String, val user_email:String, val user_profile:String, val user_phone:String, val media_file: String, val description: String, val created_datetime:String, val location: String, val cloth_id: String, val clothes_category_id: String,val item_category_id: String,val cloth_size: String,val cloth_condition: String,val cloth_season: String, val donation_status: String) :
    Serializable {
    override fun toString(): String {
        return media_file
    }
}
