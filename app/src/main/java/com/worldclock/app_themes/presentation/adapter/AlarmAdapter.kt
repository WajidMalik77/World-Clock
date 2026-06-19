package com.worldclock.app_themes.presentation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.worldclock.app_themes.R
import com.worldclock.app_themes.data.database.AlarmEntity
import com.worldclock.app_themes.databinding.ItemAlarmBinding
import com.worldclock.app_themes.core.utils.getTimeUntilAlarm


class AlarmAdapter(
    val callbacks: (AlarmEntity) -> Unit,
    private val onLongClick: (AlarmEntity) -> Unit,
    private val onEditClick: (AlarmEntity) -> Unit,
) :
    RecyclerView.Adapter<AlarmAdapter.ViewHolder>() {
    private var list: List<AlarmEntity> = emptyList()


    fun updateList(newList: List<AlarmEntity>) {
        list = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemAlarmBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemAlarmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    // Function to convert 24-hour time to 12-hour with AM/PM
    @SuppressLint("DefaultLocale")
    fun formatTo12Hour(hour: Int, minute: Int): Pair<String, String> {
        val amPm = if (hour >= 12) "PM" else "AM"
        val hour12 = if (hour % 12 == 0) 12 else hour % 12
        return Pair(String.format("%02d:%02d ", hour12, minute),amPm)
        String.format("%02d:%02d %s", hour12, minute, amPm)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        if (item.isEnabled)
            holder.binding.radioImg.setImageResource(R.drawable.alarm_selected)
        else holder.binding.radioImg.setImageResource(R.drawable.alarm_unselected)
//        val formattedTime = "${formatTo12Hour(item.hour, item.minute).second} \n ${formatTo12Hour(item.hour, item.minute).first}"

        holder.binding.flagText.text = formatTo12Hour(item.hour, item.minute).first
        holder.binding.amPm.text = formatTo12Hour(item.hour, item.minute).second
        holder.binding.cityText.text =
            if (item.repeatDays.isNotEmpty()) item.repeatDays else holder.itemView.context.getString(
                R.string.once
            )

        holder.binding.countryText.text = holder.itemView.context.getTimeUntilAlarm(item.hour, item.minute, item.repeatDays)

        holder.binding.radioImg.setOnClickListener {
            item.isEnabled = !item.isEnabled
            notifyItemChanged(position)
            callbacks.invoke(item)
        }
        holder.itemView.setOnClickListener {
            onEditClick.invoke(item)
        }
        holder.binding.delImg.setOnClickListener {
            onLongClick(item)
        }
    }

    override fun getItemCount() = list.size


}
