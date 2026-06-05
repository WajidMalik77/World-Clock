package com.worldclock.app_themes.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.worldclock.app_themes.database.WidgetClockItem
import com.worldclock.app_themes.database.WorldClockItem
import com.worldclock.app_themes.databinding.ItemWorldClockBinding
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import com.worldclock.app_themes.R
import java.time.ZoneId

class WorldClockAdapter(
    val callbacks: (WorldClockItem) -> Unit,
    private val onLongClick: (WorldClockItem) -> Unit,
) :
    RecyclerView.Adapter<WorldClockAdapter.ViewHolder>() {
    private var list: List<WorldClockItem> = emptyList()
    fun updateTime() {
        notifyDataSetChanged()
    }

    fun updateList(newList: List<WorldClockItem>) {
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
        val formattedTime = currentTime.format(DateTimeFormatter.ofPattern("hh:mm"))
        holder.binding.timeText.text = formattedTime

        val hour = currentTime.hour
        if (hour in 6..17) {
            holder.binding.dayImg.setImageResource(R.drawable.ic_sun)
        } else {
            holder.binding.dayImg.setImageResource(R.drawable.ic_moon)
        }

        val localZoneId = ZoneId.systemDefault().id
        val isLocalZone = item.timeZoneId == localZoneId
        val cornerRadius =
            holder.itemView.context.resources.getDimension(com.intuit.sdp.R.dimen._14sdp)

        if (isLocalZone) {
            val gradient = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.parseColor("#7441D0"), Color.parseColor("#EF4A9A"))
            ).apply { this.cornerRadius = cornerRadius }
            holder.binding.del.background = gradient

            holder.binding.cityText.setTextColor(Color.WHITE)
            holder.binding.countryText.setTextColor(Color.WHITE)
            holder.binding.timeText.setTextColor(Color.WHITE)
            holder.binding.flagText.setTextColor(Color.WHITE)
            holder.binding.delImg.setColorFilter(Color.WHITE)
        } else {
            holder.binding.del.setBackgroundColor(Color.parseColor("#f5f5f5"))
            holder.binding.cityText.setTextColor(Color.parseColor("#606060"))
            holder.binding.countryText.setTextColor(Color.BLACK)
            holder.binding.timeText.setTextColor(Color.BLACK)
            holder.binding.flagText.setTextColor(Color.BLACK)
            holder.binding.delImg.clearColorFilter()
        }

        holder.itemView.setOnClickListener {
            item.isSelected = !item.isSelected
            notifyItemChanged(position)
            callbacks.invoke(item)
        }
        holder.binding.delImg.setOnClickListener { onLongClick(item) }
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
