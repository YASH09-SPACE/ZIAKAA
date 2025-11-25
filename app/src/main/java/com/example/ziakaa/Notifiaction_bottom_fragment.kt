package com.example.ziakaa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ziakaa.adapter.NotificationAdapter
import com.example.ziakaa.databinding.FragmentNotifiactionBottomFragmentBinding
import com.example.ziakaa.model.OrderDetails // OrderDetails model required
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Notifiaction_bottom_fragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentNotifiactionBottomFragmentBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    // List to hold the converted notification data (Message, ImageId)
    private val notificationsData: MutableList<Pair<String, Int>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNotifiactionBottomFragmentBinding.inflate(inflater, container, false)

        retrieveNotificationsFromOrders()

        return binding.root
    }

    private fun retrieveNotificationsFromOrders() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val orderReference = database.reference.child("OrderDetails")

        orderReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                notificationsData.clear()

                for (orderSnapshot in snapshot.children) {
                    val order = orderSnapshot.getValue(OrderDetails::class.java)

                    if (order?.userUid == userId) {
                        // 1. Determine a simplified status based on order fields
                        val status = getOrderStatus(order)

                        // 2. Create the notification message
                        val message = createNotificationMessage(order, status)

                        // 3. Map status to a local icon
                        val imageId = getNotificationImageId(status)

                        notificationsData.add(Pair(message, imageId))
                    }
                }

                // Show newest notifications at the top
                notificationsData.reverse()
                setupRecyclerView()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load order history: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /**
     * Simplifies the OrderDetails into a single status string for display.
     * Uses safe calls on nullable boolean fields.
     */
    private fun getOrderStatus(order: OrderDetails): String {
        return when {
            // Check if paymentSuccessful is explicitly true (using ?: false for safety)
            order.paymentSuccessful ?: false -> "PLACED"

            // Check if isAccepted is explicitly true (using ?: false for safety)
            order.isAccepted ?: false -> "ACCEPTED"

            // Default status
            else -> "PLACED"
        }
    }

    /**
     * Creates a user-friendly message for the notification list.
     * Uses safe calls and Elvis operator (?:) for nullable lists.
     */
    private fun createNotificationMessage(order: OrderDetails, status: String): String {
        // Safely access the first item name or use "An Item" as placeholder
        val itemName = order.foodItemName?.firstOrNull() ?: "An Item"

        // Safely get the size of the food list (use 1 if the list is null/empty, as we found at least one item name above)
        val itemCount = order.foodItemName?.size ?: 1

        return when (status) {
            "PLACED" -> "Congrats! Your order for $itemName and ${itemCount - 1} more items was successfully placed."
            "ACCEPTED" -> "Your order is being processed by the restaurant."
            else -> "Status update available for your recent order."
        }
    }

    /**
     * Helper function to map order status strings to local image resources.
     */
    private fun getNotificationImageId(status: String): Int {
        // The mapping itself is safe as long as the R.drawable resources exist
        return when (status.uppercase()) {
            "ACCEPTED" -> R.drawable.truck
            "PLACED" -> R.drawable.congrats
            else -> R.drawable.congrats
        }
    }

    private fun setupRecyclerView() {
        // Ensure that map operations handle potential nulls if they were allowed in notificationsData
        val messages = ArrayList(notificationsData.map { it.first ?: "Unknown Status" })
        val images = ArrayList(notificationsData.map { it.second ?: R.drawable.congrats }) // Default icon if imageId is null

        val adapter = NotificationAdapter(messages, images)
        binding.notificationRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.notificationRecyclerview.adapter = adapter
    }

    companion object {
        const val TAG = "NotificationBottomSheet"
    }
}