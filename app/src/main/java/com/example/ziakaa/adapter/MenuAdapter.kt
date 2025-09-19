package com.example.ziakaa.adapter

import android.media.Image
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ziakaa.databinding.ManuItemBinding

class MenuAdapter(private val menuItemsName: MutableList<String>,private val menuItemPrice: MutableList<String>,private val menuImage: MutableList<Int>): RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MenuViewHolder {
        val binding = ManuItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: MenuViewHolder,
        position: Int
    ) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = menuItemsName.size

    inner class MenuViewHolder(private val binding: ManuItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            binding.apply {
                menufoodName.text = menuItemsName[position]
                menuPrice.text = menuItemPrice[position]
                menuimageView.setImageResource(menuImage[position])
            }
        }

    }
}