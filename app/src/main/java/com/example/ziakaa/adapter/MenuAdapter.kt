    package com.example.ziakaa.adapter

    import android.content.Context
    import android.content.DialogInterface
    import android.content.Intent
    import android.media.Image
    import android.net.Uri
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import androidx.recyclerview.widget.RecyclerView
    import com.bumptech.glide.Glide
    import com.example.ziakaa.DetailsActivity
    import com.example.ziakaa.databinding.ManuItemBinding
    import com.example.ziakaa.model.MenuItem

    class MenuAdapter(private val menuItems:List<MenuItem>, private val requireContext: Context): RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {
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

        override fun getItemCount(): Int = menuItems.size

        inner class MenuViewHolder(private val binding: ManuItemBinding): RecyclerView.ViewHolder(binding.root) {
            init {
                binding.root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        openDetailsActivity(position)
                    }
                }
            }

            private fun openDetailsActivity(position: Int) {
                val menuItem = menuItems[position]
                // a intent to open details activity and pass data
                val intent = Intent(requireContext, DetailsActivity::class.java).apply{
                    putExtra("MenuItemName", menuItem. foodName)
                    putExtra("MenuItemImage", menuItem. foodImage)
                    putExtra("MenuItemDescription", menuItem. foodDescription)
                    putExtra("MenuItemIngredients", menuItem. foodIngredient)
                    putExtra("MenuItemPrice", menuItem. foodPrice)
                }
                // start the details activity
                requireContext.startActivity(intent)
            }

            fun bind(position: Int) {
                val menuItem = menuItems[position]
                binding.apply {
                    menufoodName.text = menuItem.foodName
                    menuPrice.text = menuItem.foodPrice
                    val uri = Uri.parse(menuItem.foodImage)
                    Glide.with(requireContext).load(uri).into(menuimageView)
                }
            }

        }
    }





