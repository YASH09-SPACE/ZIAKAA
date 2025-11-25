package com.example.ziakaa.model

// CartItems.kt (or wherever your model classes are)
// Assuming your CartItems data class is structured like this:
data class CartItems(
    val foodImage: String? = null,    // 1st argument in your code
    val foodName: String? = null,     // 2nd argument in your code
    val foodDescription: String? = null, // 3rd argument in your code
    val foodPrice: String? = null,    // 4th argument in your code
    var foodQuantity: Int? = 1,       // 5th argument in your code
    val foodIngredient: String? = null // Often missed
)