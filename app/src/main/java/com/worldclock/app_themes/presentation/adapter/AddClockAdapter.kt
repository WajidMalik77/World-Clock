package com.worldclock.app_themes.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.worldclock.app_themes.R
import com.worldclock.app_themes.data.database.WorldClockItem
import com.worldclock.app_themes.databinding.ItemAddClockBinding


class AddClockAdapter(val callbacks: (WorldClockItem) -> Unit) :
    RecyclerView.Adapter<AddClockAdapter.ViewHolder>() {
    private var list: List<WorldClockItem> = emptyList()
    fun updateTime() {
        notifyDataSetChanged()
    }

    fun updateList(newList: List<WorldClockItem>) {
        list = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemAddClockBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemAddClockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        if (item.isSelected)
            holder.binding.radioImg.setImageResource(R.drawable.radio_selected)
        else
            holder.binding.radioImg.setImageResource(R.drawable.clock_unselected)

        holder.binding.flagText.text = item.flag
        holder.binding.cityText.text = item.city
        holder.binding.countryText.text = item.country

        holder.itemView.setOnClickListener {
            item.isSelected = !item.isSelected
            notifyItemChanged(position)
        }
    }

    fun getSelectedItems(): List<WorldClockItem> {
        return list.filter { it.isSelected }
    }

    override fun getItemCount() = list.size

    fun filter(query: String) {
        list = list.filter {
            it.city.contains(query, ignoreCase = true) ||
                    it.country.contains(query, ignoreCase = true)
        }
        notifyDataSetChanged()
    }
}
