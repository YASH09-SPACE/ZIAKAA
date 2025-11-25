package com.example.ziakaa.Fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ziakaa.PayOutActivity
import com.example.ziakaa.adapter.CartAdapter
import com.example.ziakaa.databinding.FragmentCartBinding
import com.example.ziakaa.model.CartItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CartFragment : Fragment() {
    private lateinit var userId : String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var adapter: CartAdapter

    // NEW: Combined data list
    private lateinit var cartItemsList: MutableList<CartItems>

    // Remaining necessary list
    private lateinit var foodItemKey: MutableList<String> // Unique keys list

    private lateinit var binding: FragmentCartBinding

    private lateinit var foodNames:MutableList<String>
    private lateinit var foodPrices: MutableList<String>
    private lateinit var foodDescriptions: MutableList<String>
    private lateinit var foodImagesUri: MutableList<String>
    private lateinit var foodIngredients: MutableList<String>
    private lateinit var quantities: MutableList<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartBinding.inflate(inflater,container,false)
        auth = FirebaseAuth.getInstance()

        retrieveCartItems()

        binding.proccedButton.setOnClickListener {
            getOrderItemDetail()

        }
        return binding.root
    }

    private fun getOrderItemDetail() {
        val orderIdReference: DatabaseReference = database.reference.child("user").child(userId).child("CartItems")

        val foodName : MutableList<String> = mutableListOf<String>()
        val foodPrice : MutableList<String> = mutableListOf<String>()
        val foodImage : MutableList<String> = mutableListOf<String>()
        val foodDescription : MutableList<String> = mutableListOf<String>()
        val foodIngredient : MutableList<String> = mutableListOf<String>()
// get items Quantities
        val foodQuantities = adapter.getUpdatedItemsQuantities()

        orderIdReference.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                for (foodSnapshot in snapshot.children) {
// get the cartItems to respective List
                    val orderItems: CartItems? = foodSnapshot.getValue(CartItems::class. java)
// add items details in to list
                    orderItems?. foodName?. let { foodName.add(it) }
                    orderItems?. foodPrice ?. let { foodPrice.add(it) }
                    orderItems?.foodDescription?.let { foodDescription.add(it) }
                    orderItems?. foodImage ?. let { foodImage.add(it) }
                    orderItems?. foodIngredient ?. let { foodIngredient.add(it) }
                }
                orderNow(foodName,foodPrice,foodDescription,foodImage,foodIngredient,foodQuantities)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(),"Order Making Failed,Plz Try Later 🙄", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun orderNow(
        foodName: MutableList<String>,
        foodPrice: MutableList<String>,
        foodDescription: MutableList<String>,
        foodImage: MutableList<String>,
        foodIngredient: MutableList<String>,
        foodQuantities: MutableList<Int>
    ) {
        if (isAdded && context != null) {
            val intent = Intent(requireContext(), PayOutActivity::class.java)
            intent. putExtra (  "FoodItemName", foodName as ArrayList<String>)
            intent. putExtra(  "FoodItemPrice", foodPrice as ArrayList<String>)
            intent. putExtra(  "FoodItemImage", foodImage as ArrayList<String>)
            intent. putExtra(  "FoodItemDescription", foodDescription as ArrayList<String>)
            intent. putExtra(  "FoodItemIngredient", foodIngredient as ArrayList<String>)
            intent. putExtra( "FoodItemQuantities", foodQuantities as ArrayList<Int>)
            startActivity(intent)
        }


    }
    private fun retrieveCartItems () {
        database = FirebaseDatabase.getInstance ()
        userId = auth.currentUser?.uid ?: ""
        val foodReference : DatabaseReference = database.reference.child("user").child(userId).child("CartItems")

        // List initialization
        cartItemsList = mutableListOf() // Initialize the new list
        foodItemKey = mutableListOf()

        foodReference.addListenerForSingleValueEvent (object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (foodSnapshot in snapshot.children) {
                    // 1. Retrieve the unique key
                    foodSnapshot.key?.let{ foodItemKey.add(it) }

                    // 2. Retrieve the CartItems object
                    val cartItem = foodSnapshot.getValue(CartItems::class.java)

                    // Add the entire CartItems object to the list
                    cartItem?.let{ cartItemsList.add(it) }
                }
                setAdapter()
            }

            private fun setAdapter() {
                // The new CartAdapter now only requires the list of CartItems and the list of Keys
                adapter = CartAdapter(
                    requireContext(),
                    cartItemsList, // Pass the single list of objects
                    foodItemKey    // Pass the unique keys
                )
                binding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext(),
                    LinearLayoutManager.VERTICAL,false)
                binding.cartRecyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context,"data not fetch: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}