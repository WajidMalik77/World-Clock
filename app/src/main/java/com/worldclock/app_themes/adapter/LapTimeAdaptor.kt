package com.worldclock.app_themes.adapter

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
            counterList.removeAt(position)
            lapTimeList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount)
        }
    }

    override fun getItemCount() = counterList.size
}