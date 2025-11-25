package com.example.ziakaa.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ziakaa.databinding.BuyAgainItemBinding

// Constructor updated to accept List<String> for images and a click listener lambda
class BuyAgainAdapter(
    private val buyAgainFoodName: List<String>,
    private val buyAgainFoodPrice: List<String>,
    private val buyAgainFoodImage: List<String>, // List of Strings (URLs)
    private val context: Context,
    // Lambda to handle item clicks: returns index, name, price, and image URL
    private val onItemClicked: (Int, String, String, String) -> Unit
) : RecyclerView.Adapter<BuyAgainAdapter.BuyAgainViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BuyAgainViewHolder {
        val binding = BuyAgainItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // Pass context and the click listener
        return BuyAgainViewHolder(binding, context, onItemClicked)
    }

    override fun onBindViewHolder(
        holder: BuyAgainViewHolder,
        position: Int
    ) {
        holder.bind(
            position,
            buyAgainFoodName[position],
            buyAgainFoodPrice[position],
            buyAgainFoodImage[position]
        )
    }

    override fun getItemCount(): Int = buyAgainFoodName.size

    class BuyAgainViewHolder(
        private val binding: BuyAgainItemBinding,
        private val context: Context,
        private val onItemClicked: (Int, String, String, String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        // Setup click listener on the entire item (binding.root)
        init {
            binding.root.setOnClickListener {
                onItemClicked(
                    adapterPosition,
                    binding.buyAgainFoodName.text.toString(),
                    binding.buyAgainFoodPrice.text.toString(),
                    binding.buyAgainFoodImage.tag.toString() // Get the image URL from the tag
                )
            }
        }

        fun bind(position: Int, foodName: String, foodPrice: String, foodImageUrl: String) {
            binding.buyAgainFoodName.text = foodName
            binding.buyAgainFoodPrice.text = foodPrice

            // Set the image URL as a tag on the ImageView for retrieval in the click listener
            binding.buyAgainFoodImage.tag = foodImageUrl

            // Use Glide to load the image URL (String)
            val uri = Uri.parse(foodImageUrl)
            Glide.with(context)
                .load(uri)
                .into(binding.buyAgainFoodImage)
        }
    }
}