package com.worldclock.app_themes.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.worldclock.app_themes.R
import com.worldclock.app_themes.database.WidgetClockItem
import com.worldclock.app_themes.database.WorldClockItem
import com.worldclock.app_themes.databinding.ItemAddClockBinding
import com.worldclock.app_themes.databinding.ItemWorldClockBinding
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class AddWidgetsClockAdapter(val callbacks: (WidgetClockItem) -> Unit) :
    RecyclerView.Adapter<AddWidgetsClockAdapter.ViewHolder>() {
    private var list: List<WidgetClockItem> = emptyList()
    fun updateTime() {
        notifyDataSetChanged()
    }

    fun updateList(newList: List<WidgetClockItem>) {
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
          else  holder.binding.radioImg.setImageResource(R.drawable.radio_unselected)

        holder.binding.flagText.text = item.flag
        holder.binding.cityText.text = item.city
        holder.binding.countryText.text = item.country
//        val zone = ZoneId.of(item.timeZoneId)
//        val currentTime = ZonedDateTime.now(zone)
//        val formattedTime = currentTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
//        holder.binding.timeText.text = formattedTime
//
//        holder.binding.timeDifference.text = item.relation
        holder.itemView.setOnClickListener {
            item.isSelected = !item.isSelected
            notifyItemChanged(position)
            callbacks.invoke(item)
        }
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
