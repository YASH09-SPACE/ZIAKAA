package com.example.ziakaa

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ziakaa.databinding.ActivityPayOutBinding
import com.example.ziakaa.model.OrderDetails
import com.example.ziakaa.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

// ⚠️ IMPORTANT: Implement PaymentResultListener interface
class PayOutActivity : AppCompatActivity(), PaymentResultListener {

    private lateinit var binding: ActivityPayOutBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userId : String

    // Class properties to hold received cart data
    private lateinit var foodItemName: ArrayList<String>
    private lateinit var foodItemPrice: ArrayList<String>
    private lateinit var foodItemImage: ArrayList<String>
    private lateinit var foodItemDescription: ArrayList<String>
    private lateinit var foodItemIngredient: ArrayList<String>
    private lateinit var foodItemQuantities: ArrayList<Int>
    private var totalAmountValue: Int = 0

    private var selectedPaymentMethod: String = "COD"

    // Properties to temporarily hold user details before placing order
    private lateinit var currentUserName: String
    private lateinit var currentUserAddress: String
    private lateinit var currentUserPhone: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayOutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Razorpay SDK
        Checkout.preload(applicationContext)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userId = auth.currentUser?.uid ?: ""

        getIntentData()
        setUserDataAndCalculateTotal()
        setupPaymentSelectionListeners()

        binding.placeMyOrder.setOnClickListener {
            val name = binding.name.text.toString().trim()
            val address = binding.address.text.toString().trim()
            val phone = binding.phone.text.toString().trim()

            if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Please fill in all your delivery details (Name, Address, Phone).", Toast.LENGTH_SHORT).show()
            } else {
                // Store current details and initiate order placement
                currentUserName = name
                currentUserAddress = address
                currentUserPhone = phone
                placeOrder(name, address, phone)
            }
        }

        binding.button4.setOnClickListener {
            finish()
        }
    }

    // --- Data Initialization and UI Setup (Unchanged from last step) ---

    private fun getIntentData() {
        foodItemName = intent.getStringArrayListExtra("FoodItemName") ?: arrayListOf()
        foodItemPrice = intent.getStringArrayListExtra("FoodItemPrice") ?: arrayListOf()
        foodItemImage = intent.getStringArrayListExtra("FoodItemImage") ?: arrayListOf()
        foodItemDescription = intent.getStringArrayListExtra("FoodItemDescription") ?: arrayListOf()
        foodItemIngredient = intent.getStringArrayListExtra("FoodItemIngredient") ?: arrayListOf()
        foodItemQuantities = intent.getIntegerArrayListExtra("FoodItemQuantities") ?: arrayListOf()
    }

    private fun setUserDataAndCalculateTotal() {
        var total = 0
        for (i in 0 until foodItemPrice.size) {
            val price = foodItemPrice[i].filter { it.isDigit() }.toIntOrNull() ?: 0
            val quantity = foodItemQuantities[i]
            total += (price * quantity)
        }
        totalAmountValue = total

        binding.totalAmount.setText("₹${totalAmountValue}")

        val userReference: DatabaseReference = database.reference.child("user").child(userId)
        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserModel::class.java)
                user?.let {
                    binding.name.setText(it.name)
                    binding.address.setText(it.address)
                    binding.phone.setText(it.phone)
                }
            }
            override fun onCancelled(error: DatabaseError) { /* Handle error */ }
        })
    }

    private fun setupPaymentSelectionListeners() {
        // Read the initial state (COD is checked in XML by default)
        when (binding.paymentMethodGroup.checkedRadioButtonId) {
            R.id.rb_cod -> selectedPaymentMethod = "COD"
            R.id.rb_net_banking -> selectedPaymentMethod = "NET_BANKING"
        }

        binding.paymentMethodGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rb_cod -> selectedPaymentMethod = "COD"
                R.id.rb_net_banking -> selectedPaymentMethod = "NET_BANKING"
            }
        }
    }

    // --- Order Placement Logic ---

    private fun placeOrder(userName: String, userAddress: String, userPhone: String) {

        when (selectedPaymentMethod) {
            "COD" -> {
                // COD: Payment is false, order is placed immediately
                saveOrderToFirebase(userName, userAddress, userPhone, isPaymentSuccessful = false)
            }
            "NET_BANKING" -> {
                // Net Banking: Initiate Razorpay transaction
                startRazorpayPayment(userName, userPhone)
            }
        }
    }

    /**
     * Initiates the Razorpay Payment Dialog.
     */
    private fun startRazorpayPayment(name: String, phone: String) {
        val checkout = Checkout()
        // ⚠️ IMPORTANT: Set your official Razorpay Key ID here
        checkout.setKeyID("rzp_test_RjfdCf4GPTx0oo")

        try {
            val options = JSONObject()
            options.put("name", "Ziakaa Food App")
            options.put("description", "Order Payment")
            options.put("currency", "INR")
            // Amount is in paisa (1 INR = 100 paisa). Convert totalAmountValue from INR to paisa.
            options.put("amount", totalAmountValue * 100)

            val prefill = JSONObject()
            prefill.put("email", auth.currentUser?.email)
            prefill.put("contact", phone)
            options.put("prefill", prefill)

            checkout.open(this, options)

        } catch (e: Exception) {
            Toast.makeText(this, "Error in payment: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    // --- Razorpay Listener Implementation ---

    /**
     * Callback for successful Razorpay payment.
     */
    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        // The payment was successful. Now save the order with paymentSuccessful = true.
        Toast.makeText(this, "Payment Successful: $razorpayPaymentID", Toast.LENGTH_LONG).show()

        // Use the temporarily stored user details
        saveOrderToFirebase(currentUserName, currentUserAddress, currentUserPhone, isPaymentSuccessful = true)
    }

    /**
     * Callback for failed Razorpay payment.
     */
    override fun onPaymentError(code: Int, response: String?) {
        // Payment failed. Do not save the order yet.
        Toast.makeText(this, "Payment Failed: $response", Toast.LENGTH_LONG).show()
    }

    // --- Firebase Logic (Updated to use temp properties) ---

    private fun saveOrderToFirebase(userName: String, userAddress: String, userPhone: String, isPaymentSuccessful: Boolean) {

        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val orderPushKey = database.reference.child("OrderDetails").push().key ?: time

        val orderDetails = OrderDetails(
            userId,
            userName,
            userAddress,
            userPhone,
            foodItemName,
            foodItemPrice,
            foodItemImage,
            foodItemQuantities,
            orderPushKey,
            time,
            totalAmountValue.toString(),
            false,
            isPaymentSuccessful
        )

        // 1. Save the order to Firebase
        database.reference.child("OrderDetails").child(orderPushKey).setValue(orderDetails)
            .addOnSuccessListener {
                // 2. Clear the user's cart
                clearUserCart(isPaymentSuccessful)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Order placement failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearUserCart(isPaymentSuccessful: Boolean) {
        val cartItemsReference = database.reference.child("user").child(userId).child("CartItems")
        cartItemsReference.removeValue()
            .addOnSuccessListener {
                val successMessage = if (isPaymentSuccessful) "Payment Successful & Order Placed! 🎉" else "Order Placed (COD)! 🚚"
                Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show()

                try {
                    val bottomSheetDialog = congrates_bottom_sheet()
                    bottomSheetDialog.show(supportFragmentManager,"CongratsDialog")
                } catch (e: Exception) {
                    Toast.makeText(this, "Error showing congrats screen.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Order Placed but failed to clear cart.", Toast.LENGTH_SHORT).show()
            }
    }
}