package com.worldclock.app_themes.presentation.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.addCallback
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



        PreloadController.observeBanner(this,
            BannerPreload.adBannerTopLiveData,
            binding.bannerContainer.bannerTopContainer,binding.bannerContainer.adVeiwTop,binding.bannerContainer.adTextAdvertisementTop,
            GetFirebase.banner_ad_clockactivity_top,"top",
            window,
            NativePreload.adNativeTopLiveData){

        }

        PreloadController.observeBanner(this,
            BannerPreload.adBannerBottomLiveData,
            binding.adsContainer.bannerBottomContainer,binding.adsContainer.adVeiwBottom,binding.adsContainer.adTextAdvertisementBottom,
            GetFirebase.banner_ad_clockactivity_bottom,"bottom",
            window,
            NativePreload.adNativeBottomLiveData){

        }


        binding.toolbar.back.setOnClickListener {

            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_top,"top",this@ClockActivity,
                GetFirebase.adIdMainActivity_bannerTop, GetFirebase.adIdMainActivity_nativeTop)

            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_bottom,"bottom",this@ClockActivity,
                GetFirebase.adIdMainActivity_bannerBottom, GetFirebase.adIdMainActivity_nativeBottom)


            InterstitialAdManager.showIfReady(
                this@ClockActivity,
                InterstitialScreen.OTHER,
                GetFirebase.adIdOther_interstitial,
                if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                GetFirebase.transition_ClockBack,
                GetFirebase.counter_interval,
                Utils.isPremium,
                GetFirebase.enable_interstitial_ads,
                {
                    startActivity(Intent(this@ClockActivity, MainActivity::class.java))
                    finish()
                },
                {
                    startActivity(Intent(this@ClockActivity, MainActivity::class.java))
                    finish()
                })

        }


        onBackPressedDispatcher.addCallback(this@ClockActivity){

            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_top,"top",this@ClockActivity,
                GetFirebase.adIdMainActivity_bannerTop, GetFirebase.adIdMainActivity_nativeTop)

            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_bottom,"bottom",this@ClockActivity,
                GetFirebase.adIdMainActivity_bannerBottom, GetFirebase.adIdMainActivity_nativeBottom)



            InterstitialAdManager.showIfReady(
                this@ClockActivity,
                InterstitialScreen.OTHER,
                GetFirebase.adIdOther_interstitial,
                if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                GetFirebase.transition_ClockBack,
                GetFirebase.counter_interval,
                Utils.isPremium,
                GetFirebase.enable_interstitial_ads,
                {
                    startActivity(Intent(this@ClockActivity, MainActivity::class.java))
                    finish()
                },
                {
                    startActivity(Intent(this@ClockActivity, MainActivity::class.java))
                    finish()
                })
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
                PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_addclock_top,"top",this@ClockActivity,
                    GetFirebase.adIdAddClock_bannerTop, GetFirebase.adIdAddClock_nativeTop)

                PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_addclock_bottom,"bottom",this@ClockActivity,
                    GetFirebase.adIdAddClock_bannerBottom, GetFirebase.adIdAddClock_nativeBottom)


                InterstitialAdManager.showIfReady(this, InterstitialScreen.OTHER, GetFirebase.adIdOther_interstitial,
                    if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                    GetFirebase.transition_ClockForward, GetFirebase.counter_interval,
                    Utils.isPremium, GetFirebase.enable_interstitial_ads,{
                        navigate()
                    },{
                        navigate()
                    })

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