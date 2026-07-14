package com.worldclock.app_themes.presentation.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.worldclock.app_themes.R
import com.worldclock.app_themes.presentation.adapter.MenuAdapter
import com.worldclock.app_themes.databinding.ActivityLanguagesBinding
import com.worldclock.app_themes.databinding.ActivityMenuBinding
import com.worldclock.app_themes.core.utils.getMenuData
import com.worldclock.app_themes.core.utils.moreApps
import com.worldclock.app_themes.core.utils.openPrivacyPolicy
import com.worldclock.app_themes.core.utils.rateApp
import com.worldclock.app_themes.core.utils.sendFeedback
import com.worldclock.app_themes.core.utils.shareApp

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.worldclock.app_themes.ads.helpers.ui.BannerAdOrchestrator
import com.worldclock.app_themes.core.analytics.AppEventLogger

import com.worldclock.app_themes.ads.helpers.ui.NativeAdOrchestrator
import com.worldclock.app_themes.ads.helpers.models.NativeAdConfig
import dagger.hilt.android.EntryPointAccessors
import com.worldclock.app_themes.ads.di.AdConfigEntryPoint
import com.worldclock.app_themes.ads.preload.AdLoadMode
import com.worldclock.app_themes.ads.preload.BannerPreload
import com.worldclock.app_themes.ads.preload.InterstitialAdManager
import com.worldclock.app_themes.ads.preload.InterstitialScreen
import com.worldclock.app_themes.ads.preload.NativePreload
import com.worldclock.app_themes.ads.preload.PreloadController
import com.worldclock.app_themes.ads.utils.GetFirebase
import com.worldclock.app_themes.ads.utils.Utils

@AndroidEntryPoint
class MenuActivity : BaseActivity() {
    private val binding by lazy {
        ActivityMenuBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var bannerAdOrchestrator: BannerAdOrchestrator

    @Inject
    lateinit var nativeAdOrchestrator: NativeAdOrchestrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)
        AppEventLogger.trackScreenCreate(this, savedInstanceState, "MenuScreen", "activity_lifecycle")


        PreloadController.observeBanner(this,
            BannerPreload.adBannerTopLiveData,
            binding.bannerContainer.bannerTopContainer,binding.bannerContainer.adVeiwTop,binding.bannerContainer.adTextAdvertisementTop,
            GetFirebase.banner_ad_menuactivity_top,"top",
            window,
            NativePreload.adNativeTopLiveData){

        }

        PreloadController.observeBanner(this,
            BannerPreload.adBannerBottomLiveData,
            binding.adsContainer.bannerBottomContainer,binding.adsContainer.adVeiwBottom,binding.adsContainer.adTextAdvertisementBottom,
            GetFirebase.banner_ad_menuactivity_bottom,"bottom",
            window,
            NativePreload.adNativeBottomLiveData){

        }

        binding.toolbar.back.setOnClickListener {
            AppEventLogger.trackButtonClick("MenuScreen", "back", "navigate_back", "menu_flow")

            PreloadController.loadAdInBannerPosition(
                GetFirebase.banner_ad_mainactivity_top, "top", this@MenuActivity,
                GetFirebase.adIdMainActivity_bannerTop, GetFirebase.adIdMainActivity_nativeTop
            )

            PreloadController.loadAdInBannerPosition(
                GetFirebase.banner_ad_mainactivity_bottom, "bottom", this@MenuActivity,
                GetFirebase.adIdMainActivity_bannerBottom, GetFirebase.adIdMainActivity_nativeBottom
            )


            InterstitialAdManager.showIfReady(
                this@MenuActivity,
                InterstitialScreen.OTHER,
                GetFirebase.adIdOther_interstitial,
                if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                GetFirebase.transition_MenuBack,
                GetFirebase.counter_interval,
                Utils.isPremium,
                GetFirebase.enable_interstitial_ads,
                {
                    startActivity(Intent(this@MenuActivity, MainActivity::class.java))
                    finish()
                },
                {
                    startActivity(Intent(this@MenuActivity, MainActivity::class.java))
                    finish()
                })


        }
        binding.toolbar.title.text = getString(R.string.settings1)

