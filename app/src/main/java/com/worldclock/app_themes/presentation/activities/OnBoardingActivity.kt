package com.worldclock.app_themes.presentation.activities

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.edit
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
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
import androidx.viewpager.widget.ViewPager
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
import com.worldclock.app_themes.databinding.NativeFullMediaBinding

@AndroidEntryPoint
class OnBoardingActivity : BaseActivity() {
    private val binding by lazy {
        ActivityOnBoardingBinding.inflate(layoutInflater)
    }

    companion object {
        internal var isFullAd = false
    }

    // Add a field to store the ad binding
    private var adBinding: LayoutFullscreenAdIntroBinding? = null
    private var adTimerShown = false

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

        if (GetFirebase.enable_on_demand_interstitial == 1 && (GetFirebase.transition_OnboardingForward == 1)){
            InterstitialAdManager.loadOnboarding(this, GetFirebase.adIdOnboarding_interstitial)
        }

        if (GetFirebase.show_full_screen_native && !isPremium()){
            NativePreload.loadNormalNative(this, GetFirebase.adIDOnboarding_FullNative)
        }

        isFullAd = false
        bottomNativeAvailable = shouldShowBottomNative()
//        updateOnboardingChromeForPage(binding.viewPager.currentItem)

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


        binding.nextBtn.setOnClickListener {
            val current = binding.viewPager.currentItem
            val total = pages.size ?: 0
            AppEventLogger.trackButtonClick("IntroScreen", "next", "navigate_page", "onboarding_flow")
            if (current < total - 1) {
                binding.viewPager.setCurrentItem(current + 1, true)
            } else {
                goNext()
            }
        }

        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                if (position == pages.size - 1) {
                    binding.nextBtn.text = getString(R.string.finish)
                } else {
                    binding.nextBtn.text = getString(R.string.next)
                }

                if (position == 0){
                    binding.imageDot.setImageResource(R.drawable.ic_dot_one)
                }
                if (position == 1){
                    binding.imageDot.setImageResource(R.drawable.ic_dot_two)
                }
                if (position == 2){
                    binding.imageDot.setImageResource(R.drawable.ic_dot_three)
                }
                if (GetFirebase.show_full_screen_native && !isPremium()){
                    if (position == 4){
                        binding.imageDot.setImageResource(R.drawable.ic_dot_four)
                    }
                }
                else{
                    if (position == 3){
                        binding.imageDot.setImageResource(R.drawable.ic_dot_four)
                    }
                }

                val isAdPage = pages.getOrNull(position)?.type == TYPE_AD
                if (isAdPage && !adTimerShown) {
                    if (GetFirebase.banner_ad_onboardingactivity_bottom > 0){
                        binding.adsContainer.bannerBottomContainer.visibility = View.GONE
                    }

                    if (GetFirebase.banner_ad_onboardingactivity_top > 0){
                        binding.bannerContainer.bannerTopContainer.visibility = View.GONE

                    }

                    adBinding?.let { loadIntroFullScreenNative(it) }
                }

