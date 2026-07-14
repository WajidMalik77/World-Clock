package com.worldclock.app_themes.presentation.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.addCallback
import androidx.recyclerview.widget.GridLayoutManager
import com.worldclock.app_themes.R
import com.worldclock.app_themes.presentation.adapter.ClockWidgetAdapter
import com.worldclock.app_themes.databinding.ActivityWidgetBinding
import com.worldclock.app_themes.core.utils.ClockWidget
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.worldclock.app_themes.ads.helpers.ui.NativeAdOrchestrator
import com.worldclock.app_themes.ads.helpers.models.NativeAdConfig
import androidx.lifecycle.lifecycleScope
import com.worldclock.app_themes.ads.preload.AdLoadMode
import com.worldclock.app_themes.ads.preload.BannerPreload
import com.worldclock.app_themes.ads.preload.InterstitialAdManager
import com.worldclock.app_themes.ads.preload.InterstitialScreen
import com.worldclock.app_themes.ads.preload.NativePreload
import com.worldclock.app_themes.ads.preload.PreloadController
import com.worldclock.app_themes.ads.utils.GetFirebase
import com.worldclock.app_themes.ads.utils.Utils
import com.worldclock.app_themes.core.analytics.AppEventLogger
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WidgetActivity : BaseActivity() {
    @Inject
    lateinit var bannerAdOrchestrator: com.worldclock.app_themes.ads.helpers.ui.BannerAdOrchestrator

    private val binding by lazy { ActivityWidgetBinding.inflate(layoutInflater) }

    @Inject
    lateinit var nativeAdOrchestrator: NativeAdOrchestrator
    private val clockThemes: List<ClockWidget> = listOf(
        ClockWidget.Analog(
            R.drawable.face_01_orange_ring,
            R.drawable.needle_hour_01,
            R.drawable.needle_min_01,
            R.drawable.needle_sec_01
        ),
        ClockWidget.Analog(
            R.drawable.face_02_orange_ring,
            R.drawable.needle_hour_02,
            R.drawable.needle_min_02,
            R.drawable.needle_sec_01
        ),
        ClockWidget.Analog(
            R.drawable.face_03_galaxy,
            R.drawable.needle_hour_01,
            R.drawable.needle_min_01,
            R.drawable.needle_sec_01
        ),
        ClockWidget.Digital(R.drawable.face_04_digital_dark, Color.WHITE),
        ClockWidget.Analog(
            R.drawable.face_05_flat_blue,
            R.drawable.needle_hour_05,
            R.drawable.needle_min_05,
            R.drawable.needle_sec_05
        ),
        ClockWidget.Analog(
            R.drawable.face_06_white_pink,
            R.drawable.needle_hour_06,
            R.drawable.needle_min_06,
            R.drawable.needle_sec_06
        ),
        ClockWidget.Analog(
            R.drawable.face_07_blue_glass,
            R.drawable.needle_hour_01,
            R.drawable.needle_min_01,
            R.drawable.needle_sec_06
        ),
        ClockWidget.Analog(
            R.drawable.face_08_purple_pink,
            R.drawable.needle_hour_08,
            R.drawable.needle_min_08,
            R.drawable.needle_sec_06
        ),
        ClockWidget.Analog(
            R.drawable.face_09_neon_tach,
            R.drawable.needle_hour_09,
            R.drawable.needle_min_09,
            R.drawable.needle_sec_09
        ),
        ClockWidget.Analog(
            R.drawable.face_10_slate_blue,
            R.drawable.needle_hour_10,
            R.drawable.needle_min_10,
            R.drawable.needle_sec_10
        ),
        ClockWidget.Analog(
            R.drawable.face_11_black_red_ring,
            R.drawable.needle_hour_01,
            R.drawable.needle_min_01,
            R.drawable.needle_sec_01
        ),
        ClockWidget.Analog(
            R.drawable.face_12_crimson_card,
            R.drawable.needle_hour_12,
            R.drawable.needle_min_12,
            R.drawable.needle_sec_12
        ),
        ClockWidget.Analog(
            R.drawable.face_13_amber_card,
            R.drawable.needle_hour_13,
            R.drawable.needle_min_13,
            R.drawable.needle_sec_12
        ),
        ClockWidget.Analog(
            R.drawable.face_14_teal_card,
            R.drawable.needle_hour_10,
            R.drawable.needle_min_10,
            R.drawable.needle_sec_14
        ),
        ClockWidget.Analog(
            R.drawable.face_15_sky_card,
            R.drawable.needle_hour_15,
            R.drawable.needle_min_15,
            R.drawable.needle_sec_15
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppEventLogger.trackScreenCreate(this, savedInstanceState, "WidgetScreen", "activity_lifecycle")
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)


        binding.toolbar.title.text = getString(R.string.widget_clock)
        binding.toolbar.back.setOnClickListener {
            AppEventLogger.trackButtonClick("WidgetScreen", "back", "navigate_back", "widget_flow")


            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_top,"top",this@WidgetActivity,
                GetFirebase.adIdMainActivity_bannerTop,
                GetFirebase.adIdMainActivity_nativeTop)

            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_bottom,"bottom",this@WidgetActivity,
                GetFirebase.adIdMainActivity_bannerBottom,
                GetFirebase.adIdMainActivity_nativeBottom)


            InterstitialAdManager.showIfReady(
                this@WidgetActivity,
                InterstitialScreen.OTHER,
                GetFirebase.adIdOther_interstitial,
                if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                GetFirebase.transition_WidgetBack,
                GetFirebase.counter_interval,
                Utils.isPremium,
                GetFirebase.enable_interstitial_ads,
                {
                    startActivity(Intent(this@WidgetActivity, MainActivity::class.java))
                    finish()
                },
                {
                    startActivity(Intent(this@WidgetActivity, MainActivity::class.java))
                    finish()
                })

        }

        onBackPressedDispatcher.addCallback(this){


            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_top,"top",this@WidgetActivity,
                GetFirebase.adIdMainActivity_bannerTop,
                GetFirebase.adIdMainActivity_nativeTop)

            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_bottom,"bottom",this@WidgetActivity,
                GetFirebase.adIdMainActivity_bannerBottom,
                GetFirebase.adIdMainActivity_nativeBottom)


            InterstitialAdManager.showIfReady(
                this@WidgetActivity,
                InterstitialScreen.OTHER,
                GetFirebase.adIdOther_interstitial,
                if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                GetFirebase.transition_WidgetBack,
                GetFirebase.counter_interval,
                Utils.isPremium,
                GetFirebase.enable_interstitial_ads,
                {
                    startActivity(Intent(this@WidgetActivity, MainActivity::class.java))
                    finish()
                },
                {
                    startActivity(Intent(this@WidgetActivity, MainActivity::class.java))
                    finish()
                })
        }

        PreloadController.observeBanner(this,
            BannerPreload.adBannerTopLiveData,
            binding.bannerContainer.bannerTopContainer,binding.bannerContainer.adVeiwTop,binding.bannerContainer.adTextAdvertisementTop,
            GetFirebase.banner_ad_widgetactivity_top,"top",
            window,
            NativePreload.adNativeTopLiveData){

        }

        PreloadController.observeBanner(this,
            BannerPreload.adBannerBottomLiveData,
            binding.adsContainer.bannerBottomContainer,binding.adsContainer.adVeiwBottom,binding.adsContainer.adTextAdvertisementBottom,
            GetFirebase.banner_ad_widgetactivity_bottom,"bottom",
            window,
            NativePreload.adNativeBottomLiveData){

        }

        binding.rvClockThemes.apply {
            layoutManager = GridLayoutManager(this@WidgetActivity, 3)
            adapter = ClockWidgetAdapter(clockThemes)
            setHasFixedSize(true)
        }
    }

    override fun onDestroy() {
        AppEventLogger.trackScreenDestroy(this, "WidgetScreen")
        super.onDestroy()
    }
}
