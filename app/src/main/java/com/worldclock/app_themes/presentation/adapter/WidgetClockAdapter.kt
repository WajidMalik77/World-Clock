package com.worldclock.app_themes.presentation.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.worldclock.app_themes.data.database.WidgetClockItem
import com.worldclock.app_themes.databinding.ItemWorldClockBinding
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


class WidgetClockAdapter(
    val callbacks: (WidgetClockItem) -> Unit,
    private val onLongClick: (WidgetClockItem) -> Unit,
) :
    RecyclerView.Adapter<WidgetClockAdapter.ViewHolder>() {
    private var list: List<WidgetClockItem> = emptyList()
    fun updateTime() {
        notifyDataSetChanged()
    }

    fun updateList(newList: List<WidgetClockItem>) {
        list = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemWorldClockBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemWorldClockBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.binding.flagText.text = item.flag
        holder.binding.cityText.text = item.city
        holder.binding.countryText.text = item.country
        val zone = ZoneId.of(item.timeZoneId)
        val currentTime = ZonedDateTime.now(zone)
        val formattedTime = currentTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
        holder.binding.timeText.text = formattedTime

        holder.itemView.setOnClickListener {
            item.isSelected = !item.isSelected
            notifyItemChanged(position)
            callbacks.invoke(item)
        }
        holder.binding.delImg.setOnClickListener {
            Log.d("TAG", "onBindViewHolder: onLongClick")
            onLongClick(item)
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
