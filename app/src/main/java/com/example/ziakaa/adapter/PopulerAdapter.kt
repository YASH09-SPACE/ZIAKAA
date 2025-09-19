package com.example.ziakaa.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ziakaa.databinding.PopularItemBinding

class PopulerAdapter(private val items: List<String>, private val price:List<String>, private val image: List<Int>): RecyclerView.Adapter<PopulerAdapter.PopulerViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PopulerViewHolder {
        return PopulerViewHolder(PopularItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    }

    override fun onBindViewHolder(
        holder: PopulerViewHolder,
        position: Int
    ) {
       val item = items[position]
        val images = image[position]
        val price = price[position]
        holder.bind(item,images,price)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class PopulerViewHolder(private  val binding: PopularItemBinding) : RecyclerView.ViewHolder(binding.root){
        private val imageView = binding.imageView5
        fun bind(item: String, images: Int, price: String) {
            binding.foodNmaePopuler.text = item
            binding.PricePopuler.text = price
            imageView.setImageResource(images)

        }

    }
}