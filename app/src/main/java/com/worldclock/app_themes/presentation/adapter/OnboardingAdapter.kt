package com.worldclock.app_themes.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.worldclock.app_themes.databinding.ItemBoardingBinding
import com.worldclock.app_themes.databinding.LayoutFullscreenAdIntroBinding
import com.worldclock.app_themes.core.utils.OnboardingItem
import com.worldclock.app_themes.core.utils.TYPE_AD
import kotlin.let


class OnboardingAdapter(
    private val items: List<OnboardingItem>,
    private val onBindIntroFullAd: ((LayoutFullscreenAdIntroBinding) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int = items[position].type

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_AD -> AdViewHolder(LayoutFullscreenAdIntroBinding.inflate(inflater, parent, false))
            else -> DataViewHolder(ItemBoardingBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is DataViewHolder -> holder.bind(item)
            is AdViewHolder -> onBindIntroFullAd?.invoke(holder.binding)
        }
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

    class AdViewHolder(val binding: LayoutFullscreenAdIntroBinding) :
        RecyclerView.ViewHolder(binding.root)
}
