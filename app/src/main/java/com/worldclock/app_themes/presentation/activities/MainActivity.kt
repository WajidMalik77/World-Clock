package com.worldclock.app_themes.presentation.activities

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.addCallback
import androidx.recyclerview.widget.GridLayoutManager
import com.worldclock.app_themes.R
import com.worldclock.app_themes.presentation.adapter.HomeAdapter
import com.worldclock.app_themes.databinding.ActivityMainBinding
import com.worldclock.app_themes.core.utils.AdsConstants.LifeTimePref
import com.worldclock.app_themes.core.utils.AdsConstants.PrefsName
import com.worldclock.app_themes.core.utils.AdsConstants.isFirstTime
import androidx.core.content.edit
import com.worldclock.app_themes.core.utils.ClockCanvasView
import com.worldclock.app_themes.core.utils.NOTIFICATION_PERMISSION_CODE
import com.worldclock.app_themes.core.utils.PrefUtil
import com.worldclock.app_themes.core.utils.getHomeData
import com.worldclock.app_themes.core.utils.requestNotificationPermissionIfNeeded
import com.worldclock.app_themes.core.utils.showNotificationPermissionSettings
import java.util.Calendar

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.worldclock.app_themes.ads.helpers.ui.NativeAdOrchestrator
import com.worldclock.app_themes.ads.helpers.models.NativeAdConfig

import com.worldclock.app_themes.ads.helpers.ui.BannerAdOrchestrator
import com.worldclock.app_themes.ads.helpers.safeShowInterstitialAction
import com.worldclock.app_themes.ads.di.AdConfigEntryPoint
import com.worldclock.app_themes.ads.preload.AdLoadMode
import com.worldclock.app_themes.ads.preload.BannerPreload
import com.worldclock.app_themes.ads.preload.InterstitialAdManager
import com.worldclock.app_themes.ads.preload.InterstitialScreen
import com.worldclock.app_themes.ads.preload.NativePreload
import com.worldclock.app_themes.ads.preload.PreloadController
import com.worldclock.app_themes.ads.utils.GetFirebase
import com.worldclock.app_themes.ads.utils.Utils
import com.worldclock.app_themes.core.analytics.AppEventLogger
import dagger.hilt.android.EntryPointAccessors

