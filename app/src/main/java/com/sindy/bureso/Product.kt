package com.sindy.bureso

data class Product(
    var kodeProduk: String,  // Ubah ini dari val menjadi var
    val namaProduk: String,
    val hargaProduk: String,
    val deskripsiProduk: String,
    var fotoProduk: String  // Jika fotoProduk juga akan diubah, gunakan var
)
