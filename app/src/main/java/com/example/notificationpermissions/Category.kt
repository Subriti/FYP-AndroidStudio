package com.example.notificationpermissions

class Category(val category_id: String, val category_name: String, val category_type: String) {
        override fun toString(): String {
            return category_name
        }
}
