package com.example.ziakaa.Fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log // Keep Log for debugging if needed
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.ziakaa.PayOutActivity // PayOutActivity required for redirection
import com.example.ziakaa.adapter.BuyAgainAdapter
import com.example.ziakaa.databinding.FragmentHistoryBinding
import com.example.ziakaa.model.OrderDetails // OrderDetails model required
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HistoryFragment : Fragment() {
    private lateinit var binding: FragmentHistoryBinding
    private lateinit var buyAgainAdapter: BuyAgainAdapter

    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private val userOrderItems: MutableList<OrderDetails> = mutableListOf()

    // Variable to hold the push key of the most recent order for cancellation
    private var mostRecentOrderPushKey: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        retrieveOrderHistory()

        // --- ADD: Click Listener for the Recent Buy Card (Cancellation/Options) ---
        binding.cardView2.setOnClickListener {
            handleRecentOrderClick()
        }

        return binding.root
    }

    private fun retrieveOrderHistory() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val orderReference = database.reference.child("OrderDetails")

        orderReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userOrderItems.clear()
                mostRecentOrderPushKey = null
                var latestTime: Long = 0

                for (orderSnapshot in snapshot.children) {
                    // Assuming OrderDetails is now fully nullable/safe to prevent null objects
                    val order = orderSnapshot.getValue(OrderDetails::class.java)

                    if (order?.userUid == userId) {
                        userOrderItems.add(order)

                        // Check if this is the most recent order by time
                        val orderTime = order.currentTime?.toLongOrNull() ?: 0L
                        if (orderTime > latestTime) {
                            latestTime = orderTime
                            mostRecentOrderPushKey = orderSnapshot.key // Save the unique Firebase push key
                        }
                    }
                }

                userOrderItems.sortByDescending { it.currentTime }
                setupUI(userOrderItems)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load order history: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Handles the click on the most recent order card (cardView2).
     */
    private fun handleRecentOrderClick() {
        if (mostRecentOrderPushKey != null && userOrderItems.isNotEmpty()) {

            // NOTE: Since we are deleting the node, we don't need the 'isCanceled' check here.

            // Show options: Cancel or View Details
            AlertDialog.Builder(requireContext())
                .setTitle("Recent Order Options")
                .setItems(arrayOf("Cancel Order", "View Details")) { dialog, which ->
                    when (which) {
                        0 -> confirmCancellation(mostRecentOrderPushKey!!)
                        1 -> Toast.makeText(requireContext(), "View details feature not implemented.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Close", null)
                .show()
        } else {
            Toast.makeText(requireContext(), "No recent order found.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmCancellation(orderPushKey: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion") // Changed title to reflect deletion action
            .setMessage("Are you sure you want to cancel and PERMANENTLY DELETE this order record?")
            .setPositiveButton("Yes, Delete") { dialog, which ->
                cancelOrder(orderPushKey)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelOrder(orderPushKey: String) {
        val orderRef = database.reference.child("OrderDetails").child(orderPushKey)

        // 1. DELETE THE ORDER NODE ENTIRELY
        orderRef.removeValue()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Order cancelled and removed successfully.", Toast.LENGTH_SHORT).show()

                // 2. Push a cancellation notification
                pushCancellationNotification(orderPushKey)

                // 3. Refresh the UI. The deleted item will automatically disappear from the list.
                retrieveOrderHistory()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to delete order: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * Pushes a cancellation alert to the user's dedicated Notifications node.
     */
    private fun pushCancellationNotification(orderPushKey: String) {
        val userId = auth.currentUser?.uid ?: return

        // Push to the Notifications node
        val notificationRef = database.reference.child("Notifications").child(userId).push()

        val notificationData = mapOf(
            "message" to "Order #$orderPushKey has been successfully Canceled and deleted from history.",
            "status" to "CANCELED",
            "time" to System.currentTimeMillis()
        )

        notificationRef.setValue(notificationData)
            .addOnFailureListener {
                // Log or handle error if notification push fails
            }
    }


    // --- UI Setup and Buy Again Logic ---

    private fun setupUI(orderList: List<OrderDetails>) {

        // --- A. Handle the "Recent Buy" Card (CardView2) ---
        // Since the item is deleted, orderList.first() will be the NEXT most recent item.
        if (orderList.isNotEmpty() && !orderList.first().foodItemName.isNullOrEmpty()) {
            val recentOrder = orderList.first()

            // Use safe calls as OrderDetails fields may be nullable
            val recentItemName = recentOrder.foodItemName?.first() ?: "Error Loading Name"
            val recentItemPrice = recentOrder.foodItemPrice?.first() ?: "N/A"
            val recentItemImage = recentOrder.foodItemImage?.first() ?: ""

            binding.buyAgainFoodName.text = recentItemName
            binding.buyAgainFoodPrice.text = recentItemPrice

            Glide.with(requireContext()).load(recentItemImage).into(binding.buyAgainFoodImage)
        } else {
            // If the list is empty after deletion, clear the card view
            binding.buyAgainFoodName.text = "No Orders Found"
            binding.buyAgainFoodPrice.text = ""
            // Optionally clear image
        }


        // --- B. Handle the "Previously Buy" RecyclerView ---
        val foodNames = mutableListOf<String>()
        val foodPrices = mutableListOf<String>()
        val foodImages = mutableListOf<String>()

        orderList.forEach { order ->
            // No cancellation check needed, as canceled items are deleted before reaching this loop
            if (!order.foodItemName.isNullOrEmpty()) {
                // Use !! only after null check; or use ?: operator for safety
                foodNames.add(order.foodItemName?.first() ?: "N/A")
                foodPrices.add(order.foodItemPrice?.first() ?: "N/A")
                foodImages.add(order.foodItemImage?.first() ?: "")
            }
        }

        // Implement the Click Listener Lambda for Buy Again
        val clickListener: (Int, String, String, String) -> Unit = { position, name, price, imageUrl ->
            buyAgainItem(name, price, imageUrl)
        }

        buyAgainAdapter = BuyAgainAdapter(
            foodNames,
            foodPrices,
            foodImages,
            requireContext(),
            clickListener
        )
        binding.BuyAgainRecyclerView.adapter = buyAgainAdapter
        binding.BuyAgainRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    /**
     * Creates an Intent to redirect to PayOutActivity with the single selected item's data.
     */
    private fun buyAgainItem(name: String, price: String, image: String) {
        val intent = Intent(requireContext(), PayOutActivity::class.java).apply {
            putStringArrayListExtra("FoodItemName", arrayListOf(name))
            putStringArrayListExtra("FoodItemPrice", arrayListOf(price))
            putStringArrayListExtra("FoodItemImage", arrayListOf(image))
            putIntegerArrayListExtra("FoodItemQuantities", arrayListOf(1))
        }
        startActivity(intent)
    }
}