package com.tigcal.samples.restosearch

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tigcal.samples.restosearch.model.MenuItem

class MenuItemAdapter(private val context: Context): RecyclerView.Adapter<MenuItemAdapter.ViewHolder>() {
    private var menuItems = mutableListOf<MenuItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_menu_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = menuItems.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menuItem = menuItems[position]

        with(menuItem) {
            holder.nameText.text = name
            holder.calorieText.text = calories

            Glide.with(context)
                .load(photo.thumb)
                .placeholder(R.mipmap.ic_launcher)
                .fitCenter()
                .into(holder.imageView)
        }
    }

    fun setMenuItems(menuItems: List<MenuItem>) {
        this.menuItems.clear()
        this.menuItems.addAll(menuItems)
        notifyItemInserted(menuItems.size)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.icon)
        val nameText: TextView = itemView.findViewById(R.id.name_text)
        val calorieText: TextView = itemView.findViewById(R.id.calorie_text)
    }
}