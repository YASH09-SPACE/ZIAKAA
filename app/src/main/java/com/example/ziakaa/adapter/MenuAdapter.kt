package com.example.ziakaa.adapter

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ziakaa.DetailsActivity
import com.example.ziakaa.databinding.ManuItemBinding

class MenuAdapter(private val menuItemsName: MutableList<String>,private val menuItemPrice: MutableList<String>,private val menuImage: MutableList<Int>,private val requireContext: Context): RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {
    private val itemClickListener : View.OnClickListener?= null
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
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION){
                    itemClickListener?.onItemClick(position)
                }
                val intent = Intent(requireContext, DetailsActivity::class.java)
                intent.putExtra("MenuItemName",menuItemsName.get(position))
                intent.putExtra("MenuItemImage",menuImage.get(position))
                requireContext.startActivity(intent)
            }
        }
        fun bind(position: Int) {
            binding.apply {
                menufoodName.text = menuItemsName[position]
                menuPrice.text = menuItemPrice[position]
                menuimageView.setImageResource(menuImage[position])


            }
        }

    }
}

private fun View.OnClickListener?.onItemClick(position: Int) {}