                if (!isAdPage){
                    if (GetFirebase.banner_ad_onboardingactivity_bottom > 0){
                        binding.adsContainer.bannerBottomContainer.visibility = View.VISIBLE
                    }

                    if (GetFirebase.banner_ad_onboardingactivity_top > 0){
                        binding.bannerContainer.bannerTopContainer.visibility = View.VISIBLE

                    }
                    binding.footerContainer.visibility = View.VISIBLE
                    binding.closeAdContainer.visibility = View.GONE
                }
                else{
                    binding.footerContainer.visibility = View.GONE
                    binding.closeAdContainer.visibility = View.VISIBLE

                    if (GetFirebase.banner_ad_onboardingactivity_bottom > 0){
                        binding.adsContainer.bannerBottomContainer.visibility = View.GONE
                    }

                    if (GetFirebase.banner_ad_onboardingactivity_top > 0){
                        binding.bannerContainer.bannerTopContainer.visibility = View.GONE

                    }

                }

//                updateOnboardingChromeForPage(position)
            }

            override fun onPageScrolled(pos: Int, offset: Float, offsetPixels: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
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

        startCloseAdTimer()

        NativePreload.adNativeNormalLiveData.observe(this){
            if (it){
                adBinding.shimmerContainer.visibility = View.GONE
                adBinding.admobNativeFullScreenIntroShimmer.visibility = View.GONE
                adBinding.admobNativeFullScreenIntro.visibility = View.VISIBLE
                var ad = NativePreload.nativeAdNormal

                val inflater = LayoutInflater.from(this)
                val container = adBinding.admobNativeFullScreenIntro

                val binding = NativeFullMediaBinding.inflate(inflater)
                val nativeAdView = binding.nativeAdView

                // Media
                nativeAdView.mediaView = binding.adMedia

                // Icon
                nativeAdView.iconView = binding.adAppIcon

                // Headline
                nativeAdView.headlineView = binding.adHeadline
                binding.adHeadline.text = ad?.headline

                // Body
                nativeAdView.bodyView = binding.adBody
                binding.adBody.text = ad?.body ?: ""

                // Store
                nativeAdView.storeView = binding.adStore
                if (ad?.store != null) {
                    binding.adStore.text = ad?.store
                    binding.adStore.visibility = View.VISIBLE
                } else {
                    binding.adStore.visibility = View.GONE
                }

                // Advertiser
                nativeAdView.advertiserView = binding.adAdvertiser
                if (ad?.advertiser != null) {
                    binding.adAdvertiser.text = ad.advertiser
                    binding.adAdvertiser.visibility = View.VISIBLE
                } else {
                    binding.adAdvertiser.visibility = View.GONE
                }

                // Price
                nativeAdView.priceView = binding.adPrice
                if (ad?.price != null) {
                    binding.adPrice.text = ad?.price
                    binding.adPrice.visibility = View.VISIBLE
                } else {
                    binding.adPrice.visibility = View.GONE
                }

                // Star rating
                nativeAdView.starRatingView = binding.adStars
                if (ad?.starRating != null) {
                    binding.adStars.rating = ad?.starRating!!.toFloat()
                    binding.adStars.visibility = View.VISIBLE
                } else {
                    binding.adStars.visibility = View.GONE
                }

                // Icon
                if (ad?.icon != null) {
                    binding.adAppIcon.setImageDrawable(ad.icon!!.drawable)
                    binding.adAppIcon.visibility = View.VISIBLE
                } else {
                    binding.adAppIcon.visibility = View.GONE
                }

                // Call to action
                nativeAdView.callToActionView = binding.adCallToAction
                binding.adCallToAction.text = ad?.callToAction ?: "Install"


                // Register
                ad?.let {
                    nativeAdView.setNativeAd(ad)
                }

                container.addView(binding.root)
            }
        }
    }

    private fun startCloseAdTimer() {

        if (adTimerShown) return  // already shown once, skip
        adTimerShown = true

        val timerTv = binding.tvTimer
        val closeIv = binding.ivClose
        val container = binding.closeAdContainer

        container.visibility = View.VISIBLE
        timerTv.visibility = View.VISIBLE
        closeIv.visibility = View.GONE

        binding.viewPager.swipeEnabled = false

        var timeLeft = 5
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
                    binding.viewPager.swipeEnabled = true
                    closeIv.setOnClickListener {
                        val current = binding.viewPager.currentItem
                        val total = pages.size ?: 0
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
                    title = getString(R.string.set_your_wake_time),
                    description = getString(R.string.effortlessly_schedule_your_custom_alarms_with_just_a_few_taps),
                    imageRes = R.drawable.ic_onboarding_one,
                    dotRes = R.drawable.ic_dot_one
                )
            )

            pages.add(
                OnboardingItem(
                    type = TYPE_DATA,
                    title = getString(R.string.gentle_wake_up_routines),
                    description = getString(R.string.set_a_daily_wake_up_time_to_optimize_efficiency_and_build_consistency),
                    imageRes = R.drawable.ic_onboarding_two,
                    dotRes = R.drawable.ic_dot_two
                )
            )

            pages.add(
                OnboardingItem(
                    type = TYPE_DATA,
                    title = getString(R.string.sleeping_sounds),
                    description = getString(R.string.listen_to_calming_sounds_to_quiet_your_mind_and_fall_asleep_fast),
                    imageRes = R.drawable.ic_onboarding_three,
                    dotRes = R.drawable.ic_dot_three
                )
            )

            if (!isPremium() && GetFirebase.show_full_screen_native) {
                pages.add(
                    OnboardingItem(
                        type = TYPE_AD
                    )
                )
            }

            pages.add(
                OnboardingItem(
                    type = TYPE_DATA,
                    title = getString(R.string.optimize_your_work_day),
                    description = getString(R.string.set_a_daily_wake_up_time_to_streamline_your_office_routine_and_build_consistent_productivity),
                    imageRes = R.drawable.ic_onboarding_four,
                    dotRes = R.drawable.ic_dot_four
                )
            )

            adapter = OnboardingAdapter(pages) { Binding ->
//                loadIntroFullScreenNative(adBinding)
                adBinding = Binding
            }
//            binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            binding.viewPager.adapter = adapter
            binding.viewPager.isSaveEnabled = false
            binding.viewPager.currentItem = 0
//            updateOnboardingChromeForPage(0)
        }
    }

    override fun onDestroy() {
        AppEventLogger.trackScreenDestroy(this, "IntroScreen")
        super.onDestroy()
    }
}
