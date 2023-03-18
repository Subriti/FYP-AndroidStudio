package com.example.notificationpermissions.Models

class Clothes(val cloth_id: String, val clothes_category_id: String,val item_category_id: String,val cloth_size: String,val cloth_condition: String,val cloth_season: String, val cloth_media: String) {
    override fun toString(): String {
        return cloth_id
    }
}
