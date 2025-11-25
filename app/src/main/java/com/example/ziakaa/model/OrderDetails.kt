package com.example.ziakaa.model

// com.example.ziakaa.model.OrderDetails.kt
// Example structure based on previous steps



// 🔥 CRITICAL: Every field that is NOT guaranteed to be in every Firebase node MUST be nullable.
data class OrderDetails(
    // User & General Details
    val userUid: String? = null,
    val userName: String? = null,
    val userPhone: String? = null,
    val userAddress: String? = null,

    // Food Details (These lists must be nullable to prevent null object errors)
    // Use List<String>? = emptyList() as a safer default
    val foodItemName: List<String>? = emptyList(),
    val foodItemPrice: List<String>? = emptyList(),
    val foodItemImage: List<String>? = emptyList(),
    val foodItemQuantities: List<Int>? = emptyList(),

    // Status and Payment
    val currentTime: String? = null,
    val totalAmount: String? = null,
    val paymentMethod: String? = null,

    // Booleans should be nullable to handle missing data in older orders
    val isAccepted: Boolean? = false,
    val paymentSuccessful: Boolean? = false,
    val isCanceled: Boolean? = false,  // Your new field

    // Order Key
    val itemPushKey: String? = null

    // Add any other fields you see in your Firebase OrderDetails node!
)
