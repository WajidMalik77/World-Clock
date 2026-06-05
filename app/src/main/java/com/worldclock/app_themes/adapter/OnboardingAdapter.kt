package com.worldclock.app_themes.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.worldclock.app_themes.databinding.ItemBoardingBinding
import com.worldclock.app_themes.utils.OnboardingItem
import com.worldclock.app_themes.utils.PrefUtil
import com.worldclock.app_themes.utils.SharePref
import kotlin.let


class OnboardingAdapter(
    private val items: List<OnboardingItem>
) : RecyclerView.Adapter<OnboardingAdapter.DataViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemBoardingBinding.inflate(inflater, parent, false)
        return DataViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    // ViewHolders
    class DataViewHolder(val binding: ItemBoardingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OnboardingItem) {
            binding.textView15.text = item.title
            binding.textView16.text = item.description
            item.imageRes?.let { binding.imageView3.setImageResource(it) }
            item.dotRes?.let { binding.dots.setImageResource(it) }
        }
    }
}

