package com.worldclock.app_themes.activities

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.edit
import androidx.viewpager2.widget.ViewPager2
import com.worldclock.app_themes.R
import com.worldclock.app_themes.adapter.OnboardingAdapter
import com.worldclock.app_themes.databinding.ActivityOnBoardingBinding
import com.worldclock.app_themes.utils.AdsConstants.LifeTimePref
import com.worldclock.app_themes.utils.AdsConstants.PrefsName
import com.worldclock.app_themes.utils.AdsConstants.isFirstTime
import com.worldclock.app_themes.utils.Constants
import com.worldclock.app_themes.utils.OnboardingItem
import com.worldclock.app_themes.utils.PrefUtil
import com.worldclock.app_themes.utils.SharePref
import com.worldclock.app_themes.utils.TYPE_AD
import com.worldclock.app_themes.utils.TYPE_DATA

class OnBoardingActivity : BaseActivity() {
    private val binding by lazy {
        ActivityOnBoardingBinding.inflate(layoutInflater)
    }

    companion object {
        internal var isFullAd = false
    }

    private lateinit var adapter: OnboardingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)
        isFullAd = false

        setViewPager()

        binding.nextBtn.paintFlags = binding.nextBtn.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        binding.nextBtn.setOnClickListener {
            val current = binding.viewPager.currentItem
            val total = binding.viewPager.adapter?.itemCount ?: 0
            if (current < total - 1) {
                binding.viewPager.setCurrentItem(current + 1, true)
            } else {
                goNext()
            }
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if (position == pages.size - 1) {
                    binding.nextBtn.text = getString(R.string.finish)
                } else {
                    binding.nextBtn.text = getString(R.string.next)
                }
            }
        })
    }

    private fun goNext() {
        getSharedPreferences(PrefsName, Context.MODE_PRIVATE).edit {
            putBoolean(isFirstTime, true)
        }
        if (PrefUtil(this).getBool("is_premium", false) ||
            getSharedPreferences(LifeTimePref, Context.MODE_PRIVATE).getBoolean("premium", false)
        ) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, ActivityPurchase::class.java).putExtra("isSplash", true))
        }
        finish()
    }

    val pages = mutableListOf<OnboardingItem>()

    private fun setViewPager() {
        if (!isFinishing) {
            pages.add(
                OnboardingItem(
                    type = TYPE_DATA,
                    title = getString(R.string.set_alarm_time),
                    description = getString(R.string.set_your_alarm_with_ease_using_customizable_options_for_effortless_scheduling),
                    imageRes = R.drawable.i1,
                    dotRes = R.drawable.ic_dot
                )
            )

            pages.add(
                OnboardingItem(
                    type = TYPE_DATA,
                    title = getString(R.string.global_watch),
                    description = getString(R.string.global_watch_provides_a_streamlined_solution_for_monitoring_global_time_zones),
                    imageRes = R.drawable.i2,
                    dotRes = R.drawable.ic_dot_1
                )
            )

            pages.add(
                OnboardingItem(
                    type = TYPE_DATA,
                    title = getString(R.string.timer_running_out),
                    description = getString(R.string.the_timer_feature_enables_countdowns_for_specific_tasks_or_events),
                    imageRes = R.drawable.i3,
                    dotRes = R.drawable.ic_dot_2
                )
            )

            adapter = OnboardingAdapter(pages)
            binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            binding.viewPager.adapter = adapter
            binding.viewPager.isSaveEnabled = false
            binding.viewPager.currentItem = 0
        }
    }
}