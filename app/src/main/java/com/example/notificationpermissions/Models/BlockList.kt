package com.example.notificationpermissions.Models

import java.io.Serializable

class BlockList(
    val block_id: String,
    val blocked_user_id: String,
    val blocked_by_id: String
) :
    Serializable
