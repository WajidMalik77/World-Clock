package com.worldclock.app_themes.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.addCallback
import androidx.recyclerview.widget.GridLayoutManager
import com.worldclock.app_themes.R
import com.worldclock.app_themes.presentation.adapter.HomeAdapter
import com.worldclock.app_themes.databinding.ActivityExitBinding
import com.worldclock.app_themes.core.utils.ClockCanvasView
import com.worldclock.app_themes.core.utils.getHomeData
import java.util.Calendar

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.worldclock.app_themes.ads.helpers.ui.NativeAdOrchestrator
import com.worldclock.app_themes.ads.helpers.models.NativeAdConfig

import com.worldclock.app_themes.ads.helpers.ui.BannerAdOrchestrator
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
class ExitActivity : BaseActivity() {

    private val binding by lazy { ActivityExitBinding.inflate(layoutInflater) }
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var clockRunnable: Runnable

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
            "ExitScreen",
            "activity_lifecycle"
        )



        PreloadController.observeBanner(
            this,
            BannerPreload.adBannerTopLiveData,
            binding.bannerContainer.bannerTopContainer,
            binding.bannerContainer.adVeiwTop,
            binding.bannerContainer.adTextAdvertisementTop,
            GetFirebase.banner_ad_exitactivity_top,
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
            GetFirebase.banner_ad_exitactivity_bottom,
            "bottom",
            window,
            NativePreload.adNativeBottomLiveData
        ) {

        }


        binding.toolbar.back.setOnClickListener {
            AppEventLogger.trackButtonClick("ExitScreen", "back", "navigate_back", "exit_flow")
            navigateToMain()
        }

        clockRunnable = object : Runnable {
            override fun run() {
                val cal = Calendar.getInstance()
                val sec = cal.get(Calendar.SECOND)
                val min = cal.get(Calendar.MINUTE)
                val hour = cal.get(Calendar.HOUR)


                binding.clockContainer.removeAllViews()
                binding.clockContainer.addView(
                    ClockCanvasView(
                        context = this@ExitActivity,
                        startDegHr = hour * 30f + min * 0.5f,
                        startDegMin = min * 6f + sec * 0.1f,
                        startDegSec = 0f,
                        clockImage = R.drawable.ic_exit_clock,
                        hourImage = R.drawable.ic_hour_hand,
                        minImage = R.drawable.ic_minute_hand
                    )
                )
                handler.postDelayed(this, 1_000L)
            }
        }
        handler.post(clockRunnable)

        val exitItems = ArrayList(getHomeData().take(3))


        onBackPressedDispatcher.addCallback(this@ExitActivity){
            navigateToMain()
        }


        binding.recycler.layoutManager = GridLayoutManager(this, 3)
        binding.recycler.adapter = HomeAdapter(exitItems) { pos ->
            val triggerKey = when (pos) {
                0 -> "clock"
                1 -> "alarm"
                2 -> "stopwatch"
                else -> "clock"
            }
            AppEventLogger.trackButtonClick("ExitScreen", triggerKey, "navigate", "exit_grid")
            when (pos) {
                0 -> {


                    PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_clockactivity_top,"top",this@ExitActivity,
                        GetFirebase.adIdClock_bannerTop, GetFirebase.adIdClock_nativeTop)

                    PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_clockactivity_bottom,"bottom",this@ExitActivity,
                        GetFirebase.adIdClock_bannerBottom, GetFirebase.adIdClock_nativeBottom)


                    InterstitialAdManager.showIfReady(
                        this,
                        InterstitialScreen.OTHER,
                        GetFirebase.adIdOther_interstitial,
                        if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                        GetFirebase.transition_ExitForward,
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


                    PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_alarmactivity_top,"top",this@ExitActivity,
                        GetFirebase.adIdAlarm_bannerTop, GetFirebase.adIdAlarm_nativeTop)

                    PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_alarmactivity_bottom,"bottom",this@ExitActivity,
                        GetFirebase.adIdAlarm_bannerBottom, GetFirebase.adIdAlarm_nativeBottom)



                    InterstitialAdManager.showIfReady(
                        this,
                        InterstitialScreen.OTHER,
                        GetFirebase.adIdOther_interstitial,
                        if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                        GetFirebase.transition_ExitForward,
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

                    PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_stopwatchactivity_top,"top",this@ExitActivity,
                        GetFirebase.adIdStopWatch_bannerTop, GetFirebase.adIdStopWatch_nativeTop)

                    PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_stopwatchactivity_bottom,"bottom",this@ExitActivity,
                        GetFirebase.adIdStopWatch_bannerBottom, GetFirebase.adIdStopWatch_nativeBottom)



                    InterstitialAdManager.showIfReady(
                        this,
                        InterstitialScreen.OTHER,
                        GetFirebase.adIdOther_interstitial,
                        if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                        GetFirebase.transition_ExitForward,
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
            }
        }

        binding.tapToExit.setOnClickListener {
            AppEventLogger.trackButtonClick("ExitScreen", "exit", "exit_app", "exit_flow")
            finishAffinity()
        }
    }

    private fun navigateToMain() {

        PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_top,"top",this@ExitActivity,
            GetFirebase.adIdMainActivity_bannerTop, GetFirebase.adIdMainActivity_nativeTop)

        PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_bottom,"bottom",this@ExitActivity,
            GetFirebase.adIdMainActivity_bannerBottom, GetFirebase.adIdMainActivity_nativeBottom)

        InterstitialAdManager.showIfReady(
            this,
            InterstitialScreen.OTHER,
            GetFirebase.adIdOther_interstitial,
            if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
            GetFirebase.transition_ExitBackpress,
            GetFirebase.counter_interval,
            Utils.isPremium,
            GetFirebase.enable_interstitial_ads,
            {
                startActivity(
                    Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                )
                finish()

            },
            {
                startActivity(
                    Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                )
                finish()

            })


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
        AppEventLogger.trackScreenDestroy(this, "ExitScreen")
        super.onDestroy()
        handler.removeCallbacks(clockRunnable)
    }
}