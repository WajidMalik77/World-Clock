package com.worldclock.app_themes.presentation.activities

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.edit
import androidx.constraintlayout.widget.ConstraintSet
import androidx.viewpager2.widget.ViewPager2
import com.worldclock.app_themes.R
import com.worldclock.app_themes.presentation.adapter.OnboardingAdapter
import com.worldclock.app_themes.databinding.ActivityOnBoardingBinding
import com.worldclock.app_themes.core.utils.AdsConstants.LifeTimePref
import com.worldclock.app_themes.core.utils.AdsConstants.PrefsName
import com.worldclock.app_themes.core.utils.AdsConstants.isFirstTime
import com.worldclock.app_themes.core.utils.OnboardingItem
import com.worldclock.app_themes.core.utils.PrefUtil
import com.worldclock.app_themes.core.utils.TYPE_AD
import com.worldclock.app_themes.core.utils.TYPE_DATA

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.worldclock.app_themes.ads.helpers.ui.NativeAdOrchestrator
import com.worldclock.app_themes.ads.helpers.models.NativeAdEvent
import com.worldclock.app_themes.ads.helpers.models.NativeAdConfig
import dagger.hilt.android.EntryPointAccessors
import com.worldclock.app_themes.ads.di.AdConfigEntryPoint

import com.worldclock.app_themes.ads.helpers.ui.BannerAdOrchestrator
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
import com.worldclock.app_themes.core.utils.AdsConstants
import com.worldclock.app_themes.databinding.LayoutFullscreenAdIntroBinding

@AndroidEntryPoint
class OnBoardingActivity : BaseActivity() {
    private val binding by lazy {
        ActivityOnBoardingBinding.inflate(layoutInflater)
    }

    companion object {
        internal var isFullAd = false
    }

    private lateinit var adapter: OnboardingAdapter
    private var introFullscreenNativeLoaded = false
    private var bottomNativeAvailable = false

    @Inject
    lateinit var nativeAdOrchestrator: NativeAdOrchestrator

