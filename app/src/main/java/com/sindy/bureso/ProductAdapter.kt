package com.sindy.bureso

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sindy.bureso.databinding.ItemProductBinding

class ProductAdapter(
    private var products: MutableList<Product>,
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    fun updateProducts(newProducts: List<Product>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount() = products.size

    inner class ProductViewHolder(private val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.apply {
                tvProductName.text = product.namaProduk
                tvProductDescription.text = product.deskripsiProduk
                tvProductPrice.text = product.hargaProduk

                // Load image using Glide
                Glide.with(itemView.context)
                    .load(product.fotoProduk)
                    .placeholder(R.drawable.image) // Use your placeholder image
                    .error(R.drawable.image) // Use your error image
                    .into(ivProductImage)

                btnEdit.setOnClickListener { onEditClick(product) }
                btnDelete.setOnClickListener { onDeleteClick(product) }
            }
        }
    }
}