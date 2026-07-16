package com.worldclock.app_themes.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.worldclock.app_themes.databinding.ItemBoardingBinding
import com.worldclock.app_themes.databinding.LayoutFullscreenAdIntroBinding
import com.worldclock.app_themes.core.utils.OnboardingItem
import com.worldclock.app_themes.core.utils.TYPE_AD
import kotlin.let


class OnboardingAdapter(
    private val items: List<OnboardingItem>,
    private val onBindIntroFullAd: ((LayoutFullscreenAdIntroBinding) -> Unit)? = null
) : PagerAdapter() {

    override fun getCount(): Int = items.size

    override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(container.context)
        val item = items[position]

        val view = when (item.type) {
            TYPE_AD -> {
                val binding = LayoutFullscreenAdIntroBinding.inflate(inflater, container, false)
                onBindIntroFullAd?.invoke(binding)
                binding.root
            }
            else -> {
                val binding = ItemBoardingBinding.inflate(inflater, container, false)
                binding.mainTxt.text = item.title
                binding.descTxt.text = item.description
                item.imageRes?.let { binding.imageOnboarding.setImageResource(it) }
                binding.root
            }
        }

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }
}