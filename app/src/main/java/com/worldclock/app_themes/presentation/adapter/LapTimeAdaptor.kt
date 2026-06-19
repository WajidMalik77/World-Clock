package com.worldclock.app_themes.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.worldclock.app_themes.databinding.LapsItemLayoutBinding

class LapTimeAdaptor(
    var counterList: ArrayList<String>,
    var lapTimeList: ArrayList<String>
) : RecyclerView.Adapter<LapTimeAdaptor.ViewHolder>() {

    inner class ViewHolder(var binding: LapsItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            LapsItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.counterTv.text = "Lap ${counterList[position]}"
        holder.binding.lapTimeTv.text = lapTimeList[position]

        holder.binding.deleteImg.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition == RecyclerView.NO_POSITION ||
                currentPosition !in counterList.indices ||
                currentPosition !in lapTimeList.indices
            ) {
                return@setOnClickListener
            }

            counterList.removeAt(currentPosition)
            lapTimeList.removeAt(currentPosition)
            notifyItemRemoved(currentPosition)
            val changedCount = itemCount - currentPosition
            if (changedCount > 0) {
                notifyItemRangeChanged(currentPosition, changedCount)
            }
        }
    }

    override fun getItemCount() = minOf(counterList.size, lapTimeList.size)
}
