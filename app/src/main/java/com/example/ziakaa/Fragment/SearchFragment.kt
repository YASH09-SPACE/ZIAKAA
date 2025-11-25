package com.example.ziakaa.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.SearchView // Using standard Android SearchView to match your XML
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ziakaa.adapter.MenuAdapter
import com.example.ziakaa.databinding.FragmentSearchBinding
import com.example.ziakaa.model.MenuItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding

    // Store ALL data fetched from Firebase
    private var originalMenuItemList: MutableList<MenuItem> = mutableListOf()
    // Store filtered data for the RecyclerView adapter
    private var filteredMenuItemList: MutableList<MenuItem> = mutableListOf()

    private lateinit var adapter: MenuAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var menuReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()
        menuReference = database.reference.child("Menu") // Use "Menu" as per previous agreement

        // Initialize Adapter (with empty list initially)
        adapter = MenuAdapter(filteredMenuItemList, requireContext())
        binding.menuRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.menuRecyclerView.adapter = adapter

        // Fetch data
        retrieveMenuData()

        setupSearchView()

        return binding.root
    }

    /**
     * Fetches all menu items from Firebase (adapted from your BottomSheetFragment logic).
     */
    private fun retrieveMenuData() {
        // Use the "menu" node name from your BottomSheet code, though "Menu" was used before.
        // I will use "Menu" for consistency with the last complete code, but you can change it back if needed.
        val foodRef: DatabaseReference = database.reference.child("menu")
        originalMenuItemList.clear() // Clear any old data

        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (foodSnapshot in snapshot.children) {
                    val menuItem = foodSnapshot.getValue(MenuItem::class.java)
                    menuItem?.let {
                        originalMenuItemList.add(it) // Add the entire MenuItem object
                    }
                }

                // Once data is loaded, display all of it initially
                showAllMenu()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load menu data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    /**
     * Populates the filtered list with all original data and notifies the adapter.
     */
    private fun showAllMenu() {
        filteredMenuItemList.clear()
        filteredMenuItemList.addAll(originalMenuItemList)
        adapter.notifyDataSetChanged()
    }

    /**
     * Sets up the SearchView listener for real-time searching.
     */
    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    filterMenuItems(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    filterMenuItems(newText)
                }
                return true
            }
        })
    }

    /**
     * Filters the original list based on the query and updates the adapter.
     */
    private fun filterMenuItems(query: String) {
        filteredMenuItemList.clear()

        val searchLower = query.lowercase()

        if (originalMenuItemList.isNotEmpty()) {
            // Filter the list directly by checking the foodName
            val results = originalMenuItemList.filter { menuItem ->
                // Check if the food name contains the query (case-insensitive)
                menuItem.foodName?.lowercase()?.contains(searchLower) == true
            }
            filteredMenuItemList.addAll(results)
        }

        adapter.notifyDataSetChanged()
    }
}