    @Inject
    lateinit var bannerAdOrchestrator: BannerAdOrchestrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)
        AppEventLogger.trackScreenCreate(this, savedInstanceState, "IntroScreen", "activity_lifecycle")

        if (GetFirebase.enable_on_demand_interstitial == 1){
            InterstitialAdManager.loadOnboarding(this, GetFirebase.adIdOnboarding_interstitial)
        }

        isFullAd = false
        bottomNativeAvailable = shouldShowBottomNative()
        updateOnboardingChromeForPage(binding.viewPager.currentItem)

        setViewPager()


        PreloadController.observeBanner(this,
            BannerPreload.adBannerTopLiveData,
            binding.bannerContainer.bannerTopContainer,binding.bannerContainer.adVeiwTop,binding.bannerContainer.adTextAdvertisementTop,
            GetFirebase.banner_ad_onboardingactivity_top,"top",
            window,
            NativePreload.adNativeTopLiveData){

        }

        PreloadController.observeBanner(this,
            BannerPreload.adBannerBottomLiveData,
            binding.adsContainer.bannerBottomContainer,binding.adsContainer.adVeiwBottom,binding.adsContainer.adTextAdvertisementBottom,
            GetFirebase.banner_ad_onboardingactivity_bottom,"bottom",
            window,
            NativePreload.adNativeBottomLiveData){

        }


        binding.nextBtn.paintFlags = binding.nextBtn.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        binding.nextBtn.setOnClickListener {
            val current = binding.viewPager.currentItem
            val total = binding.viewPager.adapter?.itemCount ?: 0
            AppEventLogger.trackButtonClick("IntroScreen", "next", "navigate_page", "onboarding_flow")
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
                updateOnboardingChromeForPage(position)
            }
        })
    }

    private fun updateOnboardingChromeForPage(position: Int) {
        val isAdPage = pages.getOrNull(position)?.type == TYPE_AD
        binding.nextBtn.visibility = if (isAdPage) View.GONE else View.VISIBLE
        binding.bannerContainer.root.visibility = if (isAdPage) View.GONE else View.VISIBLE
        binding.adsContainer.root.visibility =
            if (!isAdPage && bottomNativeAvailable) View.VISIBLE else View.GONE
        updatePagerBottomConstraint(!isAdPage && bottomNativeAvailable)
    }

    private fun updatePagerBottomConstraint(useBottomNative: Boolean) {
        ConstraintSet().apply {
            clone(binding.main)
            clear(R.id.viewPager, ConstraintSet.BOTTOM)
            connect(
                R.id.viewPager,
                ConstraintSet.BOTTOM,
                if (useBottomNative) R.id.ads_container else ConstraintSet.PARENT_ID,
                if (useBottomNative) ConstraintSet.TOP else ConstraintSet.BOTTOM,
                resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._8sdp)
            )
            applyTo(binding.main)
        }
    }

    private fun shouldShowBottomNative(): Boolean {
        if (isPremium()) return false
        val entry = EntryPointAccessors.fromActivity(this, AdConfigEntryPoint::class.java)
        if (!entry.adControlConfigManager().areAdsEnabled()) return false
        val nativeConfigManager = entry.nativeAdConfigManager()
        return nativeConfigManager.isNativeVisible("IntroScreen", "bottom") ||
                nativeConfigManager.isNativeVisible("OnBoardingScreen", "bottom")
    }

    private fun shouldShowIntroFullScreenNative(): Boolean {
        val entry = EntryPointAccessors.fromActivity(this, AdConfigEntryPoint::class.java)
        if (!entry.adControlConfigManager().areAdsEnabled()) return false
        val nativeConfigManager = entry.nativeAdConfigManager()
        return nativeConfigManager.isNativeVisible("IntroScreen", "full_screen") ||
                nativeConfigManager.isNativeVisible("OnBoardingScreen", "full_screen")
    }

    private fun loadIntroFullScreenNative(adBinding: LayoutFullscreenAdIntroBinding) {
        if (introFullscreenNativeLoaded || isPremium() || !shouldShowIntroFullScreenNative()) return
        lifecycleScope.launch {
            nativeAdOrchestrator.loadNativeAds(
                context = this@OnBoardingActivity,
                screen = "IntroScreen",
                nativeConfigs = listOf(
                    NativeAdConfig(
                        position = "full_screen",
                        container = adBinding.admobNativeFullScreenIntro,
                        shimmer = adBinding.shimmerContainer
                    )
                )
            )
            introFullscreenNativeLoaded = true
            startCloseAdTimer(adBinding)
        }
    }

    private fun startCloseAdTimer(adBinding: LayoutFullscreenAdIntroBinding) {
        val timerTv = adBinding.tvTimer
        val closeIv = adBinding.ivClose
        val container = adBinding.closeAdContainer

        container.visibility = View.VISIBLE
        timerTv.visibility = View.VISIBLE
        closeIv.visibility = View.GONE

        var timeLeft = 3
        val timerRunnable = object : Runnable {
            override fun run() {
                if (isFinishing || isDestroyed) return
                if (timeLeft > 0) {
                    timerTv.text = timeLeft.toString()
                    timeLeft--
                    timerTv.postDelayed(this, 1000)
                } else {
                    timerTv.visibility = View.GONE
                    closeIv.visibility = View.VISIBLE
                    closeIv.setOnClickListener {
                        val current = binding.viewPager.currentItem
                        val total = binding.viewPager.adapter?.itemCount ?: 0
                        if (current < total - 1) {
                            binding.viewPager.setCurrentItem(current + 1, true)
                        } else {
                            goNext()
                        }
                    }
                }
            }
        }
        timerTv.post(timerRunnable)
    }

    private fun isPremium(): Boolean {
        return PrefUtil(this).getBool("is_premium", false) ||
                getSharedPreferences(LifeTimePref, Context.MODE_PRIVATE).getBoolean("premium", false)
    }

    private fun goNext() {
        val proceed = {
            if (isPremium()) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {

                startActivity(getNextScreenIntent(this, "intro"))
            }
            getSharedPreferences(PrefsName, Context.MODE_PRIVATE).edit {
                putBoolean(isFirstTime, true)
            }
            finish()
        }

        if (isPremium()) {
            proceed()
        } else {

            InterstitialAdManager.showIfReady(
                this,
                InterstitialScreen.ONBOARDING,
                GetFirebase.adIdOther_interstitial,
                if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                GetFirebase.transition_OnboardingForward,
                GetFirebase.counter_interval,
                Utils.isPremium,
                GetFirebase.enable_interstitial_ads,
                {
                    proceed()
                },
                {
                    proceed()
                })

        }
    }

    fun getNextScreenIntent(context: Context, currentScreen: String): Intent {
        val isFirstLaunch = !context.getSharedPreferences(AdsConstants.PrefsName, Context.MODE_PRIVATE)
            .getBoolean(AdsConstants.isFirstTime, false)

        if (Utils.isPremium) {
            return Intent(context, MainActivity::class.java)
        }

        when (currentScreen) {
            "splash" -> {
                return Intent(context, PremiumActivity::class.java).putExtra("isSplash", true)

            }
            "languages" -> {
                return Intent(context, PremiumActivity::class.java).putExtra("isSplash", true)

            }
            "intro" -> {
                return Intent(context, PremiumActivity::class.java).putExtra("isSplash", true)

            }
            "premium" -> {
                return Intent(context, PremiumActivity::class.java).putExtra("isSplash", true)

            }
            else -> {
                return Intent(context, PremiumActivity::class.java).putExtra("isSplash", true)

            }
        }

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

//            if (!isPremium() && shouldShowIntroFullScreenNative()) {
//                pages.add(
//                    OnboardingItem(
//                        type = TYPE_AD
//                    )
//                )
//            }

            pages.add(
                OnboardingItem(
                    type = TYPE_DATA,
                    title = getString(R.string.timer_running_out),
                    description = getString(R.string.the_timer_feature_enables_countdowns_for_specific_tasks_or_events),
                    imageRes = R.drawable.i3,
                    dotRes = R.drawable.ic_dot_2
                )
            )

            adapter = OnboardingAdapter(pages) { adBinding ->
//                loadIntroFullScreenNative(adBinding)
            }
            binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            binding.viewPager.adapter = adapter
            binding.viewPager.isSaveEnabled = false
            binding.viewPager.currentItem = 0
            updateOnboardingChromeForPage(0)
        }
    }

    override fun onDestroy() {
        AppEventLogger.trackScreenDestroy(this, "IntroScreen")
        super.onDestroy()
    }
}
