package com.worldclock.app_themes.ads.helpers.usecases

import com.worldclock.app_themes.BuildConfig
import com.worldclock.app_themes.ads.config.AdControlConfigManager
import com.worldclock.app_themes.ads.config.RemoteScreens
import com.worldclock.app_themes.ads.helpers.AdConfigInitializer
import com.worldclock.app_themes.ads.utils.ADS
import timber.log.Timber
import javax.inject.Inject

class AdConfigRepositoryImpl @Inject constructor(
    private val adConfigInitializer: AdConfigInitializer,
    private val adControlConfigManager: AdControlConfigManager
) : AdConfigRepository {

    @Volatile
    private var configLoaded = false

    override fun initialize(onReady: () -> Unit, onFailed: () -> Unit) {
        adConfigInitializer.setListener(
            onReady = {
                configLoaded = true
                onReady()
            },
            onFailed = {
                configLoaded = false
                Timber.w("Ad configuration failed to load")
                onFailed()
            }
        )
    }

    override fun isConfigLoaded(): Boolean = configLoaded

    override fun getInterstitialAdUnitId(screen: String, trigger: String): String {
        if (BuildConfig.DEBUG) {
            return when (screen) {
                RemoteScreens.SPLASH_SCREEN -> ADS.TEST_ADMOB_INTERSTITIAL_SPLASH_AD_ID
                RemoteScreens.INTRO_SCREEN, "OnBoardingScreen" -> ADS.TEST_ADMOB_INTERSTITIAL_ONBOARDING_AD_ID
                RemoteScreens.LANGUAGE_SCREEN -> ADS.TEST_ADMOB_INTERSTITIAL_LANGUAGE_AD_ID
                else -> ADS.TEST_ADMOB_INTERSTITIAL_AD_ID
            }
        }
        return adControlConfigManager.getProdInterstitialAdUnitId(
            screen,
            trigger,
            fallbackInterstitialId(screen, trigger)
        )
    }

    override fun getBannerAdUnitId(screen: String, position: String): String {
        return if (BuildConfig.DEBUG) {
            ADS.TEST_ADMOB_BANNER_AD_ID
        } else {
            adControlConfigManager.getProdBannerAdUnitId(
                screen,
                position,
                fallbackBannerId(screen, position)
            )
        }
    }

    override fun getNativeAdUnitId(screen: String, position: String): String {
        if (BuildConfig.DEBUG) {
            return when (screen) {
                RemoteScreens.INTRO_SCREEN, "OnBoardingScreen", "IntroFullScreen" -> ADS.TEST_ADMOB_NATIVE_ONBOARDING_AD_ID
                RemoteScreens.SPLASH_SCREEN -> ADS.TEST_ADMOB_NATIVE_SPLASH_AD_ID
                RemoteScreens.LANGUAGE_SCREEN -> ADS.TEST_ADMOB_NATIVE_LANGUAGE_AD_ID
                else -> ADS.TEST_ADMOB_NATIVE_AD_ID
            }
        }
        return adControlConfigManager.getProdNativeAdUnitId(
            screen,
            position,
            fallbackNativeId(screen, position)
        )
    }

    private fun fallbackInterstitialId(screen: String, trigger: String): String {
        return when {
            screen == RemoteScreens.SPLASH_SCREEN -> ADS.PROD_ADMOB_INTERSTITIAL_SPLASH_AD_ID
            screen == RemoteScreens.LANGUAGE_SCREEN -> ADS.PROD_ADMOB_INTERSTITIAL_LANGUAGE_AD_ID
            screen == "PremiumScreen" || trigger == "close" -> ADS.PROD_ADMOB_INTERSTITIAL_PREMIUM_AD_ID
            screen == "HomeScreen" && trigger == "exit_interstitial" -> ADS.PROD_ADMOB_INTERSTITIAL_EXIT_AD_ID
            else -> ADS.PROD_ADMOB_INTERSTITIAL_AD_ID
        }
    }

    private fun fallbackBannerId(screen: String, position: String): String {
        return when {
            (screen == RemoteScreens.INTRO_SCREEN || screen == "OnBoardingScreen") && position == "bottom" ->
                ADS.PROD_ADMOB_BANNER_ONBOARDING_INLINE_AD_ID
            else -> ADS.PROD_ADMOB_BANNER_AD_ID
        }
    }

    private fun fallbackNativeId(screen: String, position: String): String {
        return when {
            screen == RemoteScreens.SPLASH_SCREEN -> ADS.PROD_ADMOB_NATIVE_SPLASH_AD_ID
            screen == RemoteScreens.LANGUAGE_SCREEN -> ADS.PROD_ADMOB_NATIVE_LANGUAGE_AD_ID
            (screen == RemoteScreens.INTRO_SCREEN || screen == "OnBoardingScreen" || screen == "IntroFullScreen") &&
                (position.equals("full_screen", ignoreCase = true) || position.equals("full_screen_1", ignoreCase = true)) ->
                ADS.PROD_ADMOB_NATIVE_ONBOARDING_FULLSCREEN_AD_ID
            (screen == RemoteScreens.INTRO_SCREEN || screen == "OnBoardingScreen" || screen == "IntroFullScreen") &&
                position.equals("full_screen_2", ignoreCase = true) ->
                ADS.PROD_ADMOB_NATIVE_ONBOARDING_FULLSCREEN_AD_ID_2
            screen == RemoteScreens.INTRO_SCREEN || screen == "OnBoardingScreen" ->
                ADS.PROD_ADMOB_NATIVE_ONBOARDING_AD_ID
            screen == "ExitScreen" -> ADS.PROD_ADMOB_NATIVE_EXIT_AD_ID
            screen in setOf(
                "HomeScreen",
                "ExploreScreen",
                "LiveScreen",
                "LiveWallpaperScreen",
                "CategoryScreen",
                "MenuScreen",
                "SettingsScreen",
                "ApplyScreen",
                "DescribeScreen",
                "PurchaseScreen",
                "PremiumScreen"
            ) -> ADS.PROD_ADMOB_NATIVE_INSIDE_AD_ID
            else -> ADS.PROD_ADMOB_NATIVE_AD_ID
        }
    }
}
