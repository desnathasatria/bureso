package com.sindy.bureso

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso

class CustomAdapter(
    val context: Context,
    private val productList: ArrayList<HashMap<String, Any>>
) : BaseAdapter() {
    private val P_CODE = "product_code"
    private val P_NAME = "product_name"
    private val P_PRICE = "product_price"
    private val P_DESC = "product_description"
    private val P_IMAGE_URL = "product_image_url"

    override fun getCount(): Int {
        return productList.size
    }

    override fun getItem(position: Int): Any {
        return productList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var holder: ViewHolder
        var view = convertView

        if (view == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(R.layout.fragment_product, parent, false)

            holder = ViewHolder()
            holder.txKodeProduk = view!!.findViewById(R.id.txKodeProdukE)
            holder.txNamaProduk = view.findViewById(R.id.edNamaProduk)
            holder.txHarga = view.findViewById(R.id.edHarga)
            holder.txDeskripsi = view.findViewById(R.id.edDeskripsi)
            holder.imvFotoProduk = view.findViewById(R.id.imvFotoProdukE)

            view.tag = holder
        } else {
            holder = view!!.tag as ViewHolder
        }

        val product = productList[position]
        val productImageUrl = product[P_IMAGE_URL].toString()

        holder.txKodeProduk?.text = product[P_CODE].toString()
        holder.txNamaProduk?.text = product[P_NAME].toString()
        holder.txHarga?.text = product[P_PRICE].toString()
        holder.txDeskripsi?.text = product[P_DESC].toString()

        // Load image using Picasso from URL
        Picasso.get().load(Uri.parse(productImageUrl)).into(holder.imvFotoProduk)

        return view!!
    }

    private class ViewHolder {
        var txKodeProduk: TextView? = null
        var txNamaProduk: TextView? = null
        var txHarga: TextView? = null
        var txDeskripsi: TextView? = null
        var imvFotoProduk: ImageView? = null
    }
}
