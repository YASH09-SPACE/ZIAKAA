package com.example.ziakaa.model

data class NotificationItem(
    val message: String? = null,
    val image: Int? = null, // Using Int for local drawable resource ID (R.drawable.X)
    val time: Long? = null // Optional: Timestamp for sorting/display
)
