package com.iammert.tabscrollattacher

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iammert.tabscrollattacher.data.Item

class ItemListAdapter : RecyclerView.Adapter<ItemListAdapter.ItemViewHolder>() {

    private val itemList = arrayListOf<Item>()

    fun setItems(itemList: List<Item>) {
        this.itemList.clear()
        this.itemList.addAll(itemList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        ItemViewHolder.create(parent)

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.tv).setText(position.toString())
    }

    override fun onViewRecycled(holder: ItemViewHolder) {
        super.onViewRecycled(holder)
        Log.d("Hehe", "Recycling Akak")
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        companion object {

            fun create(parent: ViewGroup): ItemViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.ir, parent, false)
                return ItemViewHolder(view)
            }
        }
    }
}