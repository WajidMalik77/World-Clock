package com.worldclock.app_themes.presentation.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
        AppEventLogger.trackScreenCreate(this, savedInstanceState, "HomeScreen", "activity_lifecycle")

        getSharedPreferences(PrefsName, MODE_PRIVATE).edit {
            putBoolean(isFirstTime, true)
        }

        lifecycleScope.launch {
            bannerAdOrchestrator.loadBannerAd(
                context = this@MainActivity,
                screen = "HomeScreen",
                position = "top",
                container = binding.bannerContainer.admobBanner,
                shimmer = binding.bannerContainer.bannerAdShimmer
            )
        }

        lifecycleScope.launch {
            nativeAdOrchestrator.loadNativeAds(
                context = this@MainActivity,
                screen = "HomeScreen",
                nativeConfigs = listOf(
                    NativeAdConfig(
                        position = "bottom",
                        container = binding.adsContainer.admobNative,
                        shimmer = binding.adsContainer.nativeAdShimmer
                    )
                )
            )
        }

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
            startActivity(Intent(this, PremiumActivity::class.java))
        }

        val showCenterNative = EntryPointAccessors.fromActivity(
            this,
            AdConfigEntryPoint::class.java
        ).nativeAdConfigManager().isNativeVisible("HomeScreen", "center")

        lateinit var homeAdapter: HomeAdapter
        val layoutManager = GridLayoutManager(this, 6)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int = homeAdapter.getSpanSize(position)
        }
        binding.recycler.layoutManager = layoutManager

        homeAdapter = HomeAdapter(
            courseList = getHomeData(),
            showCenterNative = showCenterNative,
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
        ) { pos ->
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
                    0 -> startActivity(Intent(this, ClockActivity::class.java))
                    1 -> startActivity(Intent(this, AlarmActivity::class.java))
                    2 -> startActivity(Intent(this, StopWatchActivity::class.java))
                    3 -> startActivity(Intent(this, TimerActivity::class.java))
                    4 -> startActivity(Intent(this, CompassActivity::class.java))
                    5 -> startActivity(Intent(this, WidgetActivity::class.java))
                    6 -> startActivity(Intent(this, AllRemindersActivity::class.java))
                    7 -> startActivity(Intent(this, SleepSoundActivity::class.java))
                }
            }

            if (isPremium) {
                navigate()
            } else {
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
                safeShowInterstitialAction(
                    screenName = "HomeScreen",
                    trigger = triggerKey,
                    noCounterNeeded = false,
                    afterAd = navigate
                )
            }
        }
        binding.recycler.adapter = homeAdapter
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
    override fun onBackPressed() {
        val proceed = {
            super.onBackPressed()
            startActivity(
                Intent(this, ExitActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
            )
        }

        val isPremium = PrefUtil(this).getBool("is_premium", false)
            || getSharedPreferences(LifeTimePref, 0).getBoolean("premium", false)

        if (isPremium) {
            proceed()
        } else {
            safeShowInterstitialAction(
                screenName = "HomeScreen",
                trigger = "back",
                noCounterNeeded = false,
                afterAd = proceed
            )
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(clockRunnable)
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
}
