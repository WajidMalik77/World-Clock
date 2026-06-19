package com.worldclock.app_themes.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.worldclock.app_themes.databinding.ItemAlarmCategoryBinding
import com.worldclock.app_themes.domain.model.AlarmCategory

class AlarmCategoryAdapter(
    private var items: List<AlarmCategory>,
    private val onClick: (AlarmCategory) -> Unit
) : RecyclerView.Adapter<AlarmCategoryAdapter.VH>() {

    inner class VH(val binding: ItemAlarmCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemAlarmCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ctx = holder.itemView.context
        val category = items[position]

        with(holder.binding) {
            root.setCardBackgroundColor(ContextCompat.getColor(ctx, category.bgColorRes))
            tvTitle.text = category.title
            ivCategory.setImageResource(category.imageRes)

            tvSubtitle.text = if (category.reminderCount > 0)
                "${category.reminderCount} Reminder${if (category.reminderCount > 1) "s" else ""}"
            else
                category.subtitle

            root.setOnClickListener { onClick(category) }
        }
    }

    /** Call this to update reminder counts from DB */
    fun updateItems(newItems: List<AlarmCategory>) {
        items = newItems
        notifyDataSetChanged()
    }
}