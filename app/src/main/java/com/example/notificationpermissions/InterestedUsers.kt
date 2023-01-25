package com.example.notificationpermissions

class InterestedUsers (val user_id:String, val user_name: String, val user_profile:String, val post_id:String, val post_username: String, val post_userprofile: String) {
    override fun toString(): String {
        return user_id
    }
}
