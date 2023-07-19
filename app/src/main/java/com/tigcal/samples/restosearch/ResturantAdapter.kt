package com.tigcal.samples.restosearch

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tigcal.samples.restosearch.model.Restaurant

class ResturantAdapter(private val context: Context): RecyclerView.Adapter<ResturantAdapter.ViewHolder>() {
    private var restaurants = mutableListOf<Restaurant>()

    var onClickListener: (Restaurant) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_restaurant, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = restaurants.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val resto = restaurants[position]

        with(resto) {
            holder.nameText.text = name
            holder.addressText.text = vicinity
            holder.ratingText.text = rating.toString()

            Glide.with(context)
                .load(icon)
                .placeholder(R.mipmap.ic_launcher)
                .fitCenter()
                .into(holder.imageView)
        }
        holder.itemView.setOnClickListener {
            onClickListener(resto)
        }
    }

    fun setRestaurants(restos: List<Restaurant>) {
        this.restaurants.clear()
        this.restaurants.addAll(restos)
        notifyItemInserted(restos.size)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.icon)
        val nameText: TextView = itemView.findViewById(R.id.name_text)
        val addressText: TextView = itemView.findViewById(R.id.address_text)
        val ratingText: TextView = itemView.findViewById(R.id.rating_text)
    }
}