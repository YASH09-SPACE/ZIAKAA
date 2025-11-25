package com.example.ziakaa.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri // Required for String.toUri extension
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ziakaa.databinding.CartItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.example.ziakaa.model.CartItems
import java.util.HashMap // Using fully qualified name for clarity

class CartAdapter(
    private val context: Context,
    private var cartItemsList: MutableList<CartItems>,
    private val cartItemKey: MutableList<String>
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    // NOTE: 'lateinit' is used here, initialized in init block. (Warning suppressed)
    private lateinit var cartItemsReference: DatabaseReference

    // FIX: Changed from IntArray to MutableList<Int> to resolve Assignment Type Mismatch errors.
    private var itemQuantities: MutableList<Int>

    init {
        val userId: String = auth.currentUser?.uid ?: ""
        val database: FirebaseDatabase = FirebaseDatabase.getInstance()

        // FIX: Use .toMutableList() instead of .toIntArray()
        itemQuantities = cartItemsList.map { it.foodQuantity ?: 1 }.toMutableList()

        cartItemsReference = database.reference.child("user").child(userId).child("CartItems")
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CartViewHolder {
        val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CartViewHolder,
        position: Int
    ) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = cartItemsList.size
    fun getUpdatedItemsQuantities(): MutableList<Int> {
        val itemQuantity = mutableListOf<Int>()
        itemQuantity.addAll(itemQuantities)
        return itemQuantity
    }


    inner class CartViewHolder(private val binding: CartItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                val item = cartItemsList[position]
                val quantity = itemQuantities[position]

                cartFoodName.text = item.foodName
                CartItemPrice.text = item.foodPrice

                // Load image using Glide
                val uriString = item.foodImage
                // FIX: Use KTX extension function .toUri()
                val uri: Uri? = uriString?.toUri()
                Glide.with(context).load(uri).into(CartImage)

                cartItemQuantity.text = quantity.toString()

                minusbutton.setOnClickListener {
                    deceaseQuantity(position)
                }
                plusbutton.setOnClickListener {
                    increaseQuantity(position)
                }
                deleteButton.setOnClickListener {
                    // FIX: Use bindingAdapterPosition instead of deprecated adapterPosition
                    val itemPosition = bindingAdapterPosition
                    if (itemPosition != RecyclerView.NO_POSITION) {
                        deleteItem(itemPosition)
                    }
                }
            }
        }

        // Function to save quantity change to Firebase
        private fun updateItemQuantity(position: Int, newQuantity: Int) {
            val uniqueKey = cartItemKey[position]
            // Use explicit HashMap<String, Any>() for clarity
            val quantityMap = HashMap<String, Any>()
            quantityMap["foodQuantity"] = newQuantity

            cartItemsReference.child(uniqueKey).updateChildren(quantityMap).addOnSuccessListener {
                // Also update the quantity in the local CartItems object
                cartItemsList[position].foodQuantity = newQuantity
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to update quantity", Toast.LENGTH_SHORT).show()
                // Optionally revert local change if Firebase update fails
                itemQuantities[position] = cartItemsList[position].foodQuantity ?: 1
                binding.cartItemQuantity.text = itemQuantities[position].toString()
            }
        }

        private fun increaseQuantity(position: Int) {
            if (itemQuantities[position] < 10) {
                itemQuantities[position]++
                binding.cartItemQuantity.text = itemQuantities[position].toString()
                updateItemQuantity(position, itemQuantities[position]) // Update Firebase
            }
        }

        private fun deceaseQuantity(position: Int) {
            if (itemQuantities[position] > 1) {
                itemQuantities[position]--
                binding.cartItemQuantity.text = itemQuantities[position].toString()
                updateItemQuantity(position, itemQuantities[position]) // Update Firebase
            }
        }

        private fun deleteItem(position: Int) {
            val uniqueKey = cartItemKey[position]
            removeItem(position, uniqueKey)
        }

        private fun removeItem(position: Int, uniqueKey: String) {
            if (uniqueKey.isNotEmpty()) {
                cartItemsReference.child(uniqueKey).removeValue().addOnSuccessListener {

                    // Remove item from all local lists
                    cartItemsList.removeAt(position)
                    cartItemKey.removeAt(position)

                    Toast.makeText(context, "Item Deleted", Toast.LENGTH_SHORT).show()

                    // FIX: Rebuild itemQuantities list using .toMutableList() to maintain type
                    itemQuantities = itemQuantities.filterIndexed { index, _ -> index != position }.toMutableList()

                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, cartItemsList.size)
                }.addOnFailureListener {
                    Toast.makeText(context, "Failed to Delete", Toast.LENGTH_SHORT).show()
                }
            }
        }
        //Function 'getUpdatedItemsQuantities' is kept as it might be used externally. (Warning ignored)
    }
}