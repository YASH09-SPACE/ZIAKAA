package com.example.ziakaa

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.ziakaa.databinding.ActivityDetailsBinding
import com.example.ziakaa.model.CartItems
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
        private var foodName: String? = null
        private var foodImage: String? = null
        private var foodDescriptions: String? = null
        private var foodIngredient: String? = null
        private var foodPrice: String? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        foodName = intent.getStringExtra("MenuItemName")
        foodDescriptions = intent.getStringExtra("MenuItemDescription")
        foodIngredient = intent. getStringExtra( "MenuItemIngredients")
        foodPrice = intent.getStringExtra( "MenuItemPrice")
        foodImage = intent.getStringExtra( "MenuItemImage")
        with(binding) {
            detailFoodName.text = foodName
            detailDescription.text = foodDescriptions
            detailIngredient.text = foodIngredient
            Glide.with(this@DetailsActivity). load(Uri.parse(foodImage)).into(detailFoodImage)
        }

        binding.imageButton.setOnClickListener {
            finish()
        }
        binding.addItemButton.setOnClickListener {
            addItemToCart()
        }
    }

    private fun addItemToCart() {
        val database = FirebaseDatabase.getInstance().reference
        val userId = auth.currentUser?.uid?:""
        val cartItem = CartItems(
            foodName = foodName.toString(),
            foodImage = foodImage.toString(),
            foodDescription = foodDescriptions.toString(),
            foodPrice = foodPrice.toString(),
            foodQuantity = 1,
            foodIngredient = foodIngredient.toString()
        )
        database.child("user").child(userId).child("CartItems").push().setValue(cartItem).addOnSuccessListener {
            Toast.makeText(this,"Items added into cart Successfuly 😁", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this,"Items not added into cart 😒", Toast.LENGTH_SHORT).show()
        }

    }
}