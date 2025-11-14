package com.example.ziakaa.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ziakaa.R
import com.example.ziakaa.adapter.MenuAdapter
import com.example.ziakaa.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {
    private lateinit var adapter: MenuAdapter
    private lateinit var binding: FragmentSearchBinding
    private val originalMenuFoodName = listOf("Burger","sandWhich","Momo","Item","SandWhich","Momo","sandWhich","Momo","Item","SandWhich","Momo")
    val originalMenuItemPrice = listOf("$5","$6","$8","$7","$9","$10","$6","$8","$7","$9","$10")
    val originalMenuImage = listOf(
        R.drawable.menu1,
        R.drawable.menu2,
        R.drawable.menu3,
        R.drawable.menu4,
        R.drawable.menu5,
        R.drawable.menu6,
        R.drawable.menu2,
        R.drawable.menu3,
        R.drawable.menu4,
        R.drawable.menu5,
        R.drawable.menu6,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    private val filterdMenuFoodName = mutableListOf<String>()
    private val filterdMenuItemPrice = mutableListOf<String>()
    private val filterdMenuImage = mutableListOf<Int>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater,container,false)
        adapter = MenuAdapter(filterdMenuFoodName,filterdMenuItemPrice,filterdMenuImage,requireContext())
        binding.menuRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.menuRecyclerView.adapter = adapter
        // Inflate the layout for this fragment

        setupSearchView()
        showAllMenu()
        return binding.root
    }

    private fun showAllMenu() {
        filterdMenuFoodName.clear()
        filterdMenuItemPrice.clear()
        filterdMenuImage.clear()

        filterdMenuFoodName.addAll(originalMenuFoodName)
        filterdMenuItemPrice.addAll(originalMenuItemPrice)
        filterdMenuImage.addAll(originalMenuImage)

        adapter.notifyDataSetChanged()
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filterMenuItems(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterMenuItems(newText)
                return true
            }
        })
    }

    private fun filterMenuItems(query: String) {
        filterdMenuFoodName.clear()
        filterdMenuItemPrice.clear()
        filterdMenuImage.clear()

        originalMenuFoodName.forEachIndexed { index, foodName ->
            if (foodName.contains(query, ignoreCase = true)){
                filterdMenuFoodName.add(foodName)
                filterdMenuItemPrice.add(originalMenuItemPrice[index])
                filterdMenuImage.add(originalMenuImage[index])
            }
        }
        adapter.notifyDataSetChanged()
    }

    companion object {

    }
}

