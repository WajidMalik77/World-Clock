package com.worldclock.app_themes.ads.config

import android.content.Context
import android.content.Intent
import timber.log.Timber
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.worldclock.app_themes.BuildConfig
import com.worldclock.app_themes.ads.config.models.AdControlConfig
import com.worldclock.app_themes.ads.helpers.models.AdNetwork
import com.worldclock.app_themes.ads.helpers.models.AdWaterfallPlan
import com.worldclock.app_themes.ads.utils.ADS
import com.worldclock.app_themes.core.utils.AdsConstants
import com.worldclock.app_themes.core.utils.PrefUtil
import com.worldclock.app_themes.presentation.activities.LanguagesActivity
import com.worldclock.app_themes.presentation.activities.OnBoardingActivity
import com.worldclock.app_themes.presentation.activities.ActivityPurchase
import com.worldclock.app_themes.presentation.activities.PremiumActivity
import com.worldclock.app_themes.presentation.activities.MainActivity
import kotlinx.serialization.json.Json

class AdControlConfigManager(
    firebaseRemoteConfig: FirebaseRemoteConfig
) : BaseRemoteConfigManager<AdControlConfig>(firebaseRemoteConfig, "Config_v6") {

    companion object {
        private const val TAG_CFG = "ConfigTrace"
        private const val FORCE_REMOTE_ADS_ON_FOR_TESTING = false
        private val jsonParser by lazy {
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
        }
    }

    init {
        configData = AdControlConfig()
        val timeOut = if (BuildConfig.DEBUG) 0 else 300
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(timeOut.toLong())
            .build()

        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)
            .addOnFailureListener {
                Timber.e(it, "Remote Config settings failed")
            }
    }

    fun fetchAdConfig() {
        fetchConfig()
    }

    override fun parseJson(json: String): AdControlConfig? {
        Timber.tag(TAG_CFG).d("AdControl parseJson start length=${json.length}")
        return try {
            val parsed = jsonParser.decodeFromString<AdControlConfig>(json)
            Timber.tag(TAG_CFG).d("AdControl parseJson success appNull=${parsed.app == null} appOpenEnabled=${parsed.app.appOpen.resume} bannerEnabled=${parsed.banner.enabled} interEnabled=${parsed.interstitial.enabled}"
            )
            parsed
        } catch (e: Exception) {
            Timber.tag(TAG_CFG).e("AdControl parseJson serialization error: ${e.localizedMessage}")
            null
        }
    }

    override fun postProcessParsedData(json: String) {
        val cfg = getConfigV2()
        Timber.tag(TAG_CFG).d("AdControl postProcess appAds=${cfg?.app?.ads} appOpenSplash=${cfg?.app?.appOpen?.splash} appOpenResume=${cfg?.app?.appOpen?.resume} minBgSec=${getResumeMinBackgroundSeconds()} shouldResume=${shouldShowAppOpenResume()} shouldSplash=${shouldShowAppOpenSplash()} rawPrefix=${json.take(180)}"
        )
    }

    private fun getConfigV2() = configData

    private fun remoteAppOpenIds(): Map<String, String> = configData?.adIds?.appOpen.orEmpty()
    private fun remoteInterstitialIds(): Map<String, String> = configData?.adIds?.interstitial.orEmpty()
    private fun remoteBannerIds(): Map<String, String> = configData?.adIds?.banner.orEmpty()
    private fun remoteNativeIds(): Map<String, String> = configData?.adIds?.nativeIds.orEmpty()
    private fun remoteFbAppOpenIds(): Map<String, String> = configData?.fbAdIds?.appOpen.orEmpty()
    private fun remoteFbInterstitialIds(): Map<String, String> = configData?.fbAdIds?.interstitial.orEmpty()
    private fun remoteFbBannerIds(): Map<String, String> = configData?.fbAdIds?.banner.orEmpty()
    private fun remoteFbNativeIds(): Map<String, String> = configData?.fbAdIds?.nativeIds.orEmpty()

    private fun resolveProdAdId(configuredId: String?, fallbackProdId: String): String {
        if (BuildConfig.DEBUG) return fallbackProdId
        val candidate = configuredId?.trim().orEmpty()
        return candidate.ifEmpty { fallbackProdId }
    }

    private fun resolveProdFbAdId(configuredId: String?, fallbackProdId: String): String {
        val candidate = configuredId?.trim().orEmpty()
        return candidate.ifEmpty { fallbackProdId }
    }

    fun getProdBannerAdUnitId(screen: String, position: String, fallbackProdId: String): String {
        return resolveProdAdId(
            configuredId = resolveAdIdFromMap(
                ids = remoteBannerIds(),
                primaryCandidates = placementCandidatesForScreenPosition(screen, position),
                fallbackCandidates = legacyBannerAdIdCandidates(screen)
            ),
            fallbackProdId = fallbackProdId
        )
    }

    fun getProdAppOpenSplashAdUnitId(fallbackProdId: String): String {
        val ids = remoteAppOpenIds()
        return resolveProdAdId(ids["splash"].orEmpty().ifEmpty { ids["default"].orEmpty() }, fallbackProdId)
    }

    fun getProdAppOpenResumeAdUnitId(fallbackProdId: String): String {
        val ids = remoteAppOpenIds()
        return resolveProdAdId(ids["resume"].orEmpty().ifEmpty { ids["default"].orEmpty() }, fallbackProdId)
    }

    fun getProdInterstitialAdUnitId(screen: String, trigger: String, fallbackProdId: String): String {
        return resolveProdAdId(
            configuredId = resolveAdIdFromMap(
                ids = remoteInterstitialIds(),
                primaryCandidates = placementCandidatesForScreenTrigger(screen, trigger),
                fallbackCandidates = legacyInterstitialAdIdCandidates(screen)
            ),
            fallbackProdId = fallbackProdId
        )
    }

    fun getProdNativeAdUnitId(screen: String, position: String, fallbackProdId: String): String {
        return resolveProdAdId(
            configuredId = resolveAdIdFromMap(
                ids = remoteNativeIds(),
                primaryCandidates = placementCandidatesForScreenPosition(screen, position),
                fallbackCandidates = legacyNativeAdIdCandidates(screen)
            ),
            fallbackProdId = fallbackProdId
        )
    }

    fun shouldShowScreen(screenName: String, isFirstLaunch: Boolean): Boolean {
        val screens = launchScreens(isFirstLaunch)
        if (screens == null) {
            return when (screenName) {
                "splash", "languages" -> true
                "intro", "premium" -> isFirstLaunch
                else -> false
            }
        }

        return when (screenName) {
            "splash" -> screens.splash == 1
            "languages" -> screens.languages == 1
            "intro" -> screens.intro == 1
            "premium" -> (screens.premium ?: 0) > 0
            else -> false
        }
    }

    fun getPremiumScreenMode(isFirstLaunch: Boolean): Int {
        val screens = launchScreens(isFirstLaunch)
            ?: return if (isFirstLaunch) 1 else 0
        return screens.premium ?: 0
    }

    fun getGoProPremiumScreenMode(): Int {
        return configData?.app?.screens?.goPro ?: getPremiumScreenMode(isFirstLaunch = false)
    }

    fun getSettingsPremiumScreenMode(): Int {
        val screens = configData?.app?.screens
        return screens?.settingsPremium
            ?: screens?.settingsPremiumSnake
            ?: getPremiumScreenMode(isFirstLaunch = false)
    }

    private fun launchScreens(isFirstLaunch: Boolean) = if (isFirstLaunch) {
        configData?.app?.screens?.first
    } else {
        configData?.app?.screens?.second
    }

    fun shouldShowLanguagesFirst() = shouldShowScreen("languages", true)
    fun shouldShowIntroFirst() = shouldShowScreen("intro", true)
    fun shouldShowPremiumFirst() = shouldShowScreen("premium", true)

    fun shouldShowLanguagesSecond() = shouldShowScreen("languages", false)
    fun shouldShowIntroSecond() = shouldShowScreen("intro", false)
    fun shouldShowPremiumSecond() = shouldShowScreen("premium", false)

    fun areAdsEnabled(): Boolean {
        return isRemoteAdsOverrideEnabled() || configData?.app?.ads == 1
    }

    fun shouldShowAppOpen(): Boolean {
        val app = configData?.app ?: return false
        return areAdsEnabled() && (isRemoteAdsOverrideEnabled() || app.appOpen.resume in 1..2)
    }
    fun shouldShowAppOpenSplash(): Boolean {
        val app = configData?.app ?: return false
        return areAdsEnabled() && (isRemoteAdsOverrideEnabled() || app.appOpen.splash in 1..2)
    }
    fun shouldShowAppOpenResume(): Boolean {
        val app = configData?.app ?: return false
        return areAdsEnabled() && (isRemoteAdsOverrideEnabled() || app.appOpen.resume in 1..2)
    }
    fun getResumeMinBackgroundSeconds(): Int =
        configData?.app?.appOpen?.resumeMinBackgroundSeconds
            ?: 0

    fun isBannerVisible(activityName: String, position: String): Boolean {
        if (position.equals("bottom", ignoreCase = true)) return false
        if (!areAdsEnabled()) return false

        val v2 = resolveBannerPlacementValue(activityName, position)
        return (v2 ?: 0) > 0
    }

    fun getBannerType(activityName: String, position: String): String {
        if (position.equals("bottom", ignoreCase = true)) return "a"
        return "a"
    }

    fun isInterstitialEnabledForTrigger(screen: String, trigger: String): Boolean {
        if (configData == null) {
            Timber.tag(TAG_CFG).d("Interstitial gate OFF (config missing) screen=$screen trigger=$trigger")
            return false
        }
        val v2 = resolveInterstitialPlacementValue(screen, trigger)
        if (v2 != null) {
            val enabled = isInterstitialEnabled() && v2 in 1..2
            Timber.tag(TAG_CFG).d("Interstitial gate v2 screen=$screen trigger=$trigger placement=$v2 enabled=$enabled")
            return enabled
        }
        Timber.tag(TAG_CFG).d("Interstitial gate OFF (placement missing) screen=$screen trigger=$trigger")
        return false
    }

    fun isInterstitialThresholdReached(counter: Int): Boolean {
        val showAfter = getInterstitialClickInterval()
        return counter >= showAfter
    }

    fun isInterstitialEnabled(): Boolean {
        val cfg = configData ?: return false
        return areAdsEnabled() && (isRemoteAdsOverrideEnabled() || cfg.interstitial?.enabled == 1)
    }

    fun getInterstitialClickInterval(): Int {
        return configData?.interstitial?.clickInterval
            ?: 2
    }

    fun isInterFirstCountEnabledForHome(): Boolean {
        return (configData?.interstitial?.isInterFirstCount
            ?: 1) == 1
    }

    fun getInterstitialCooldownSeconds(): Int {
        return configData?.interstitial?.cooldownSeconds
            ?: 0
    }

    fun isPreHomeScreen(screen: String): Boolean {
        return screen in setOf("SplashScreen", "LanguagesScreen", "IntroScreen", "PremiumScreen")
    }

    private fun resolveBannerPlacementValue(screen: String, position: String): Int? {
        val cfg = configData?.banner ?: return null
        if (!isRemoteAdsOverrideEnabled() && cfg.enabled != 1) return 0
        for (candidate in placementCandidatesForScreenPosition(screen, position)) {
            cfg.placements[candidate]?.let { return it }
        }
        return if (isRemoteAdsOverrideEnabled()) 1 else null
    }

    private fun resolveInterstitialPlacementValue(screen: String, trigger: String): Int? {
        val cfg = configData?.interstitial ?: return null
        if (!isRemoteAdsOverrideEnabled() && cfg.enabled != 1) return 0
        for (candidate in placementCandidatesForScreenTrigger(screen, trigger)) {
            cfg.placements[candidate]?.let { return it }
        }
        return if (isRemoteAdsOverrideEnabled()) 1 else null
    }

    fun isRemoteAdsOverrideEnabled(): Boolean {
        return FORCE_REMOTE_ADS_ON_FOR_TESTING
    }

    private fun placementCandidatesForScreenPosition(screen: String, position: String): List<String> {
        val normalizedPosition = normalizeKey(position)
        val out = linkedSetOf<String>()
        when (screen) {
            RemoteScreens.LANGUAGE_SCREEN -> out += "languages_$normalizedPosition"
            RemoteScreens.INTRO_SCREEN, "OnBoardingScreen" -> out += "intro_$normalizedPosition"
            "IntroFullScreen" -> out += "intro_full_$normalizedPosition"
            "HomeScreen" -> out += "home_$normalizedPosition"
            "ExitScreen" -> out += "exit_$normalizedPosition"
            "MenuScreen" -> {
                out += "menu_$normalizedPosition"
                out += "settings_$normalizedPosition"
            }
            "SplashScreen" -> out += "splash_$normalizedPosition"
            "ClockScreen" -> out += "clock_$normalizedPosition"
            "AddClockScreen" -> out += "add_clock_$normalizedPosition"
            "AlarmScreen" -> out += "alarm_$normalizedPosition"
            "AddAlarmScreen" -> out += "add_alarm_$normalizedPosition"
            "StopwatchScreen" -> out += "stopwatch_$normalizedPosition"
            "TimerScreen" -> out += "timer_$normalizedPosition"
            "CompassScreen" -> out += "compass_$normalizedPosition"
            "WidgetScreen" -> out += "widget_$normalizedPosition"
            "AddWidgetScreen" -> out += "add_widget_$normalizedPosition"
            "AllRemindersScreen" -> out += "all_reminders_$normalizedPosition"
            "AddReminderScreen" -> out += "add_reminder_$normalizedPosition"
            "SleepSoundScreen" -> out += "sleep_sound_$normalizedPosition"
            "PlaySoundScreen" -> out += "play_sound_$normalizedPosition"
            "AddAllRemindersScreen" -> out += "add_all_reminders_$normalizedPosition"
        }
        
        val allowedKeys = setOf(
            "languages_top", "languages_bottom",
            "intro_top", "intro_bottom", "intro_full_screen",
            "home_top", "home_center", "home_bottom",
            "exit_top", "exit_bottom",
            "menu_top", "menu_bottom", "settings_bottom",
            "splash_top", "splash_bottom",
            "clock_top", "clock_bottom",
            "add_clock_top", "add_clock_bottom",
            "alarm_top", "alarm_bottom",
            "add_alarm_top", "add_alarm_bottom",
            "stopwatch_top", "stopwatch_bottom",
            "timer_top", "timer_bottom",
            "compass_top", "compass_bottom",
            "widget_top", "widget_bottom",
            "add_widget_top", "add_widget_bottom",
            "all_reminders_top", "all_reminders_bottom",
            "add_reminder_top", "add_reminder_bottom",
            "sleep_sound_top", "sleep_sound_bottom",
            "play_sound_top", "play_sound_bottom",
            "add_all_reminders_top", "add_all_reminders_bottom"
        )
        return out.filter { it in allowedKeys }
    }

    private fun placementCandidatesForScreenTrigger(screen: String, trigger: String): List<String> {
        val normalizedTrigger = normalizeKey(trigger)
        val out = linkedSetOf<String>()
        val baseScreen = normalizeKey(screen.removeSuffix("Screen"))
        if (baseScreen.isNotBlank()) {
            out += "${baseScreen}_$normalizedTrigger"
        }
        out += normalizedTrigger

        when (screen) {
            "SplashScreen" -> out += "splash"
            "LanguagesScreen" -> Unit
            "IntroScreen", "OnBoardingScreen" -> {
                if (normalizedTrigger == "finish") out += "intro_finish"
            }
            "PremiumScreen" -> {
                if (normalizedTrigger == "close") out += "premium_close"
            }
            "PurchaseScreen" -> {
                if (normalizedTrigger == "close") out += "purchase_close"
                if (normalizedTrigger == "continue_with_ads") out += "purchase_continue_with_ads"
            }
            "HomeScreen" -> {
                if (normalizedTrigger == "back") out += "home_back"
            }
        }
        
        val allowedKeys = setOf(
            "splash",
            "language_first_done",
            "language_second_done",
            "intro_finish",
            "premium_close",
            "purchase_continue_with_ads",
            "purchase_close",
            "home_back",
            "home_clock",
            "home_alarm",
            "home_stopwatch",
            "home_timer",
            "home_compass",
            "home_widget",
            "home_reminders",
            "home_sleep_sound",
            "clock_add_clock",
            "add_clock_save",
            "alarm_add_alarm",
            "alarm_edit_alarm",
            "add_alarm_save",
            "all_reminders_category",
            "add_all_reminders_add",
            "add_all_reminders_edit",
            "add_reminder_save",
            "sleep_sound_play"
        )
        val filtered = out.filter { it in allowedKeys }
        Timber.tag(TAG_CFG).d("Interstitial candidates screen=$screen trigger=$trigger candidates=$filtered")
        return filtered
    }

    private fun resolveAdIdFromMap(
        ids: Map<String, String>,
        primaryCandidates: List<String>,
        fallbackCandidates: List<String>
    ): String {
        for (candidate in primaryCandidates + fallbackCandidates + listOf("default")) {
            val value = ids[candidate]?.trim().orEmpty()
            if (value.isNotEmpty()) return value
        }
        return ""
    }

    private fun legacyBannerAdIdCandidates(screen: String): List<String> {
        return when (screen) {
            RemoteScreens.SPLASH_SCREEN -> listOf("splash")
            RemoteScreens.LANGUAGE_SCREEN -> listOf("language")
            RemoteScreens.INTRO_SCREEN, "OnBoardingScreen" -> listOf("intro", "onboarding")
            else -> emptyList()
        }
    }

    private fun legacyInterstitialAdIdCandidates(screen: String): List<String> {
        return when (screen) {
            RemoteScreens.SPLASH_SCREEN -> listOf("splash", "splash_interstitial")
            RemoteScreens.LANGUAGE_SCREEN -> listOf("language", "language_interstitial")
            RemoteScreens.INTRO_SCREEN -> listOf("intro", "onboarding_interstitial")
            "OnBoardingScreen" -> listOf("onboarding", "intro", "onboarding_interstitial")
            else -> emptyList()
        }
    }

    private fun legacyNativeAdIdCandidates(screen: String): List<String> {
        return when (screen) {
            RemoteScreens.SPLASH_SCREEN -> listOf("splash", "splash_native")
            RemoteScreens.LANGUAGE_SCREEN -> listOf("language", "language_native")
            RemoteScreens.INTRO_SCREEN -> listOf("intro", "onboarding_native")
            "OnBoardingScreen" -> listOf("onboarding", "intro", "onboarding_native")
            "IntroFullScreen" -> listOf("intro_full", "intro", "onboarding_native")
            else -> emptyList()
        }
    }

    private fun normalizeKey(value: String): String {
        return value
            .replace(Regex("([a-z0-9])([A-Z])"), "$1_$2")
            .replace("-", "_")
            .replace(" ", "_")
            .lowercase()
    }

    private fun adNetworkFromValue(value: Int): AdNetwork = when (value) {
        1 -> AdNetwork.ADMOB
        2 -> AdNetwork.FACEBOOK
        else -> AdNetwork.NONE
    }

    private fun resolveWaterfallPlan(value: Int, waterfallFlag: Int): AdWaterfallPlan? {
        val primary = adNetworkFromValue(value)
        if (primary == AdNetwork.NONE) return null
        val fallback = if (waterfallFlag == 1) {
            if (primary == AdNetwork.ADMOB) AdNetwork.FACEBOOK else AdNetwork.ADMOB
        } else {
            null
        }
        return AdWaterfallPlan(primary = primary, fallback = fallback)
    }

    fun getBannerWaterfallPlan(screen: String, position: String): AdWaterfallPlan? {
        if (position.equals("bottom", ignoreCase = true)) return null
        val value = resolveBannerPlacementValue(screen, position) ?: return null
        return resolveWaterfallPlan(value, configData?.banner?.waterfall ?: 0)
    }

    fun getInterstitialWaterfallPlan(screen: String, trigger: String): AdWaterfallPlan? {
        val value = resolveInterstitialPlacementValue(screen, trigger) ?: return null
        return resolveWaterfallPlan(value, configData?.interstitial?.waterfall ?: 0)
    }

    fun getAppOpenWaterfallPlan(type: String): AdWaterfallPlan? {
        val appOpen = configData?.app?.appOpen ?: return null
        val value = when (type) {
            "splash" -> appOpen.splash
            "resume" -> appOpen.resume
            else -> return null
        }
        return resolveWaterfallPlan(value, appOpen.waterfall)
    }

    fun getProdFbBannerAdUnitId(screen: String, position: String): String {
        return resolveProdFbAdId(
            configuredId = resolveAdIdFromMap(
                ids = remoteFbBannerIds(),
                primaryCandidates = placementCandidatesForScreenPosition(screen, position),
                fallbackCandidates = legacyBannerAdIdCandidates(screen)
            ),
            fallbackProdId = ADS.PROD_FB_BANNER_AD_ID
        )
    }

    fun getProdFbInterstitialAdUnitId(screen: String, trigger: String): String {
        return resolveProdFbAdId(
            configuredId = resolveAdIdFromMap(
                ids = remoteFbInterstitialIds(),
                primaryCandidates = placementCandidatesForScreenTrigger(screen, trigger),
                fallbackCandidates = legacyInterstitialAdIdCandidates(screen)
            ),
            fallbackProdId = ADS.PROD_FB_INTERSTITIAL_AD_ID
        )
    }

    fun getProdFbNativeAdUnitId(screen: String, position: String): String {
        return resolveProdFbAdId(
            configuredId = resolveAdIdFromMap(
                ids = remoteFbNativeIds(),
                primaryCandidates = placementCandidatesForScreenPosition(screen, position),
                fallbackCandidates = legacyNativeAdIdCandidates(screen)
            ),
            fallbackProdId = ADS.PROD_FB_NATIVE_AD_ID
        )
    }

    fun getProdFbAppOpenSplashAdUnitId(): String {
        val ids = remoteFbAppOpenIds()
        return resolveProdFbAdId(
            configuredId = ids["splash"].orEmpty().ifEmpty { ids["default"].orEmpty() },
            fallbackProdId = ADS.PROD_FB_APP_OPEN_AD_ID
        )
    }

    fun getProdFbAppOpenResumeAdUnitId(): String {
        val ids = remoteFbAppOpenIds()
        return resolveProdFbAdId(
            configuredId = ids["resume"].orEmpty().ifEmpty { ids["default"].orEmpty() },
            fallbackProdId = ADS.PROD_FB_APP_OPEN_AD_ID
        )
    }

    fun getNextScreenIntent(context: Context, currentScreen: String): Intent {
        val isFirstLaunch = !context.getSharedPreferences(AdsConstants.PrefsName, Context.MODE_PRIVATE)
            .getBoolean(AdsConstants.isFirstTime, false)

        val isPremium = PrefUtil(context).getBool("is_premium", false)
            || context.getSharedPreferences(AdsConstants.LifeTimePref, 0).getBoolean("premium", false)

        if (isPremium) {
            return Intent(context, MainActivity::class.java)
        }

        var step = when (currentScreen) {
            "splash" -> 0
            "languages" -> 1
            "intro" -> 2
            "premium" -> 3
            else -> 4
        }

        while (step < 4) {
            step++
            when (step) {
                1 -> {
                    if (shouldShowScreen("languages", isFirstLaunch)) {
                        return Intent(context, LanguagesActivity::class.java).putExtra("isSplash", true)
                    }
                }
                2 -> {
                    if (shouldShowScreen("intro", isFirstLaunch)) {
                        return Intent(context, OnBoardingActivity::class.java)
                    }
                }
                3 -> {
                    val mode = getPremiumScreenMode(isFirstLaunch)
                    if (mode == 1) {
                        return Intent(context, ActivityPurchase::class.java).putExtra("isSplash", true)
                    } else if (mode == 2) {
                        return Intent(context, PremiumActivity::class.java).putExtra("isSplash", true)
                    }
                }
                4 -> {
                    return Intent(context, MainActivity::class.java)
                }
            }
        }
        return Intent(context, MainActivity::class.java)
    }
}
