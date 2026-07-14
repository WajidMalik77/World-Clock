package com.worldclock.app_themes.presentation.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
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
import com.worldclock.app_themes.ads.preload.AdLoadMode
import com.worldclock.app_themes.ads.preload.BannerPreload
import com.worldclock.app_themes.ads.preload.InterstitialAdManager
import com.worldclock.app_themes.ads.preload.InterstitialScreen
import com.worldclock.app_themes.ads.preload.NativePreload
import com.worldclock.app_themes.ads.preload.PreloadController
import com.worldclock.app_themes.ads.utils.GetFirebase
import com.worldclock.app_themes.ads.utils.Utils
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



        PreloadController.observeBanner(this,
            BannerPreload.adBannerTopLiveData,
            binding.bannerContainer.bannerTopContainer,binding.bannerContainer.adVeiwTop,binding.bannerContainer.adTextAdvertisementTop,
            GetFirebase.banner_ad_allreminders_top,"top",
            window,
            NativePreload.adNativeTopLiveData){

        }

        PreloadController.observeBanner(this,
            BannerPreload.adBannerBottomLiveData,
            binding.adsContainer.bannerBottomContainer,binding.adsContainer.adVeiwBottom,binding.adsContainer.adTextAdvertisementBottom,
            GetFirebase.banner_ad_allreminders_bottom,"bottom",
            window,
            NativePreload.adNativeBottomLiveData){

        }

        binding.toolbar.back.setOnClickListener {
            AppEventLogger.trackButtonClick("AllRemindersScreen", "back", "navigate_back", "reminders_flow")
            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_top,"top",this@AllRemindersActivity,
                GetFirebase.adIdMainActivity_bannerTop, GetFirebase.adIdMainActivity_nativeTop)

            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_bottom,"bottom",this@AllRemindersActivity,
                GetFirebase.adIdMainActivity_bannerBottom, GetFirebase.adIdMainActivity_nativeBottom)


            InterstitialAdManager.showIfReady(
                this@AllRemindersActivity,
                InterstitialScreen.OTHER,
                GetFirebase.adIdOther_interstitial,
                if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                GetFirebase.transition_AllRemindersBack,
                GetFirebase.counter_interval,
                Utils.isPremium,
                GetFirebase.enable_interstitial_ads,
                {
                    startActivity(Intent(this@AllRemindersActivity, MainActivity::class.java))
                    finish()
                },
                {
                    startActivity(Intent(this@AllRemindersActivity, MainActivity::class.java))
                    finish()
                })

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
                PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_addallreminders_top,"top",this@AllRemindersActivity,
                    GetFirebase.adIdAddAllReminders_bannerTop, GetFirebase.adIdAddAllReminders_nativeTop)

                PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_addallreminders_bottom,"bottom",this@AllRemindersActivity,
                    GetFirebase.adIdAddAllReminders_bannerBottom, GetFirebase.adIdAddAllReminders_nativeBottom)


                InterstitialAdManager.showIfReady(this, InterstitialScreen.OTHER, GetFirebase.adIdOther_interstitial,
                    if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                    GetFirebase.transition_AllRemindersForward, GetFirebase.counter_interval,
                    Utils.isPremium, GetFirebase.enable_interstitial_ads,{
                        navigate()
                    },{
                        navigate()
                    })


            }
        }

        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(this@AllRemindersActivity, 2)
            adapter = categoryAdapter
        }


        onBackPressedDispatcher.addCallback(this@AllRemindersActivity){

            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_top,"top",this@AllRemindersActivity,
                GetFirebase.adIdMainActivity_bannerTop, GetFirebase.adIdMainActivity_nativeTop)

            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_bottom,"bottom",this@AllRemindersActivity,
                GetFirebase.adIdMainActivity_bannerBottom, GetFirebase.adIdMainActivity_nativeBottom)



            InterstitialAdManager.showIfReady(
                this@AllRemindersActivity,
                InterstitialScreen.OTHER,
                GetFirebase.adIdOther_interstitial,
                if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                GetFirebase.transition_AllRemindersBack,
                GetFirebase.counter_interval,
                Utils.isPremium,
                GetFirebase.enable_interstitial_ads,
                {
                    startActivity(Intent(this@AllRemindersActivity, MainActivity::class.java))
                    finish()
                },
                {
                    startActivity(Intent(this@AllRemindersActivity, MainActivity::class.java))
                    finish()
                })
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
