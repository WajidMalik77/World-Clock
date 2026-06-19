package com.worldclock.app_themes.presentation.activities

import android.content.Intent
import android.os.Bundle
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

        lifecycleScope.launch {
            bannerAdOrchestrator.loadBannerAd(
                context = this@MenuActivity,
                screen = "MenuScreen",
                position = "top",
                container = binding.bannerContainer.admobBanner,
                shimmer = binding.bannerContainer.bannerAdShimmer
            )
        }

        lifecycleScope.launch {
            nativeAdOrchestrator.loadNativeAds(
                context = this@MenuActivity,
                screen = "MenuScreen",
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
            AppEventLogger.trackButtonClick("MenuScreen", "back", "navigate_back", "menu_flow")
            finish()
        }
        binding.toolbar.title.text = getString(R.string.settings1)

        binding.pro.setOnClickListener {
            AppEventLogger.trackButtonClick("MenuScreen", "go_premium", "navigate", "menu_flow")
            startActivity(Intent(this, PremiumActivity::class.java))
        }
        binding.recycler.adapter = MenuAdapter(getMenuData()) {
            when (it) {
                0 -> {
                    AppEventLogger.trackButtonClick("MenuScreen", "languages", "navigate", "menu_options")
                    startActivity(Intent(this, LanguagesActivity::class.java))
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

    }
 
    override fun onDestroy() {
        AppEventLogger.trackScreenDestroy(this, "MenuScreen")
        super.onDestroy()
    }
}