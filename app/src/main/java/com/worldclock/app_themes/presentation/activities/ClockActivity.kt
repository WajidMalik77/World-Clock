package com.worldclock.app_themes.presentation.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.worldclock.app_themes.R
import com.worldclock.app_themes.presentation.adapter.WorldClockAdapter
import com.worldclock.app_themes.databinding.ActivityClockBinding
import androidx.lifecycle.lifecycleScope
import com.worldclock.app_themes.data.database.WorldClockDao
import com.worldclock.app_themes.data.database.WorldClockDatabase
import com.worldclock.app_themes.data.database.WorldClockItem
import com.worldclock.app_themes.core.utils.getCurrentWorldClock
import com.worldclock.app_themes.core.utils.updateTimes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.worldclock.app_themes.ads.helpers.ui.NativeAdOrchestrator
import com.worldclock.app_themes.ads.helpers.models.NativeAdConfig
import com.worldclock.app_themes.core.utils.PrefUtil
import com.worldclock.app_themes.core.utils.AdsConstants.LifeTimePref
import com.worldclock.app_themes.ads.helpers.safeShowInterstitialAction
import com.worldclock.app_themes.core.analytics.AppEventLogger

@AndroidEntryPoint
class ClockActivity : BaseActivity() {
    @Inject
    lateinit var bannerAdOrchestrator: com.worldclock.app_themes.ads.helpers.ui.BannerAdOrchestrator

    private val binding by lazy {
        ActivityClockBinding.inflate(layoutInflater)
    }
    private lateinit var adapter: WorldClockAdapter

    @Inject
    lateinit var nativeAdOrchestrator: NativeAdOrchestrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)
        AppEventLogger.trackScreenCreate(this, savedInstanceState, "ClockScreen", "activity_lifecycle")

        lifecycleScope.launch {
            bannerAdOrchestrator.loadBannerAd(
                context = this@ClockActivity,
                screen = "ClockScreen",
                position = "top",
                container = binding.bannerContainer.admobBanner,
                shimmer = binding.bannerContainer.bannerAdShimmer
            )
        }



        lifecycleScope.launch {
            nativeAdOrchestrator.loadNativeAds(
                context = this@ClockActivity,
                screen = "ClockScreen",
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
            finish()
        }
        binding.countryName.text = getCurrentWorldClock().countryName
        binding.countryImg.text = getCurrentWorldClock().flag
        binding.timeTv.text = getCurrentWorldClock().time
        binding.dateTv.text = getCurrentWorldClock().date
        adapter = WorldClockAdapter(callbacks = {}, onLongClick = {
            showDeleteDialog(
                it,
                WorldClockDatabase.getDatabase(this@ClockActivity).worldClockDao()
            )
        })
        WorldClockDatabase.getDatabase(this@ClockActivity).worldClockDao().getSelectedClocks()
            .observe(this@ClockActivity) {
                val updated = updateTimes(it)
                adapter.updateList(updated)
                binding.recycler.adapter = adapter
                startClockUpdates()

            }
        binding.addClock.setOnClickListener {
            val navigate = {
                startActivity(Intent(this, AddClockActivity::class.java))
            }
            val isPremium = PrefUtil(this).getBool("is_premium", false)
                || getSharedPreferences(LifeTimePref, 0).getBoolean("premium", false)

            if (isPremium) {
                navigate()
            } else {
                safeShowInterstitialAction(
                    screenName = "ClockScreen",
                    trigger = "add_clock",
                    noCounterNeeded = false,
                    afterAd = navigate
                )
            }
        }

    }

    private fun showDeleteDialog(alarm: WorldClockItem, dao: WorldClockDao) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_clock))
            .setMessage(getString(R.string.are_you_sure_you_want_to_delete_this_clock))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                alarm.isSelected = false
                lifecycleScope.launch(Dispatchers.IO) {
                    dao.updateClock(alarm)

                }

            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateRunnable: Runnable
    private fun startClockUpdates() {
        updateRunnable = object : Runnable {
            override fun run() {
                adapter.updateTime()
                binding.timeTv.text = getCurrentWorldClock().time

                handler.postDelayed(this, 1000)
            }
        }
        handler.post(updateRunnable)
    }

    override fun onDestroy() {
        AppEventLogger.trackScreenDestroy(this, "ClockScreen")
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }

    private fun loadClocks() {
        CoroutineScope(Dispatchers.IO).launch {
            WorldClockDatabase.getDatabase(this@ClockActivity).worldClockDao().getSelectedClocks()
                .observe(this@ClockActivity) {
                    val updated = updateTimes(it)

                }
            withContext(Dispatchers.Main) {

            }
        }
    }
}