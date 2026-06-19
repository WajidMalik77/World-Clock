package com.worldclock.app_themes.presentation.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.worldclock.app_themes.R
import com.worldclock.app_themes.data.database.ReminderEntity
import com.worldclock.app_themes.databinding.ItemReminderBinding

class ReminderAdapter(
    private val onToggle: (ReminderEntity, Boolean) -> Unit,
    private val onEdit: (ReminderEntity) -> Unit,
    private val onDelete: (ReminderEntity) -> Unit
) : ListAdapter<ReminderEntity, ReminderAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemReminderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reminder: ReminderEntity) {
            binding.tvReminderTitle.text = reminder.title
            binding.tvReminderTime.text = String.format(
                "%02d:%02d %s", reminder.hour, reminder.minute,
                if (reminder.isAm) "AM" else "PM"
            )
            binding.tvAlarmSound.text = "Alarm Sound: ${reminder.sound}"

            binding.switchEnabled.setImageResource(
                if (reminder.isEnabled) R.drawable.alarm_selected
                else R.drawable.alarm_unselected
            )

            // Day chips
            val dayViews = listOf(
                binding.dayMon, binding.dayTue, binding.dayWed, binding.dayThu,
                binding.dayFri, binding.daySat, binding.daySun
            )
            val selectedIndices = reminder.repeatDays
                .split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .toSet()

            dayViews.forEachIndexed { i, tv ->
                if (i in selectedIndices) {
                    tv.setBackgroundResource(R.drawable.bg_day_selected)
                    tv.setTextColor(Color.WHITE)
                } else {
                    tv.setBackgroundResource(R.drawable.bg_day_unselected)
                    tv.setTextColor(Color.BLACK)
                }
            }

            binding.switchEnabled.setOnClickListener {
                val newState = !reminder.isEnabled
                binding.switchEnabled.setImageResource(
                    if (newState) R.drawable.alarm_selected
                    else R.drawable.alarm_unselected
                )
                onToggle(reminder, newState)
            }

            binding.ivEdit.setOnClickListener { onEdit(reminder) }
            binding.ivDelete.setOnClickListener { onDelete(reminder) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<ReminderEntity>() {
        override fun areItemsTheSame(a: ReminderEntity, b: ReminderEntity) = a.id == b.id
        override fun areContentsTheSame(a: ReminderEntity, b: ReminderEntity) = a == b
    }
}