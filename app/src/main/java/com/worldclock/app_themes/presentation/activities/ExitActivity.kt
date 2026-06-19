package com.worldclock.app_themes.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
        AppEventLogger.trackScreenCreate(this, savedInstanceState, "ExitScreen", "activity_lifecycle")

        lifecycleScope.launch {
            bannerAdOrchestrator.loadBannerAd(
                context = this@ExitActivity,
                screen = "ExitScreen",
                position = "top",
                container = binding.bannerContainer.admobBanner,
                shimmer = binding.bannerContainer.bannerAdShimmer
            )
        }

        lifecycleScope.launch {
            nativeAdOrchestrator.loadNativeAds(
                context = this@ExitActivity,
                screen = "ExitScreen",
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
                0 -> startActivity(Intent(this, ClockActivity::class.java))
                1 -> startActivity(Intent(this, AlarmActivity::class.java))
                2 -> startActivity(Intent(this, StopWatchActivity::class.java))
            }
        }

        binding.tapToExit.setOnClickListener {
            AppEventLogger.trackButtonClick("ExitScreen", "exit", "exit_app", "exit_flow")
            finishAffinity()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        navigateToMain()
    }

    private fun navigateToMain() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        )
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