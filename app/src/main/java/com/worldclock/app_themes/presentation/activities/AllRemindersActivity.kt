package com.worldclock.app_themes.presentation.activities

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.worldclock.app_themes.R
import com.worldclock.app_themes.presentation.adapter.AlarmCategoryAdapter
import com.worldclock.app_themes.data.database.WorldClockDatabase
import com.worldclock.app_themes.databinding.ActivityAllRemindersBinding
import com.worldclock.app_themes.core.utils.getAlarmCategories
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.worldclock.app_themes.ads.helpers.ui.NativeAdOrchestrator
import com.worldclock.app_themes.ads.helpers.models.NativeAdConfig
import com.worldclock.app_themes.core.utils.PrefUtil
import com.worldclock.app_themes.core.utils.AdsConstants.LifeTimePref
import com.worldclock.app_themes.ads.helpers.safeShowInterstitialAction
import com.worldclock.app_themes.core.analytics.AppEventLogger

@AndroidEntryPoint
class AllRemindersActivity : BaseActivity() {
    @Inject
    lateinit var bannerAdOrchestrator: com.worldclock.app_themes.ads.helpers.ui.BannerAdOrchestrator


    private val binding by lazy { ActivityAllRemindersBinding.inflate(layoutInflater) }

    @Inject
    lateinit var nativeAdOrchestrator: NativeAdOrchestrator
    private lateinit var categoryAdapter: AlarmCategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppEventLogger.trackScreenCreate(this, savedInstanceState, "AllRemindersScreen", "activity_lifecycle")
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)

        lifecycleScope.launch {
            bannerAdOrchestrator.loadBannerAd(
                context = this@AllRemindersActivity,
                screen = "AllRemindersScreen",
                position = "top",
                container = binding.bannerContainer.admobBanner,
                shimmer = binding.bannerContainer.bannerAdShimmer
            )
        }



        lifecycleScope.launch {
            nativeAdOrchestrator.loadNativeAds(
                context = this@AllRemindersActivity,
                screen = "AllRemindersScreen",
                nativeConfigs = listOf(
                    NativeAdConfig(
                        position = "bottom",
                        container = binding.adsContainer.admobNative,
                        shimmer = binding.adsContainer.nativeAdShimmer
                    )
                )
            )
        }
        binding.toolbar.back.setOnClickListener {
            AppEventLogger.trackButtonClick("AllRemindersScreen", "back", "navigate_back", "reminders_flow")
            finish()
        }
        binding.toolbar.title.text = getString(R.string.all_reminders)

        categoryAdapter = AlarmCategoryAdapter(getAlarmCategories()) { category ->
            AppEventLogger.trackButtonClick(
                "AllRemindersScreen",
                "category_${category.id}",
                "navigate",
                "reminders_grid"
            )
            val navigate = {
                startActivity(
                    Intent(this, AddAllRemindersActivity::class.java)
                        .putExtra("category_id", category.id)
                        .putExtra("category_title", category.title)
                )
            }

            val isPremium = PrefUtil(this).getBool("is_premium", false)
                || getSharedPreferences(LifeTimePref, 0).getBoolean("premium", false)

            if (isPremium) {
                navigate()
            } else {
                safeShowInterstitialAction(
                    screenName = "AllRemindersScreen",
                    trigger = "category",
                    noCounterNeeded = false,
                    afterAd = navigate
                )
            }
        }

        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(this@AllRemindersActivity, 2)
            adapter = categoryAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        loadReminderCounts()
    }

    private fun loadReminderCounts() {
        val dao = WorldClockDatabase.getDatabase(this).reminderDao()
        lifecycleScope.launch {
            val updatedCategories = getAlarmCategories().map { category ->
                val count = dao.getCountByCategoryId(category.id)
                category.copy(reminderCount = count)
            }
            categoryAdapter.updateItems(updatedCategories)
        }
    }

    override fun onDestroy() {
        AppEventLogger.trackScreenDestroy(this, "AllRemindersScreen")
        super.onDestroy()
    }
}