        binding.pro.setOnClickListener {
            AppEventLogger.trackButtonClick("MenuScreen", "go_premium", "navigate", "menu_flow")

            InterstitialAdManager.showIfReady(
                this,
                InterstitialScreen.OTHER,
                GetFirebase.adIdOther_interstitial,
                if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                GetFirebase.transition_MenuForward,
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
        binding.recycler.adapter = MenuAdapter(getMenuData()) {
            when (it) {
                0 -> {
                    AppEventLogger.trackButtonClick("MenuScreen", "languages", "navigate", "menu_options")


                    PreloadController.loadAdInBannerPosition(
                        GetFirebase.banner_ad_languagesactivity_top, "top", this@MenuActivity,
                        GetFirebase.adIdLanguagesActivity_bannerTop, GetFirebase.adIdLanguagesActivity_nativeTop
                    )

                    PreloadController.loadAdInBannerPosition(
                        GetFirebase.banner_ad_languagesactivity_bottom, "bottom", this@MenuActivity,
                        GetFirebase.adIdLanguagesActivity_bannerBottom, GetFirebase.adIdLanguagesActivity_nativeBottom
                    )

                    InterstitialAdManager.showIfReady(
                        this,
                        InterstitialScreen.OTHER,
                        GetFirebase.adIdOther_interstitial,
                        if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                        GetFirebase.transition_MenuForward,
                        GetFirebase.counter_interval,
                        Utils.isPremium,
                        GetFirebase.enable_interstitial_ads,
                        {
                            startActivity(Intent(this, LanguagesActivity::class.java))

                        },
                        {
                            startActivity(Intent(this, LanguagesActivity::class.java))

                        })
                }
                1 -> {
                    AppEventLogger.trackButtonClick("MenuScreen", "rate_us", "open_external", "menu_options")
                    rateApp(this)
                }
                2 -> {
                    AppEventLogger.trackButtonClick("MenuScreen", "feedback", "open_external", "menu_options")
                    sendFeedback(this)
                }
//                3 -> moreApps(this)
                3 -> {
                    AppEventLogger.trackButtonClick("MenuScreen", "share_app", "open_external", "menu_options")
                    shareApp(this)
                }
                4 -> {
                    AppEventLogger.trackButtonClick("MenuScreen", "terms_and_conditions", "open_external", "menu_options")
                    openPrivacyPolicy("https://styleappsworld.wordpress.com/world-clock-_terms-conditions/")
                }
                5 -> {
                    AppEventLogger.trackButtonClick("MenuScreen", "privacy_policy", "open_external", "menu_options")
                    openPrivacyPolicy("https://styleappsworld.wordpress.com/world-clock-privacy-policy/")
                }
            }
        }

        onBackPressedDispatcher.addCallback(this@MenuActivity){
            PreloadController.loadAdInBannerPosition(
                GetFirebase.banner_ad_mainactivity_top, "top", this@MenuActivity,
                GetFirebase.adIdMainActivity_bannerTop, GetFirebase.adIdMainActivity_nativeTop
            )

            PreloadController.loadAdInBannerPosition(
                GetFirebase.banner_ad_mainactivity_bottom, "bottom", this@MenuActivity,
                GetFirebase.adIdMainActivity_bannerBottom, GetFirebase.adIdMainActivity_nativeBottom
            )


            InterstitialAdManager.showIfReady(
                this@MenuActivity,
                InterstitialScreen.OTHER,
                GetFirebase.adIdOther_interstitial,
                if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                GetFirebase.transition_MenuBack,
                GetFirebase.counter_interval,
                Utils.isPremium,
                GetFirebase.enable_interstitial_ads,
                {
                    startActivity(Intent(this@MenuActivity, MainActivity::class.java))
                    finish()
                },
                {
                    startActivity(Intent(this@MenuActivity, MainActivity::class.java))
                    finish()
                })

        }

    }
 
    override fun onDestroy() {
        AppEventLogger.trackScreenDestroy(this, "MenuScreen")
        super.onDestroy()
    }

    private fun resolveManualPremiumIntent(): Intent {
        val premiumMode = runCatching {
            EntryPointAccessors.fromActivity(this, AdConfigEntryPoint::class.java)
                .adControlConfigManager()
                .getSettingsPremiumScreenMode()
        }.getOrDefault(1)

        return when (premiumMode) {
            2 -> Intent(this, ActivityPurchase::class.java)
            else -> Intent(this, PremiumActivity::class.java)
        }
    }
}