package com.worldclock.app_themes.ads.utils

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.worldclock.app_themes.R
import com.worldclock.app_themes.ads.utils.GetFirebase.adIDOnboarding_FullNative
import com.worldclock.app_themes.ads.utils.GetFirebase.enable_banner_native_ads
import com.worldclock.app_themes.ads.utils.GetFirebase.enable_on_demand_interstitial_onboarding
import com.worldclock.app_themes.ads.utils.GetFirebase.isAppOpenOnDemand
import com.worldclock.app_themes.ads.utils.GetFirebase.show_full_screen_native
import com.worldclock.app_themes.ads.utils.GetFirebase.time_delay_for_ondemand_appopen
import org.json.JSONObject

object FetchRemoteConfig {

    fun fetchAndApply(onComplete: () -> Unit) {
        val remoteConfig = FirebaseRemoteConfig.getInstance()

        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0) // 1 hour in production, use 0 for debugging
            .build()

        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            applyAll(remoteConfig)
            onComplete()
        }
    }

    fun loadCachedValues() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()




        remoteConfig.activate().addOnCompleteListener {



            applyAll(remoteConfig)
        }
    }

    private fun applyAll(rc: FirebaseRemoteConfig) {
        if (rc.getString("interstitials").isEmpty()) return
        // ══════════════════════════════════════
        // INTERSTITIAL TRANSITIONS
        // ══════════════════════════════════════

        val interJson = rc.getString("interstitials")
        val jsonInterstitial = JSONObject(interJson)

        GetFirebase.transition_PremiumBack = jsonInterstitial.getLong("transition_PremiumBack").toInt()
        GetFirebase.transition_AddAlarmBack = jsonInterstitial.getLong("transition_AddAlarmBack").toInt()
        GetFirebase.transition_splash_ad_type = jsonInterstitial.getLong("transition_splash_ad_type").toInt()
        GetFirebase.transition_AddAllRemindersBack = jsonInterstitial.getLong("transition_AddAllRemindersBack").toInt()
        GetFirebase.transition_AddClockBack = jsonInterstitial.getLong("transition_AddClockBack").toInt()
        GetFirebase.transition_AddReminderBack = jsonInterstitial.getLong("transition_AddReminderBack").toInt()
        GetFirebase.transition_AlarmActivityBack = jsonInterstitial.getLong("transition_AlarmActivityBack").toInt()
        GetFirebase.transition_AllRemindersBack = jsonInterstitial.getLong("transition_AllRemindersBack").toInt()
        GetFirebase.transition_ClockBack = jsonInterstitial.getLong("transition_ClockBack").toInt()
        GetFirebase.transition_CompassBack = jsonInterstitial.getLong("transition_CompassBack").toInt()
        GetFirebase.transition_LanguagesBack = jsonInterstitial.getLong("transition_LanguagesBack").toInt()
        GetFirebase.transition_MainBack = jsonInterstitial.getLong("transition_MainBack").toInt()
        GetFirebase.transition_MenuBack = jsonInterstitial.getLong("transition_MenuBack").toInt()
        GetFirebase.transition_PlaySoundBack = jsonInterstitial.getLong("transition_PlaySoundBack").toInt()
        GetFirebase.transition_SleepSoundBack = jsonInterstitial.getLong("transition_SleepSoundBack").toInt()
        GetFirebase.transition_StopWatchBack = jsonInterstitial.getLong("transition_StopWatchBack").toInt()
        GetFirebase.transition_TimerBack = jsonInterstitial.getLong("transition_TimerBack").toInt()
        GetFirebase.transition_WidgetBack = jsonInterstitial.getLong("transition_WidgetBack").toInt()

        GetFirebase.transition_AddAllRemindersForward = jsonInterstitial.getLong("transition_AddAllRemindersForward").toInt()
        GetFirebase.transition_AlarmForward = jsonInterstitial.getLong("transition_AlarmForward").toInt()
        GetFirebase.transition_AllRemindersForward = jsonInterstitial.getLong("transition_AllRemindersForward").toInt()
        GetFirebase.transition_ClockForward = jsonInterstitial.getLong("transition_ClockForward").toInt()
        GetFirebase.transition_ExitForward = jsonInterstitial.getLong("transition_ExitForward").toInt()
        GetFirebase.transition_ExitBackpress = jsonInterstitial.getLong("transition_ExitBackpress").toInt()
        GetFirebase.transition_LanguageForward = jsonInterstitial.getLong("transition_LanguageForward").toInt()
        GetFirebase.transition_MainForward = jsonInterstitial.getLong("transition_MainForward").toInt()
        GetFirebase.transition_MenuForward = jsonInterstitial.getLong("transition_MenuForward").toInt()
        GetFirebase.transition_OnboardingForward = jsonInterstitial.getLong("transition_OnboardingForward").toInt()
        GetFirebase.transition_SleepSoundForward = jsonInterstitial.getLong("transition_SleepSoundForward").toInt()

        // ══════════════════════════════════════
        // BANNER AD VISIBILITY PER SCREEN
        // ══════════════════════════════════════

        val bannerJson = rc.getString("native_banner")
        val jsonBanner = JSONObject(bannerJson)

        GetFirebase.banner_ad_addalarm_top = jsonBanner.getLong("banner_ad_addalarm_top").toInt()
        GetFirebase.banner_ad_addalarm_bottom = jsonBanner.getLong("banner_ad_addalarm_bottom").toInt()
        GetFirebase.banner_ad_addallreminders_top = jsonBanner.getLong("banner_ad_addallreminders_top").toInt()
        GetFirebase.banner_ad_addallreminders_bottom = jsonBanner.getLong("banner_ad_addallreminders_bottom").toInt()
        GetFirebase.banner_ad_addclock_top = jsonBanner.getLong("banner_ad_addclock_top").toInt()
        GetFirebase.banner_ad_addclock_bottom = jsonBanner.getLong("banner_ad_addclock_bottom").toInt()
        GetFirebase.banner_ad_addreminder_top = jsonBanner.getLong("banner_ad_addreminder_top").toInt()
        GetFirebase.banner_ad_addreminder_bottom = jsonBanner.getLong("banner_ad_addreminder_bottom").toInt()

        GetFirebase.banner_ad_alarmactivity_top = jsonBanner.getLong("banner_ad_alarmactivity_top").toInt()
        GetFirebase.banner_ad_alarmactivity_bottom = jsonBanner.getLong("banner_ad_alarmactivity_bottom").toInt()
        GetFirebase.banner_ad_allreminders_top = jsonBanner.getLong("banner_ad_allreminders_top").toInt()
        GetFirebase.banner_ad_allreminders_bottom = jsonBanner.getLong("banner_ad_allreminders_bottom").toInt()
        GetFirebase.banner_ad_clockactivity_top = jsonBanner.getLong("banner_ad_clockactivity_top").toInt()
        GetFirebase.banner_ad_clockactivity_bottom = jsonBanner.getLong("banner_ad_clockactivity_bottom").toInt()
        GetFirebase.banner_ad_compassactivity_top = jsonBanner.getLong("banner_ad_compassactivity_top").toInt()
        GetFirebase.banner_ad_compassactivity_bottom = jsonBanner.getLong("banner_ad_compassactivity_bottom").toInt()
        GetFirebase.banner_ad_exitactivity_top = jsonBanner.getLong("banner_ad_exitactivity_top").toInt()
        GetFirebase.banner_ad_exitactivity_bottom = jsonBanner.getLong("banner_ad_exitactivity_bottom").toInt()
        GetFirebase.banner_ad_languagesactivity_top = jsonBanner.getLong("banner_ad_languagesactivity_top").toInt()
        GetFirebase.banner_ad_languagesactivity_bottom = jsonBanner.getLong("banner_ad_languagesactivity_bottom").toInt()
        GetFirebase.banner_ad_mainactivity_top = jsonBanner.getLong("banner_ad_mainactivity_top").toInt()
        GetFirebase.banner_ad_mainactivity_bottom = jsonBanner.getLong("banner_ad_mainactivity_bottom").toInt()
        GetFirebase.banner_ad_menuactivity_top = jsonBanner.getLong("banner_ad_menuactivity_top").toInt()
        GetFirebase.banner_ad_menuactivity_bottom = jsonBanner.getLong("banner_ad_menuactivity_bottom").toInt()
        GetFirebase.banner_ad_onboardingactivity_top = jsonBanner.getLong("banner_ad_onboardingactivity_top").toInt()
        GetFirebase.banner_ad_onboardingactivity_bottom = jsonBanner.getLong("banner_ad_onboardingactivity_bottom").toInt()
        GetFirebase.banner_ad_playsoundactivity_top = jsonBanner.getLong("banner_ad_playsoundactivity_top").toInt()
        GetFirebase.banner_ad_playsoundactivity_bottom = jsonBanner.getLong("banner_ad_playsoundactivity_bottom").toInt()
        GetFirebase.banner_ad_sleepsoundactivity_top = jsonBanner.getLong("banner_ad_sleepsoundactivity_top").toInt()
        GetFirebase.banner_ad_sleepsoundactivity_bottom = jsonBanner.getLong("banner_ad_sleepsoundactivity_bottom").toInt()
        GetFirebase.banner_ad_splashactivity_top = jsonBanner.getLong("banner_ad_splashactivity_top").toInt()
        GetFirebase.banner_ad_splashactivity_bottom = jsonBanner.getLong("banner_ad_splashactivity_bottom").toInt()
        GetFirebase.banner_ad_stopwatchactivity_top = jsonBanner.getLong("banner_ad_stopwatchactivity_top").toInt()
        GetFirebase.banner_ad_stopwatchactivity_bottom = jsonBanner.getLong("banner_ad_stopwatchactivity_bottom").toInt()
        GetFirebase.banner_ad_timeractivity_top = jsonBanner.getLong("banner_ad_timeractivity_top").toInt()
        GetFirebase.banner_ad_timeractivity_bottom = jsonBanner.getLong("banner_ad_timeractivity_bottom").toInt()
        GetFirebase.banner_ad_widgetactivity_top = jsonBanner.getLong("banner_ad_widgetactivity_top").toInt()
        GetFirebase.banner_ad_widgetactivity_bottom = jsonBanner.getLong("banner_ad_widgetactivity_bottom").toInt()

        // ══════════════════════════════════════
        // OTHERS / GLOBAL SETTINGS
        // ══════════════════════════════════════

        val miscJson = rc.getString("misc")
        val jsonMisc = JSONObject(miscJson)

        GetFirebase.open_ad_from_background_splash = jsonMisc.getBoolean("open_ad_from_background_splash")
        GetFirebase.open_ad_from_background = jsonMisc.getBoolean("open_ad_from_background")
        GetFirebase.toastForAds = jsonMisc.getBoolean("toastForAds")
        GetFirebase.enable_interstitial_ads = jsonMisc.getBoolean("enable_interstitial_ads")
        GetFirebase.enable_appopen_ads = jsonMisc.getBoolean("enable_appopen_ads")
        GetFirebase.use_counter = jsonMisc.getBoolean("use_counter")
        GetFirebase.MIN_INTERVAL_MS = jsonMisc.getLong("MIN_INTERVAL_MS").toInt()
        GetFirebase.time_delay_for_preloaded_interstitial = jsonMisc.getLong("time_delay_for_preloaded_interstitial")
        GetFirebase.time_delay_for_ondemand_interstitial = jsonMisc.getLong("time_delay_for_ondemand_interstitial")
        GetFirebase.show_language_for_retained_user = jsonMisc.getBoolean("show_language_for_retained_user")
        GetFirebase.show_premium_for_retained_user = jsonMisc.getBoolean("show_premium_for_retained_user")
        GetFirebase.enable_on_demand_interstitial = jsonMisc.getLong("enable_on_demand_interstitial").toInt()
        GetFirebase.counter_interval = jsonMisc.getLong("counter_interval").toInt()
        show_full_screen_native = jsonMisc.optBoolean("show_full_screen_native",false)
        enable_banner_native_ads = jsonMisc.optBoolean("enable_banner_native_ads",false)
        time_delay_for_ondemand_appopen = jsonMisc.optInt("time_delay_for_ondemand_appopen",5000)
        isAppOpenOnDemand = jsonMisc.optBoolean("isAppOpenOnDemand",false)
        GetFirebase.enable_on_demand_interstitial_splash = jsonMisc.optInt("enable_on_demand_interstitial_splash",0)
        GetFirebase.enable_on_demand_interstitial_language = jsonMisc.optInt("enable_on_demand_interstitial_language",0)
        GetFirebase.enable_on_demand_interstitial_inapp = jsonMisc.optInt("enable_on_demand_interstitial_inapp",0)
        enable_on_demand_interstitial_onboarding = jsonMisc.optInt("enable_on_demand_interstitial_onboarding",0)


        // ══════════════════════════════════════
        // NATIVE AD STYLING — SPLASH
        // ══════════════════════════════════════
        GetFirebase.native_ad_buttonheight_for_splash =jsonMisc.getDouble("native_ad_buttonheight_for_splash")
        GetFirebase.native_ad_buttontextcolor_for_splash =jsonMisc.getString("native_ad_buttontextcolor_for_splash")
        GetFirebase.native_ad_buttontextheight_for_splash =jsonMisc.getDouble("native_ad_buttontextheight_for_splash")
        GetFirebase.native_ad_buttoncolor_for_splash =jsonMisc.getString("native_ad_buttoncolor_for_splash")
        GetFirebase.native_ad_headlinecolor_for_splash =jsonMisc.getString("native_ad_headlinecolor_for_splash")
        GetFirebase.native_ad_headlinetextheight_for_splash =jsonMisc.getDouble("native_ad_headlinetextheight_for_splash")
        GetFirebase.native_ad_othertextcolor_for_splash =jsonMisc.getString("native_ad_othertextcolor_for_splash")
        GetFirebase.native_ad_bodytextheight_for_splash =jsonMisc.getDouble("native_ad_bodytextheight_for_splash")
        GetFirebase.native_ad_splash_iconsize =jsonMisc.getLong("native_ad_splash_iconsize").toInt()
        GetFirebase.native_ad_backgroundcolor_for_splash =jsonMisc.getString("native_ad_backgroundcolor_for_splash")

        // ══════════════════════════════════════
        // NATIVE AD STYLING — ONBOARDING
        // ══════════════════════════════════════
        GetFirebase.native_ad_buttonheight_for_onboardingscreen =jsonMisc.getDouble("native_ad_buttonheight_for_onboardingscreen")
        GetFirebase.native_ad_buttontextcolor_for_onboardingscreen =jsonMisc.getString("native_ad_buttontextcolor_for_onboardingscreen")
        GetFirebase.native_ad_buttontextheight_for_onboardingscreen =jsonMisc.getDouble("native_ad_buttontextheight_for_onboardingscreen")
        GetFirebase.native_ad_buttoncolor_for_onboardingscreen =jsonMisc.getString("native_ad_buttoncolor_for_onboardingscreen")
        GetFirebase.native_ad_headlinecolor_for_onboardingscreen =jsonMisc.getString("native_ad_headlinecolor_for_onboardingscreen")
        GetFirebase.native_ad_headlinetextheight_for_onboarding =jsonMisc.getDouble("native_ad_headlinetextheight_for_onboarding")
        GetFirebase.native_ad_othertextcolor_for_onboardingscreen =jsonMisc.getString("native_ad_othertextcolor_for_onboardingscreen")
        GetFirebase.native_ad_bodytextheight_for_onboarding =jsonMisc.getDouble("native_ad_bodytextheight_for_onboarding")
        GetFirebase.native_ad_onboarding_iconsize =jsonMisc.getLong("native_ad_onboarding_iconsize").toInt()
        GetFirebase.native_ad_backgroundcolor_for_onboardingscreen =jsonMisc.getString("native_ad_backgroundcolor_for_onboardingscreen")

        // ══════════════════════════════════════
        // NATIVE AD STYLING — LANGUAGE
        // ══════════════════════════════════════
        GetFirebase.native_ad_buttonheight_for_languagescreen =jsonMisc.getDouble("native_ad_buttonheight_for_languagescreen")
        GetFirebase.native_ad_buttontextcolor_for_languagescreen =jsonMisc.getString("native_ad_buttontextcolor_for_languagescreen")
        GetFirebase.native_ad_buttontextheight_for_languagescreen =jsonMisc.getDouble("native_ad_buttontextheight_for_languagescreen")
        GetFirebase.native_ad_buttoncolor_for_languagescreen =jsonMisc.getString("native_ad_buttoncolor_for_languagescreen")
        GetFirebase.native_ad_headlinecolor_for_languagescreen =jsonMisc.getString("native_ad_headlinecolor_for_languagescreen")
        GetFirebase.native_ad_headlinetextheight_for_languagescreen =jsonMisc.getDouble("native_ad_headlinetextheight_for_languagescreen")
        GetFirebase.native_ad_othertextcolor_for_languagescreen =jsonMisc.getString("native_ad_othertextcolor_for_languagescreen")
        GetFirebase.native_ad_bodytextheight_for_languagescreen =jsonMisc.getDouble("native_ad_bodytextheight_for_languagescreen")
        GetFirebase.native_ad_language_iconsize =jsonMisc.getLong("native_ad_language_iconsize").toInt()
        GetFirebase.native_ad_backgroundcolor_for_languagescreen =jsonMisc.getString("native_ad_backgroundcolor_for_languagescreen")

        GetFirebase.native_ad_buttonheight_for_homescreen =jsonMisc.getDouble("native_ad_buttonheight_for_homescreen")
        GetFirebase.native_ad_buttontextcolor_for_homescreen =jsonMisc.getString("native_ad_buttontextcolor_for_homescreen")
        GetFirebase.native_ad_buttontextheight_for_homescreen =jsonMisc.getDouble("native_ad_buttontextheight_for_homescreen")
        GetFirebase.native_ad_buttoncolor_for_homescreen =jsonMisc.getString("native_ad_buttoncolor_for_homescreen")
        GetFirebase.native_ad_headlinecolor_for_homescreen =jsonMisc.getString("native_ad_headlinecolor_for_homescreen")
        GetFirebase.native_ad_headlinetextheight_for_homescreen =jsonMisc.getDouble("native_ad_headlinetextheight_for_homescreen")
        GetFirebase.native_ad_othertextcolor_for_homescreen =jsonMisc.getString("native_ad_othertextcolor_for_homescreen")
        GetFirebase.native_ad_bodytextheight_for_homescreen =jsonMisc.getDouble("native_ad_bodytextheight_for_homescreen")
        GetFirebase.native_ad_homescreen_iconsize =jsonMisc.getLong("native_ad_homescreen_iconsize").toInt()
        GetFirebase.native_ad_backgroundcolor_for_homescreen =jsonMisc.getString("native_ad_backgroundcolor_for_homescreen")





        // ══════════════════════════════════════
        // NATIVE AD STYLING — OTHER SCREENS
        // ══════════════════════════════════════
        GetFirebase.native_ad_buttonheight_for_otherscreens =jsonMisc.getDouble("native_ad_buttonheight_for_otherscreens")
        GetFirebase.native_ad_buttontextcolor_for_otherscreens =jsonMisc.getString("native_ad_buttontextcolor_for_otherscreens")
        GetFirebase.native_ad_buttontextheight_for_otherscreens =jsonMisc.getDouble("native_ad_buttontextheight_for_otherscreens")
        GetFirebase.native_ad_buttoncolor_for_otherscreens =jsonMisc.getString("native_ad_buttoncolor_for_otherscreens")
        GetFirebase.native_ad_headlinecolor_for_otherscreens =jsonMisc.getString("native_ad_headlinecolor_for_otherscreens")
        GetFirebase.native_ad_headlinetextheight_for_otherscreens =jsonMisc.getDouble("native_ad_headlinetextheight_for_otherscreens")
        GetFirebase.native_ad_othertextcolor_for_otherscreens =jsonMisc.getString("native_ad_othertextcolor_for_otherscreens")
        GetFirebase.native_ad_bodytextheight_for_otherscreens =jsonMisc.getDouble("native_ad_bodytextheight_for_otherscreens")
        GetFirebase.native_ad_other_iconsize =jsonMisc.getLong("native_ad_other_iconsize").toInt()
        GetFirebase.native_ad_backgroundcolor_for_otherscreens =jsonMisc.getString("native_ad_backgroundcolor_for_otherscreens")

        val adsJson = rc.getString("ad_ids")
        val jsonIds = JSONObject(adsJson)

        // ══════════════════════════════════════
        // AD IDs — INTERSTITIAL
        // ══════════════════════════════════════
        GetFirebase.adIdSplash_interstitial =jsonIds.getString("adIdSplash_interstitial")
        GetFirebase.adIdLanguage_interstitial =jsonIds.getString("adIdLanguage_interstitial")
        GetFirebase.adIdOnboarding_interstitial =jsonIds.getString("adIdOnboarding_interstitial")
        GetFirebase.adIdPremium_interstitial = jsonIds.getString("adIdPremium_interstitial")
        GetFirebase.adIdOther_interstitial = jsonIds.getString("adIdOther_interstitial")
        adIDOnboarding_FullNative = jsonIds.optString("adIDOnboarding_FullNative","")


        // ══════════════════════════════════════
        // AD IDs — APP OPEN
        // ══════════════════════════════════════
        GetFirebase.adIdSplash_appopen = jsonIds.getString("adIdSplash_appopen")
        GetFirebase.adIdBackground_appopen = jsonIds.getString("adIdBackground_appopen")

        // ══════════════════════════════════════
        // AD IDs — BANNER
        // ══════════════════════════════════════
        GetFirebase.adIdSplash_bannerTop = jsonIds.getString("adIdSplash_bannerTop")
        GetFirebase.adIdSplash_bannerBottom = jsonIds.getString("adIdSplash_bannerBottom")
        GetFirebase.adIdAddAlarm_bannerTop = jsonIds.getString("adIdAddAlarm_bannerTop")
        GetFirebase.adIdAddAlarm_bannerBottom = jsonIds.getString("adIdAddAlarm_bannerBottom")
        GetFirebase.adIdAddAllReminders_bannerTop = jsonIds.getString("adIdAddAllReminders_bannerTop")
        GetFirebase.adIdAddAllReminders_bannerBottom = jsonIds.getString("adIdAddAllReminders_bannerBottom")
        GetFirebase.adIdAddClock_bannerTop = jsonIds.getString("adIdAddClock_bannerTop")
        GetFirebase.adIdAddClock_bannerBottom = jsonIds.getString("adIdAddClock_bannerBottom")
        GetFirebase.adIdAddReminder_bannerTop = jsonIds.getString("adIdAddReminder_bannerTop")
        GetFirebase.adIdAddReminder_bannerBottom = jsonIds.getString("adIdAddReminder_bannerBottom")
        GetFirebase.adIdAlarm_bannerTop = jsonIds.getString("adIdAlarm_bannerTop")
        GetFirebase.adIdAlarm_bannerBottom = jsonIds.getString("adIdAlarm_bannerBottom")
        GetFirebase.adIdAllReminders_bannerTop = jsonIds.getString("adIdAllReminders_bannerTop")
        GetFirebase.adIdAllReminders_bannerBottom = jsonIds.getString("adIdAllReminders_bannerBottom")
        GetFirebase.adIdClock_bannerTop = jsonIds.getString("adIdClock_bannerTop")
        GetFirebase.adIdClock_bannerBottom = jsonIds.getString("adIdClock_bannerBottom")
        GetFirebase.adIdCompass_bannerTop = jsonIds.getString("adIdCompass_bannerTop")
        GetFirebase.adIdCompass_bannerBottom = jsonIds.getString("adIdCompass_bannerBottom")
        GetFirebase.adIdExit_bannerTop = jsonIds.getString("adIdExit_bannerTop")
        GetFirebase.adIdExit_bannerBottom = jsonIds.getString("adIdExit_bannerBottom")
        GetFirebase.adIdLanguagesActivity_bannerTop = jsonIds.getString("adIdLanguagesActivity_bannerTop")
        GetFirebase.adIdLanguagesActivity_bannerBottom = jsonIds.getString("adIdLanguagesActivity_bannerBottom")
        GetFirebase.adIdMainActivity_bannerTop = jsonIds.getString("adIdMainActivity_bannerTop")
        GetFirebase.adIdMainActivity_bannerBottom = jsonIds.getString("adIdMainActivity_bannerBottom")
        GetFirebase.adIdMenu_bannerTop = jsonIds.getString("adIdMenu_bannerTop")
        GetFirebase.adIdMenu_bannerBottom = jsonIds.getString("adIdMenu_bannerBottom")
        GetFirebase.adIdOnboarding_bannerTop = jsonIds.getString("adIdOnboarding_bannerTop")
        GetFirebase.adIdOnboarding_bannerBottom = jsonIds.getString("adIdOnboarding_bannerBottom")
        GetFirebase.adIdPlaySound_bannerTop = jsonIds.getString("adIdPlaySound_bannerTop")
        GetFirebase.adIdPlaySound_bannerBottom = jsonIds.getString("adIdPlaySound_bannerBottom")
        GetFirebase.adIdSleepSound_bannerTop = jsonIds.getString("adIdSleepSound_bannerTop")
        GetFirebase.adIdSleepSound_bannerBottom = jsonIds.getString("adIdSleepSound_bannerBottom")
        GetFirebase.adIdStopWatch_bannerTop = jsonIds.getString("adIdStopWatch_bannerTop")
        GetFirebase.adIdStopWatch_bannerBottom = jsonIds.getString("adIdStopWatch_bannerBottom")
        GetFirebase.adIdTimer_bannerTop = jsonIds.getString("adIdTimer_bannerTop")
        GetFirebase.adIdTimer_bannerBottom = jsonIds.getString("adIdTimer_bannerBottom")
        GetFirebase.adIdWidget_bannerTop = jsonIds.getString("adIdWidget_bannerTop")
        GetFirebase.adIdWidget_bannerBottom = jsonIds.getString("adIdWidget_bannerBottom")

        // ══════════════════════════════════════
        // AD IDs — NATIVE
        // ══════════════════════════════════════
        GetFirebase.adIdSplash_nativeTop = jsonIds.getString("adIdSplash_nativeTop")
        GetFirebase.adIdSplash_nativeBottom = jsonIds.getString("adIdSplash_nativeBottom")
        GetFirebase.adIdAddAlarm_nativeTop = jsonIds.getString("adIdAddAlarm_nativeTop")
        GetFirebase.adIdAddAlarm_nativeBottom = jsonIds.getString("adIdAddAlarm_nativeBottom")
        GetFirebase.adIdAddAllReminders_nativeTop = jsonIds.getString("adIdAddAllReminders_nativeTop")
        GetFirebase.adIdAddAllReminders_nativeBottom = jsonIds.getString("adIdAddAllReminders_nativeBottom")
        GetFirebase.adIdAddClock_nativeTop = jsonIds.getString("adIdAddClock_nativeTop")
        GetFirebase.adIdAddClock_nativeBottom = jsonIds.getString("adIdAddClock_nativeBottom")
        GetFirebase.adIdAddReminder_nativeTop = jsonIds.getString("adIdAddReminder_nativeTop")
        GetFirebase.adIdAddReminder_nativeBottom = jsonIds.getString("adIdAddReminder_nativeBottom")
        GetFirebase.adIdAlarm_nativeTop = jsonIds.getString("adIdAlarm_nativeTop")
        GetFirebase.adIdAlarm_nativeBottom = jsonIds.getString("adIdAlarm_nativeBottom")
        GetFirebase.adIdAllReminders_nativeTop = jsonIds.getString("adIdAllReminders_nativeTop")
        GetFirebase.adIdAllReminders_nativeBottom = jsonIds.getString("adIdAllReminders_nativeBottom")
        GetFirebase.adIdClock_nativeTop = jsonIds.getString("adIdClock_nativeTop")
        GetFirebase.adIdClock_nativeBottom = jsonIds.getString("adIdClock_nativeBottom")
        GetFirebase.adIdCompass_nativeTop = jsonIds.getString("adIdCompass_nativeTop")
        GetFirebase.adIdCompass_nativeBottom = jsonIds.getString("adIdCompass_nativeBottom")
        GetFirebase.adIdExit_nativeTop = jsonIds.getString("adIdExit_nativeTop")
        GetFirebase.adIdExit_nativeBottom = jsonIds.getString("adIdExit_nativeBottom")
        GetFirebase.adIdLanguagesActivity_nativeTop = jsonIds.getString("adIdLanguagesActivity_nativeTop")
        GetFirebase.adIdLanguagesActivity_nativeBottom = jsonIds.getString("adIdLanguagesActivity_nativeBottom")
        GetFirebase.adIdMainActivity_nativeTop = jsonIds.getString("adIdMainActivity_nativeTop")
        GetFirebase.adIdMainActivity_nativeBottom = jsonIds.getString("adIdMainActivity_nativeBottom")
        GetFirebase.adIdMenu_nativeTop = jsonIds.getString("adIdMenu_nativeTop")
        GetFirebase.adIdMenu_nativeBottom = jsonIds.getString("adIdMenu_nativeBottom")
        GetFirebase.adIdOnboarding_nativeTop = jsonIds.getString("adIdOnboarding_nativeTop")
        GetFirebase.adIdOnboarding_nativeBottom = jsonIds.getString("adIdOnboarding_nativeBottom")
        GetFirebase.adIdPlaySound_nativeTop = jsonIds.getString("adIdPlaySound_nativeTop")
        GetFirebase.adIdPlaySound_nativeBottom = jsonIds.getString("adIdPlaySound_nativeBottom")
        GetFirebase.adIdSleepSound_nativeTop = jsonIds.getString("adIdSleepSound_nativeTop")
        GetFirebase.adIdSleepSound_nativeBottom = jsonIds.getString("adIdSleepSound_nativeBottom")
        GetFirebase.adIdStopWatch_nativeTop = jsonIds.getString("adIdStopWatch_nativeTop")
        GetFirebase.adIdStopWatch_nativeBottom = jsonIds.getString("adIdStopWatch_nativeBottom")
        GetFirebase.adIdTimer_nativeTop = jsonIds.getString("adIdTimer_nativeTop")
        GetFirebase.adIdTimer_nativeBottom = jsonIds.getString("adIdTimer_nativeBottom")
        GetFirebase.adIdWidget_nativeTop = jsonIds.getString("adIdWidget_nativeTop")
        GetFirebase.adIdWidget_nativeBottom = jsonIds.getString("adIdWidget_nativeBottom")

        GetFirebase.adIdLanguagesActivity_bannerTop_home = jsonIds.optString("adIdLanguagesActivity_bannerTop_home","")
        GetFirebase.adIdLanguagesActivity_bannerBottom_home = jsonIds.optString("adIdLanguagesActivity_bannerBottom_home","")
        GetFirebase.adIdLanguagesActivity_nativeTop_home = jsonIds.optString("adIdLanguagesActivity_nativeTop_home","")
        GetFirebase.adIdLanguagesActivity_nativeBottom_home = jsonIds.optString("adIdLanguagesActivity_nativeBottom_home","")

        // ══════════════════════════════════════
        // AD IDs — COLLAPSIBLE
        // ══════════════════════════════════════
        GetFirebase.adIdSplash_collapsibleTop = jsonIds.getString("adIdSplash_collapsibleTop")
        GetFirebase.adIdSplash_collapsibleBottom = jsonIds.getString("adIdSplash_collapsibleBottom")
        GetFirebase.adIdAddAlarm_collapsibleTop = jsonIds.getString("adIdAddAlarm_collapsibleTop")
        GetFirebase.adIdAddAlarm_collapsibleBottom = jsonIds.getString("adIdAddAlarm_collapsibleBottom")
        GetFirebase.adIdAddAllReminders_collapsibleTop = jsonIds.getString("adIdAddAllReminders_collapsibleTop")
        GetFirebase.adIdAddAllReminders_collapsibleBottom = jsonIds.getString("adIdAddAllReminders_collapsibleBottom")
        GetFirebase.adIdAddClock_collapsibleTop = jsonIds.getString("adIdAddClock_collapsibleTop")
        GetFirebase.adIdAddClock_collapsibleBottom = jsonIds.getString("adIdAddClock_collapsibleBottom")
        GetFirebase.adIdAddReminder_collapsibleTop = jsonIds.getString("adIdAddReminder_collapsibleTop")
        GetFirebase.adIdAddReminder_collapsibleBottom = jsonIds.getString("adIdAddReminder_collapsibleBottom")

        GetFirebase.adIdAlarm_collapsibleTop = jsonIds.getString("adIdAlarm_collapsibleTop")
        GetFirebase.adIdAlarm_collapsibleBottom = jsonIds.getString("adIdAlarm_collapsibleBottom")
        GetFirebase.adIdAllReminders_collapsibleTop = jsonIds.getString("adIdAllReminders_collapsibleTop")
        GetFirebase.adIdAllReminders_collapsibleBottom = jsonIds.getString("adIdAllReminders_collapsibleBottom")
        GetFirebase.adIdClock_collapsibleTop = jsonIds.getString("adIdClock_collapsibleTop")
        GetFirebase.adIdClock_collapsibleBottom = jsonIds.getString("adIdClock_collapsibleBottom")
        GetFirebase.adIdCompass_collapsibleTop = jsonIds.getString("adIdCompass_collapsibleTop")
        GetFirebase.adIdCompass_collapsibleBottom = jsonIds.getString("adIdCompass_collapsibleBottom")
        GetFirebase.adIdExit_collapsibleTop = jsonIds.getString("adIdExit_collapsibleTop")
        GetFirebase.adIdExit_collapsibleBottom = jsonIds.getString("adIdExit_collapsibleBottom")
        GetFirebase.adIdLanguagesActivity_collapsibleTop = jsonIds.getString("adIdLanguagesActivity_collapsibleTop")
        GetFirebase.adIdLanguagesActivity_collapsibleBottom = jsonIds.getString("adIdLanguagesActivity_collapsibleBottom")
        GetFirebase.adIdMainActivity_collapsibleTop = jsonIds.getString("adIdMainActivity_collapsibleTop")
        GetFirebase.adIdMainActivity_collapsibleBottom = jsonIds.getString("adIdMainActivity_collapsibleBottom")
        GetFirebase.adIdMenu_collapsibleTop = jsonIds.getString("adIdMenu_collapsibleTop")
        GetFirebase.adIdMenu_collapsibleBottom = jsonIds.getString("adIdMenu_collapsibleBottom")
        GetFirebase.adIdOnboarding_collapsibleTop = jsonIds.getString("adIdOnboarding_collapsibleTop")
        GetFirebase.adIdOnboarding_collapsibleBottom = jsonIds.getString("adIdOnboarding_collapsibleBottom")
        GetFirebase.adIdPlaySound_collapsibleTop = jsonIds.getString("adIdPlaySound_collapsibleTop")
        GetFirebase.adIdPlaySound_collapsibleBottom = jsonIds.getString("adIdPlaySound_collapsibleBottom")
        GetFirebase.adIdSleepSound_collapsibleTop = jsonIds.getString("adIdSleepSound_collapsibleTop")
        GetFirebase.adIdSleepSound_collapsibleBottom = jsonIds.getString("adIdSleepSound_collapsibleBottom")
        GetFirebase.adIdStopWatch_collapsibleTop = jsonIds.getString("adIdStopWatch_collapsibleTop")
        GetFirebase.adIdStopWatch_collapsibleBottom = jsonIds.getString("adIdStopWatch_collapsibleBottom")
        GetFirebase.adIdTimer_collapsibleTop = jsonIds.getString("adIdTimer_collapsibleTop")
        GetFirebase.adIdTimer_collapsibleBottom = jsonIds.getString("adIdTimer_collapsibleBottom")
        GetFirebase.adIdWidget_collapsibleTop = jsonIds.getString("adIdWidget_collapsibleTop")
        GetFirebase.adIdWidget_collapsibleBottom = jsonIds.getString("adIdWidget_collapsibleBottom")
    }
}