@AndroidEntryPoint
class MainActivity : BaseActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var clockRunnable: Runnable
    private var centerNativeLoaded = false

    @Inject
    lateinit var nativeAdOrchestrator: NativeAdOrchestrator

    @Inject
    lateinit var bannerAdOrchestrator: BannerAdOrchestrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)
        AppEventLogger.trackScreenCreate(
            this,
            savedInstanceState,
            "HomeScreen",
            "activity_lifecycle"
        )

        getSharedPreferences(PrefsName, MODE_PRIVATE).edit {
            putBoolean(isFirstTime, true)
        }

        loadAds()

        Utils.isPremium = PrefUtil(this).getBool("is_premium", false) ||
                getSharedPreferences(LifeTimePref, Context.MODE_PRIVATE).getBoolean(
                    "premium",
                    false
                )


        requestNotificationPermissionIfNeeded()

        binding.toolbar.back.setImageResource(R.drawable.menu)
        binding.toolbar.back.setOnClickListener {
            AppEventLogger.trackButtonClick("HomeScreen", "menu", "navigate", "home_flow")
            startActivity(Intent(this, MenuActivity::class.java))
        }

        clockRunnable = object : Runnable {
            override fun run() {
                val cal = Calendar.getInstance()
                val sec = cal.get(Calendar.SECOND)
                val min = cal.get(Calendar.MINUTE)
                val hour = cal.get(Calendar.HOUR)

                binding.timeTxt.text = String.format("%02d:%02d", hour, min)

                binding.clockContainer.removeAllViews()
                binding.clockContainer.addView(
                    ClockCanvasView(
                        context = this@MainActivity,
                        startDegHr = hour * 30f + min * 0.5f,
                        startDegMin = min * 6f + sec * 0.1f,
                        startDegSec = 0f,
                        clockImage = R.drawable.ic_clock_home,
                        hourImage = R.drawable.ic_hour_hand,
                        minImage = R.drawable.ic_minute_hand
                    )
                )
                handler.postDelayed(this, 1_000L)
            }
        }
        handler.post(clockRunnable)

        binding.premium.setOnClickListener {
            AppEventLogger.trackButtonClick("HomeScreen", "go_premium", "navigate", "home_flow")

            InterstitialAdManager.showIfReady(
                this,
                InterstitialScreen.OTHER,
                GetFirebase.adIdOther_interstitial,
                if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                GetFirebase.transition_MainForward,
                GetFirebase.counter_interval,
                Utils.isPremium,
                GetFirebase.enable_interstitial_ads,
                {
                    startActivity(resolveManualPremiumIntent())

                },
                {
                    startActivity(resolveManualPremiumIntent())

                })


        }


        lateinit var homeAdapter: HomeAdapter
        val layoutManager = GridLayoutManager(this, 6)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int = homeAdapter.getSpanSize(position)
        }
        binding.recycler.layoutManager = layoutManager


        if (GetFirebase.enable_on_demand_interstitial == 1){
            InterstitialAdManager.load(this, GetFirebase.adIdOther_interstitial, InterstitialScreen.OTHER)
        }

        homeAdapter = HomeAdapter(
            courseList = getHomeData(),
            showCenterNative = false,
            onBindCenterNative = { adBinding ->
                if (centerNativeLoaded) return@HomeAdapter
                centerNativeLoaded = true
                lifecycleScope.launch {
                    nativeAdOrchestrator.loadNativeAds(
                        context = this@MainActivity,
                        screen = "HomeScreen",
                        nativeConfigs = listOf(
                            NativeAdConfig(
                                position = "center",
                                container = adBinding.admobNative,
                                shimmer = adBinding.nativeAdShimmer
                            )
                        )
                    )
                }
            }
        )
        { pos ->
            val isPremium = PrefUtil(this).getBool("is_premium", false)
                    || getSharedPreferences(LifeTimePref, 0).getBoolean("premium", false)

            val navigate = {
                val triggerKey = when (pos) {
                    0 -> "clock"
                    1 -> "alarm"
                    2 -> "stopwatch"
                    3 -> "timer"
                    4 -> "compass"
                    5 -> "widget"
                    6 -> "reminders"
                    7 -> "sleep_sound"
                    else -> "clock"
                }
                AppEventLogger.trackButtonClick("HomeScreen", triggerKey, "navigate", "home_grid")
                when (pos) {
                    0 -> {

                        PreloadController.loadAdInBannerPosition(
                            GetFirebase.banner_ad_clockactivity_top, "top", this@MainActivity,
                            GetFirebase.adIdClock_bannerTop, GetFirebase.adIdClock_nativeTop
                        )

                        PreloadController.loadAdInBannerPosition(
                            GetFirebase.banner_ad_clockactivity_bottom, "bottom", this@MainActivity,
                            GetFirebase.adIdClock_bannerBottom, GetFirebase.adIdClock_nativeBottom
                        )


                        InterstitialAdManager.showIfReady(
                            this,
                            InterstitialScreen.OTHER,
                            GetFirebase.adIdOther_interstitial,
                            if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                            GetFirebase.transition_MainForward,
                            GetFirebase.counter_interval,
                            Utils.isPremium,
                            GetFirebase.enable_interstitial_ads,
                            {
                                startActivity(Intent(this, ClockActivity::class.java))


                            },
                            {
                                startActivity(Intent(this, ClockActivity::class.java))


                            })
                    }

                    1 -> {
                        PreloadController.loadAdInBannerPosition(
                            GetFirebase.banner_ad_alarmactivity_top, "top", this@MainActivity,
                            GetFirebase.adIdAlarm_bannerTop, GetFirebase.adIdAlarm_nativeTop
                        )

                        PreloadController.loadAdInBannerPosition(
                            GetFirebase.banner_ad_alarmactivity_bottom, "bottom", this@MainActivity,
                            GetFirebase.adIdAlarm_bannerBottom, GetFirebase.adIdAlarm_nativeBottom
                        )


                        InterstitialAdManager.showIfReady(
                            this,
                            InterstitialScreen.OTHER,
                            GetFirebase.adIdOther_interstitial,
                            if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                            GetFirebase.transition_MainForward,
                            GetFirebase.counter_interval,
                            Utils.isPremium,
                            GetFirebase.enable_interstitial_ads,
                            {
                                startActivity(Intent(this, AlarmActivity::class.java))


                            },
                            {
                                startActivity(Intent(this, AlarmActivity::class.java))


                            })
                    }

                    2 -> {

                        PreloadController.loadAdInBannerPosition(
                            GetFirebase.banner_ad_stopwatchactivity_top, "top", this@MainActivity,
                            GetFirebase.adIdStopWatch_bannerTop, GetFirebase.adIdStopWatch_nativeTop
                        )

                        PreloadController.loadAdInBannerPosition(
                            GetFirebase.banner_ad_stopwatchactivity_bottom, "bottom", this@MainActivity,
                            GetFirebase.adIdStopWatch_bannerBottom, GetFirebase.adIdStopWatch_nativeBottom
                        )


                        InterstitialAdManager.showIfReady(
                            this,
                            InterstitialScreen.OTHER,
                            GetFirebase.adIdOther_interstitial,
                            if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                            GetFirebase.transition_MainForward,
                            GetFirebase.counter_interval,
                            Utils.isPremium,
                            GetFirebase.enable_interstitial_ads,
                            {
                                startActivity(Intent(this, StopWatchActivity::class.java))


                            },
                            {
                                startActivity(Intent(this, StopWatchActivity::class.java))


                            })
                    }

                    3 -> {


                        PreloadController.loadAdInBannerPosition(
                            GetFirebase.banner_ad_timeractivity_top, "top", this@MainActivity,
                            GetFirebase.adIdTimer_bannerTop, GetFirebase.adIdTimer_nativeTop
                        )

                        PreloadController.loadAdInBannerPosition(
                            GetFirebase.banner_ad_timeractivity_bottom, "bottom", this@MainActivity,
                            GetFirebase.adIdTimer_bannerBottom, GetFirebase.adIdTimer_nativeBottom
                        )


                        InterstitialAdManager.showIfReady(
                            this,
                            InterstitialScreen.OTHER,
                            GetFirebase.adIdOther_interstitial,
                            if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                            GetFirebase.transition_MainForward,
                            GetFirebase.counter_interval,
                            Utils.isPremium,
                            GetFirebase.enable_interstitial_ads,
                            {
                                startActivity(Intent(this, TimerActivity::class.java))


                            },
                            {
                                startActivity(Intent(this, TimerActivity::class.java))


                            })
                    }

                    4 -> {


                        PreloadController.loadAdInBannerPosition(
                            GetFirebase.banner_ad_compassactivity_top, "top", this@MainActivity,
                            GetFirebase.adIdCompass_bannerTop, GetFirebase.adIdCompass_nativeTop
                        )

                        PreloadController.loadAdInBannerPosition(
                            GetFirebase.banner_ad_compassactivity_bottom, "bottom", this@MainActivity,
                            GetFirebase.adIdCompass_bannerBottom, GetFirebase.adIdCompass_nativeBottom
                        )


                        InterstitialAdManager.showIfReady(
                            this,
                            InterstitialScreen.OTHER,
                            GetFirebase.adIdOther_interstitial,
                            if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                            GetFirebase.transition_MainForward,
                            GetFirebase.counter_interval,
                            Utils.isPremium,
                            GetFirebase.enable_interstitial_ads,
                            {
                                startActivity(Intent(this, CompassActivity::class.java))


                            },
                            {
                                startActivity(Intent(this, CompassActivity::class.java))


                            })
                    }

                    5 -> {

                        PreloadController.loadAdInBannerPosition(
                            GetFirebase.banner_ad_widgetactivity_top, "top", this@MainActivity,
                            GetFirebase.adIdWidget_bannerTop, GetFirebase.adIdWidget_nativeTop
                        )

                        PreloadController.loadAdInBannerPosition(
                            GetFirebase.banner_ad_widgetactivity_bottom, "bottom", this@MainActivity,
                            GetFirebase.adIdWidget_bannerBottom, GetFirebase.adIdWidget_nativeBottom
                        )

                        InterstitialAdManager.showIfReady(
                            this,
                            InterstitialScreen.OTHER,
                            GetFirebase.adIdOther_interstitial,
                            if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                            GetFirebase.transition_MainForward,
                            GetFirebase.counter_interval,
                            Utils.isPremium,
                            GetFirebase.enable_interstitial_ads,
                            {
                                startActivity(Intent(this, WidgetActivity::class.java))


                            },
                            {
                                startActivity(Intent(this, WidgetActivity::class.java))


                            })
                    }

                    6 -> {

                        //start here

                        PreloadController.loadAdInBannerPosition(
                            GetFirebase.banner_ad_allreminders_top, "top", this@MainActivity,
                            GetFirebase.adIdAllReminders_bannerTop, GetFirebase.adIdAllReminders_nativeTop
                        )

                        PreloadController.loadAdInBannerPosition(
                            GetFirebase.banner_ad_allreminders_bottom, "bottom", this@MainActivity,
                            GetFirebase.adIdAllReminders_bannerBottom, GetFirebase.adIdAllReminders_nativeBottom
                        )


                        InterstitialAdManager.showIfReady(
                            this,
                            InterstitialScreen.OTHER,
                            GetFirebase.adIdOther_interstitial,
                            if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                            GetFirebase.transition_MainForward,
                            GetFirebase.counter_interval,
                            Utils.isPremium,
                            GetFirebase.enable_interstitial_ads,
                            {
                                startActivity(Intent(this, AllRemindersActivity::class.java))


                            },
                            {
                                startActivity(Intent(this, AllRemindersActivity::class.java))


                            })
                    }

                    7 -> {

                        PreloadController.loadAdInBannerPosition(
                            GetFirebase.banner_ad_sleepsoundactivity_top, "top", this@MainActivity,
                            GetFirebase.adIdSleepSound_bannerTop, GetFirebase.adIdSleepSound_nativeTop
                        )

                        PreloadController.loadAdInBannerPosition(
                            GetFirebase.banner_ad_sleepsoundactivity_bottom, "bottom", this@MainActivity,
                            GetFirebase.adIdSleepSound_bannerBottom, GetFirebase.adIdSleepSound_nativeBottom
                        )


                        InterstitialAdManager.showIfReady(
                            this,
                            InterstitialScreen.OTHER,
                            GetFirebase.adIdOther_interstitial,
                            if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                            GetFirebase.transition_MainForward,
                            GetFirebase.counter_interval,
                            Utils.isPremium,
                            GetFirebase.enable_interstitial_ads,
                            {
                                startActivity(Intent(this, SleepSoundActivity::class.java))


                            },
                            {
                                startActivity(Intent(this, SleepSoundActivity::class.java))


                            })
                    }
                }
            }

            if (isPremium) {
                navigate()
            } else {

                navigate()

//                val triggerKey = when (pos) {
//                    0 -> "clock"
//                    1 -> "alarm"
//                    2 -> "stopwatch"
//                    3 -> "timer"
//                    4 -> "compass"
//                    5 -> "widget"
//                    6 -> "reminders"
//                    7 -> "sleep_sound"
//                    else -> "clock"
//                }
//                safeShowInterstitialAction(
//                    screenName = "HomeScreen",
//                    trigger = triggerKey,
//                    noCounterNeeded = false,
//                    afterAd = navigate
//                )


            }
        }
        binding.recycler.adapter = homeAdapter


        onBackPressedDispatcher.addCallback(this@MainActivity) {


            PreloadController.loadAdInBannerPosition(
                GetFirebase.banner_ad_exitactivity_top, "top", this@MainActivity,
                GetFirebase.adIdExit_bannerTop, GetFirebase.adIdExit_nativeTop
            )

            PreloadController.loadAdInBannerPosition(
                GetFirebase.banner_ad_exitactivity_bottom, "bottom", this@MainActivity,
                GetFirebase.adIdExit_bannerBottom, GetFirebase.adIdExit_nativeBottom
            )


            InterstitialAdManager.showIfReady(
                this@MainActivity,
                InterstitialScreen.OTHER,
                GetFirebase.adIdOther_interstitial,
                if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                GetFirebase.transition_MainBack,
                GetFirebase.counter_interval,
                Utils.isPremium,
                GetFirebase.enable_interstitial_ads,
                {
                    startActivity(Intent(this@MainActivity, ExitActivity::class.java))
                    finish()
                },
                {
                    startActivity(Intent(this@MainActivity, ExitActivity::class.java))
                    finish()
                })

        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showNotificationPermissionSettings()
            }
        }
    }

    @Deprecated("Deprecated in Java")

    override fun onResume() {
        super.onResume()
        handler.post(clockRunnable)

    }

    private fun loadAds() {

        PreloadController.observeBanner(
            this,
            BannerPreload.adBannerTopLiveData,
            binding.bannerContainer.bannerTopContainer,
            binding.bannerContainer.adVeiwTop,
            binding.bannerContainer.adTextAdvertisementTop,
            GetFirebase.banner_ad_mainactivity_top,
            "top",
            window,
            NativePreload.adNativeTopLiveData
        ) {

        }

        PreloadController.observeBanner(
            this,
            BannerPreload.adBannerBottomLiveData,
            binding.adsContainer.bannerBottomContainer,
            binding.adsContainer.adVeiwBottom,
            binding.adsContainer.adTextAdvertisementBottom,
            GetFirebase.banner_ad_mainactivity_bottom,
            "bottom",
            window,
            NativePreload.adNativeBottomLiveData
        ) {

        }

    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(clockRunnable)
    }

    override fun onDestroy() {
        AppEventLogger.trackScreenDestroy(this, "HomeScreen")
        super.onDestroy()
        handler.removeCallbacks(clockRunnable)
    }

    private fun resolveManualPremiumIntent(): Intent {
        val premiumMode = runCatching {
            EntryPointAccessors.fromActivity(this, AdConfigEntryPoint::class.java)
                .adControlConfigManager()
                .getGoProPremiumScreenMode()
        }.getOrDefault(1)

        return when (premiumMode) {
            2 -> Intent(this, ActivityPurchase::class.java)
            else -> Intent(this, PremiumActivity::class.java)
        }
    }
}
