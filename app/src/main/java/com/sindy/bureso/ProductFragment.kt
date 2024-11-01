package com.sindy.bureso

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.sindy.bureso.databinding.FragmentProductBinding

class ProductFragment : Fragment() {

    private var _binding: FragmentProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: FirebaseDatabase
    private lateinit var productsRef: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private lateinit var adapter: ProductAdapter
    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance("https://bureso-e6c7e-default-rtdb.asia-southeast1.firebasedatabase.app/")
        productsRef = database.getReference("products")
        storage = FirebaseStorage.getInstance()

        setupRecyclerView()
        setupListeners()
        loadProducts()
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(mutableListOf(),
            onEditClick = { product -> fillFieldsForEdit(product) },
            onDeleteClick = { product -> deleteProduct(product) }
        )
        binding.rvProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProducts.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnSimpanE.setOnClickListener { saveProduct() }
        binding.btnBatalE.setOnClickListener { resetFields() }
        binding.imvFotoProdukE.setOnClickListener { openImageChooser() }
    }

    private fun loadProducts() {
        productsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = mutableListOf<Product>()
                for (productSnapshot in snapshot.children) {
                    try {
                        val product = productSnapshot.getValue(Product::class.java)
                        product?.let {
                            products.add(it)
                            Log.d("ProductFragment", "Loaded product: ${it.namaProduk}")
                        }
                    } catch (e: Exception) {
                        Log.e("ProductFragment", "Error parsing product: ${productSnapshot.key}", e)
                        // If parsing as Product fails, try to retrieve individual fields
                        val namaProduk = productSnapshot.child("namaProduk").getValue(String::class.java) ?: ""
                        val hargaProduk = productSnapshot.child("hargaProduk").getValue(String::class.java) ?: ""
                        val deskripsiProduk = productSnapshot.child("deskripsiProduk").getValue(String::class.java) ?: ""
                        val fotoProduk = productSnapshot.child("fotoProduk").getValue(String::class.java) ?: ""

                        val product = Product(
                            kodeProduk = productSnapshot.key ?: "",
                            namaProduk = namaProduk,
                            hargaProduk = hargaProduk,
                            deskripsiProduk = deskripsiProduk,
                            fotoProduk = fotoProduk
                        )
                        products.add(product)
                        Log.d("ProductFragment", "Manually created product: ${product.namaProduk}")
                    }
                }
                adapter.updateProducts(products)
                Log.d("ProductFragment", "Total products loaded: ${products.size}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProductFragment", "Failed to load products", error.toException())
                Toast.makeText(requireContext(), "Failed to load products: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveProduct() {
        val kodeProduk = binding.txKodeProdukE.text.toString().trim()
        val namaProduk = binding.edNamaProduk.text.toString().trim()
        val hargaProduk = binding.edHarga.text.toString().trim()
        val deskripsiProduk = binding.edDeskripsi.text.toString().trim()

        if (namaProduk.isEmpty() || hargaProduk.isEmpty() || deskripsiProduk.isEmpty()) {
            Toast.makeText(requireContext(), "Semua Harus Diisi", Toast.LENGTH_SHORT).show()
            return
        }

        val product = Product(kodeProduk, namaProduk, hargaProduk, deskripsiProduk, "")

        if (imageUri != null) {
            uploadImage(product)
        } else {
            saveProductToDatabase(product)
        }
    }

    private fun uploadImage(product: Product) {
        val fileReference = storage.reference.child("produk/${System.currentTimeMillis()}_${imageUri?.lastPathSegment}")
        fileReference.putFile(imageUri!!)
            .addOnSuccessListener {
                fileReference.downloadUrl.addOnSuccessListener { uri ->
                    product.fotoProduk = uri.toString()
                    saveProductToDatabase(product)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal Mengupload: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProductToDatabase(product: Product) {
        val newProductRef = productsRef.push()
        product.kodeProduk = newProductRef.key ?: ""
        newProductRef.setValue(product)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Berhasil Menambahkan Produk", Toast.LENGTH_SHORT).show()
                resetFields()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to save product: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteProduct(product: Product) {
        productsRef.child(product.kodeProduk).removeValue()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Berhasil menghapus produk", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to delete product: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fillFieldsForEdit(product: Product) {
        binding.txKodeProdukE.text = product.kodeProduk
        binding.edNamaProduk.setText(product.namaProduk)
        binding.edHarga.setText(product.hargaProduk)
        binding.edDeskripsi.setText(product.deskripsiProduk)
        // You might want to load the image using Glide or Picasso here
        // For example:
        // Glide.with(requireContext()).load(product.fotoProduk).into(binding.imvFotoProdukE)
    }

    private fun resetFields() {
        binding.txKodeProdukE.text = ""
        binding.edNamaProduk.text?.clear()
        binding.edHarga.text?.clear()
        binding.edDeskripsi.text?.clear()
        binding.imvFotoProdukE.setImageResource(R.drawable.image)
        imageUri = null
    }

    private fun openImageChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            binding.imvFotoProdukE.setImageURI(imageUri)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
