package com.worldclock.app_themes.ads.utils

object GetFirebase {

    //interstitial ad

    var transition_PremiumBack = 1
    var transition_AddAlarmBack = 1
    var transition_splash_ad_type = 1
    var transition_AddAllRemindersBack = 1
    var transition_AddClockBack = 1
    var transition_AddReminderBack = 1
    var transition_AlarmActivityBack = 1
    var transition_AllRemindersBack = 1
    var transition_ClockBack = 1
    var transition_CompassBack = 1
    var transition_LanguagesBack = 1
    var transition_MainBack = 1
    var transition_MenuBack = 1
    var transition_PlaySoundBack = 1
    var transition_SleepSoundBack = 1
    var transition_StopWatchBack = 1
    var transition_TimerBack = 1
    var transition_WidgetBack = 1

    var transition_AddAllRemindersForward = 1

    var transition_AlarmForward = 1
    var transition_AllRemindersForward = 1
    var transition_ClockForward = 1
    var transition_ExitForward = 1
    var transition_ExitBackpress = 1
    var transition_LanguageForward = 1
    var transition_MainForward = 1
    var transition_MenuForward = 1
    var transition_OnboardingForward = 1
    var transition_SleepSoundForward = 1

    //top bottom ads
    var banner_ad_addalarm_top = 0
    var banner_ad_addalarm_bottom = 0

    var banner_ad_addallreminders_top = 0
    var banner_ad_addallreminders_bottom = 0

    var banner_ad_addclock_top = 0
    var banner_ad_addclock_bottom = 0

    var banner_ad_addreminder_top = 0
    var banner_ad_addreminder_bottom = 0

    var banner_ad_addwidget_top = 0
    var banner_ad_addwidget_bottom = 0

    var banner_ad_alarmactivity_top = 0
    var banner_ad_alarmactivity_bottom = 0

    var banner_ad_allreminders_top = 0
    var banner_ad_allreminders_bottom = 0

    var banner_ad_clockactivity_top = 0
    var banner_ad_clockactivity_bottom = 0

    var banner_ad_compassactivity_top = 0
    var banner_ad_compassactivity_bottom = 0

    var banner_ad_exitactivity_top = 0
    var banner_ad_exitactivity_bottom = 0

    var banner_ad_languagesactivity_top = 0
    var banner_ad_languagesactivity_bottom = 0

    var banner_ad_mainactivity_top = 1
    var banner_ad_mainactivity_bottom = 6

    var banner_ad_menuactivity_top = 0
    var banner_ad_menuactivity_bottom = 0

    var banner_ad_onboardingactivity_top = 0
    var banner_ad_onboardingactivity_bottom = 0

    var banner_ad_playsoundactivity_top = 0
    var banner_ad_playsoundactivity_bottom = 0

    var banner_ad_sleepsoundactivity_top = 0
    var banner_ad_sleepsoundactivity_bottom = 0

    var banner_ad_splashactivity_top = 0
    var banner_ad_splashactivity_bottom = 0

    var banner_ad_stopwatchactivity_top = 0
    var banner_ad_stopwatchactivity_bottom = 0

    var banner_ad_timeractivity_top = 0
    var banner_ad_timeractivity_bottom = 0

    var banner_ad_widgetactivity_top = 0
    var banner_ad_widgetactivity_bottom = 0


    //others
    var open_ad_from_background_splash = false
    var open_ad_from_background = true
    var toastForAds = true
    var enable_interstitial_ads = true
    var enable_appopen_ads = true
    var use_counter = false
    var MIN_INTERVAL_MS = 1000
    var time_delay_for_preloaded_interstitial: Long = 0
    var time_delay_for_ondemand_interstitial: Long = 3000

    var show_language_for_retained_user = false
    var show_premium_for_retained_user = false

    var enable_on_demand_interstitial = 0

    var counter_interval = 3

    // ══════════════════════════════════════
    // NATIVE AD STYLING — SPLASH
    // ══════════════════════════════════════
    var native_ad_buttonheight_for_splash: Double = 35.0
    var native_ad_buttontextcolor_for_splash: String = "#FFFFFF"
    var native_ad_buttontextheight_for_splash: Double = 12.0
    var native_ad_buttoncolor_for_splash: String = "#FF5722"
    var native_ad_headlinecolor_for_splash: String = "#000000"
    var native_ad_headlinetextheight_for_splash: Double = 14.0
    var native_ad_othertextcolor_for_splash: String = "#808080"
    var native_ad_bodytextheight_for_splash: Double = 12.0
    var native_ad_splash_iconsize: Int = 30
    var native_ad_backgroundcolor_for_splash: String = "#FFFFFF"

    // ══════════════════════════════════════
    // NATIVE AD STYLING — ONBOARDING
    // ══════════════════════════════════════
    var native_ad_buttonheight_for_onboardingscreen: Double = 35.0
    var native_ad_buttontextcolor_for_onboardingscreen: String = "#FFFFFF"
    var native_ad_buttontextheight_for_onboardingscreen: Double = 12.0
    var native_ad_buttoncolor_for_onboardingscreen: String = "#FF5722"
    var native_ad_headlinecolor_for_onboardingscreen: String = "#000000"
    var native_ad_headlinetextheight_for_onboarding: Double = 14.0
    var native_ad_othertextcolor_for_onboardingscreen: String = "#808080"
    var native_ad_bodytextheight_for_onboarding: Double = 12.0
    var native_ad_onboarding_iconsize: Int = 30
    var native_ad_backgroundcolor_for_onboardingscreen: String = "#FFFFFF"

    // ══════════════════════════════════════
    // NATIVE AD STYLING — LANGUAGE
    // ══════════════════════════════════════
    var native_ad_buttonheight_for_languagescreen: Double = 35.0
    var native_ad_buttontextcolor_for_languagescreen: String = "#FFFFFF"
    var native_ad_buttontextheight_for_languagescreen: Double = 12.0
    var native_ad_buttoncolor_for_languagescreen: String = "#FF5722"
    var native_ad_headlinecolor_for_languagescreen: String = "#000000"
    var native_ad_headlinetextheight_for_languagescreen: Double = 14.0
    var native_ad_othertextcolor_for_languagescreen: String = "#808080"
    var native_ad_bodytextheight_for_languagescreen: Double = 12.0
    var native_ad_language_iconsize: Int = 30
    var native_ad_backgroundcolor_for_languagescreen: String = "#FFFFFF"

    // ══════════════════════════════════════
    // NATIVE AD STYLING — OTHER SCREENS
    // ══════════════════════════════════════
    var native_ad_buttonheight_for_otherscreens: Double = 35.0
    var native_ad_buttontextcolor_for_otherscreens: String = "#FFFFFF"
    var native_ad_buttontextheight_for_otherscreens: Double = 12.0
    var native_ad_buttoncolor_for_otherscreens: String = "#FF5722"
    var native_ad_headlinecolor_for_otherscreens: String = "#000000"
    var native_ad_headlinetextheight_for_otherscreens: Double = 14.0
    var native_ad_othertextcolor_for_otherscreens: String = "#808080"
    var native_ad_bodytextheight_for_otherscreens: Double = 12.0
    var native_ad_other_iconsize: Int = 30
    var native_ad_backgroundcolor_for_otherscreens: String = "#FFFFFF"
    // ══════════════════════════════════════
    // AD IDs
    // ══════════════════════════════════════
    // === INTERSTITIAL (Test ID: ca-app-pub-3940256099942544/1033173712) ===
    var adIdSplash_interstitial = "ca-app-pub-3940256099942544/1033173712"
    var adIdLanguage_interstitial = "ca-app-pub-3940256099942544/1033173712"
    var adIdOnboarding_interstitial = "ca-app-pub-3940256099942544/1033173712"
    var adIdPremium_interstitial = "ca-app-pub-3940256099942544/1033173712"
    var adIdOther_interstitial = "ca-app-pub-3940256099942544/1033173712"

    // === APP OPEN (Test ID: ca-app-pub-3940256099942544/9257395921) ===
    var adIdSplash_appopen = "ca-app-pub-3940256099942544/9257395921"
    var adIdBackground_appopen = "ca-app-pub-3940256099942544/9257395921"

// === BANNER (Test ID: ca-app-pub-3940256099942544/6300978111) ===
// === NATIVE (Test ID: ca-app-pub-3940256099942544/2247696110) ===

    var adIdSplash_bannerTop = "ca-app-pub-3940256099942544/6300978111"
    var adIdSplash_bannerBottom = "ca-app-pub-3940256099942544/6300978111"
    var adIdSplash_nativeTop = "ca-app-pub-3940256099942544/2247696110"
    var adIdSplash_nativeBottom = "ca-app-pub-3940256099942544/2247696110"

    var adIdAddAlarm_bannerTop = "ca-app-pub-3940256099942544/6300978111"
    var adIdAddAlarm_bannerBottom = "ca-app-pub-3940256099942544/6300978111"
    var adIdAddAlarm_nativeTop = "ca-app-pub-3940256099942544/2247696110"
    var adIdAddAlarm_nativeBottom = "ca-app-pub-3940256099942544/2247696110"

    var adIdAddAllReminders_bannerTop = "ca-app-pub-3940256099942544/6300978111"
    var adIdAddAllReminders_bannerBottom = "ca-app-pub-3940256099942544/6300978111"
    var adIdAddAllReminders_nativeTop = "ca-app-pub-3940256099942544/2247696110"
    var adIdAddAllReminders_nativeBottom = "ca-app-pub-3940256099942544/2247696110"

    var adIdAddClock_bannerTop = "ca-app-pub-3940256099942544/6300978111"
    var adIdAddClock_bannerBottom = "ca-app-pub-3940256099942544/6300978111"
    var adIdAddClock_nativeTop = "ca-app-pub-3940256099942544/2247696110"
    var adIdAddClock_nativeBottom = "ca-app-pub-3940256099942544/2247696110"

    var adIdAddReminder_bannerTop = "ca-app-pub-3940256099942544/6300978111"
    var adIdAddReminder_bannerBottom = "ca-app-pub-3940256099942544/6300978111"
    var adIdAddReminder_nativeTop = "ca-app-pub-3940256099942544/2247696110"
    var adIdAddReminder_nativeBottom = "ca-app-pub-3940256099942544/2247696110"
    var adIdAlarm_bannerTop = "ca-app-pub-3940256099942544/6300978111"
    var adIdAlarm_bannerBottom = "ca-app-pub-3940256099942544/6300978111"
    var adIdAlarm_nativeTop = "ca-app-pub-3940256099942544/2247696110"
    var adIdAlarm_nativeBottom = "ca-app-pub-3940256099942544/2247696110"

    var adIdAllReminders_bannerTop = "ca-app-pub-3940256099942544/6300978111"
    var adIdAllReminders_bannerBottom = "ca-app-pub-3940256099942544/6300978111"
    var adIdAllReminders_nativeTop = "ca-app-pub-3940256099942544/2247696110"
    var adIdAllReminders_nativeBottom = "ca-app-pub-3940256099942544/2247696110"

    var adIdClock_bannerTop = "ca-app-pub-3940256099942544/6300978111"
    var adIdClock_bannerBottom = "ca-app-pub-3940256099942544/6300978111"
    var adIdClock_nativeTop = "ca-app-pub-3940256099942544/2247696110"
    var adIdClock_nativeBottom = "ca-app-pub-3940256099942544/2247696110"

    var adIdCompass_bannerTop = "ca-app-pub-3940256099942544/6300978111"
    var adIdCompass_bannerBottom = "ca-app-pub-3940256099942544/6300978111"
    var adIdCompass_nativeTop = "ca-app-pub-3940256099942544/2247696110"
    var adIdCompass_nativeBottom = "ca-app-pub-3940256099942544/2247696110"

    var adIdExit_bannerTop = "ca-app-pub-3940256099942544/6300978111"
    var adIdExit_bannerBottom = "ca-app-pub-3940256099942544/6300978111"
    var adIdExit_nativeTop = "ca-app-pub-3940256099942544/2247696110"
    var adIdExit_nativeBottom = "ca-app-pub-3940256099942544/2247696110"

    var adIdLanguagesActivity_bannerTop = "ca-app-pub-3940256099942544/6300978111"
    var adIdLanguagesActivity_bannerBottom = "ca-app-pub-3940256099942544/6300978111"
    var adIdLanguagesActivity_nativeTop = "ca-app-pub-3940256099942544/2247696110"
    var adIdLanguagesActivity_nativeBottom = "ca-app-pub-3940256099942544/2247696110"

    var adIdMainActivity_bannerTop = "ca-app-pub-3940256099942544/6300978111"
    var adIdMainActivity_bannerBottom = "ca-app-pub-3940256099942544/6300978111"
    var adIdMainActivity_nativeTop = "ca-app-pub-3940256099942544/2247696110"
    var adIdMainActivity_nativeBottom = "ca-app-pub-3940256099942544/2247696110"

    var adIdMenu_bannerTop = "ca-app-pub-3940256099942544/6300978111"
    var adIdMenu_bannerBottom = "ca-app-pub-3940256099942544/6300978111"
    var adIdMenu_nativeTop = "ca-app-pub-3940256099942544/2247696110"
    var adIdMenu_nativeBottom = "ca-app-pub-3940256099942544/2247696110"

    var adIdOnboarding_bannerTop = "ca-app-pub-3940256099942544/6300978111"
    var adIdOnboarding_bannerBottom = "ca-app-pub-3940256099942544/6300978111"
    var adIdOnboarding_nativeTop = "ca-app-pub-3940256099942544/2247696110"
    var adIdOnboarding_nativeBottom = "ca-app-pub-3940256099942544/2247696110"

    var adIdPlaySound_bannerTop = "ca-app-pub-3940256099942544/6300978111"
    var adIdPlaySound_bannerBottom = "ca-app-pub-3940256099942544/6300978111"
    var adIdPlaySound_nativeTop = "ca-app-pub-3940256099942544/2247696110"
    var adIdPlaySound_nativeBottom = "ca-app-pub-3940256099942544/2247696110"

    var adIdSleepSound_bannerTop = "ca-app-pub-3940256099942544/6300978111"
    var adIdSleepSound_bannerBottom = "ca-app-pub-3940256099942544/6300978111"
    var adIdSleepSound_nativeTop = "ca-app-pub-3940256099942544/2247696110"
    var adIdSleepSound_nativeBottom = "ca-app-pub-3940256099942544/2247696110"

    var adIdStopWatch_bannerTop = "ca-app-pub-3940256099942544/6300978111"
    var adIdStopWatch_bannerBottom = "ca-app-pub-3940256099942544/6300978111"
    var adIdStopWatch_nativeTop = "ca-app-pub-3940256099942544/2247696110"
    var adIdStopWatch_nativeBottom = "ca-app-pub-3940256099942544/2247696110"

    var adIdTimer_bannerTop = "ca-app-pub-3940256099942544/6300978111"
    var adIdTimer_bannerBottom = "ca-app-pub-3940256099942544/6300978111"
    var adIdTimer_nativeTop = "ca-app-pub-3940256099942544/2247696110"
    var adIdTimer_nativeBottom = "ca-app-pub-3940256099942544/2247696110"

    var adIdWidget_bannerTop = "ca-app-pub-3940256099942544/6300978111"
    var adIdWidget_bannerBottom = "ca-app-pub-3940256099942544/6300978111"
    var adIdWidget_nativeTop = "ca-app-pub-3940256099942544/2247696110"
    var adIdWidget_nativeBottom = "ca-app-pub-3940256099942544/2247696110"

    var adIdSplash_collapsibleTop = "ca-app-pub-3940256099942544/2014213617"
    var adIdSplash_collapsibleBottom = "ca-app-pub-3940256099942544/2014213617"

    var adIdAddAlarm_collapsibleTop = "ca-app-pub-3940256099942544/2014213617"
    var adIdAddAlarm_collapsibleBottom = "ca-app-pub-3940256099942544/2014213617"

    var adIdAddAllReminders_collapsibleTop = "ca-app-pub-3940256099942544/2014213617"
    var adIdAddAllReminders_collapsibleBottom = "ca-app-pub-3940256099942544/2014213617"

    var adIdAddClock_collapsibleTop = "ca-app-pub-3940256099942544/2014213617"
    var adIdAddClock_collapsibleBottom = "ca-app-pub-3940256099942544/2014213617"

    var adIdAddReminder_collapsibleTop = "ca-app-pub-3940256099942544/2014213617"
    var adIdAddReminder_collapsibleBottom = "ca-app-pub-3940256099942544/2014213617"

    var adIdAddWidget_collapsibleTop = "ca-app-pub-3940256099942544/2014213617"
    var adIdAddWidget_collapsibleBottom = "ca-app-pub-3940256099942544/2014213617"

    var adIdAlarm_collapsibleTop = "ca-app-pub-3940256099942544/2014213617"
    var adIdAlarm_collapsibleBottom = "ca-app-pub-3940256099942544/2014213617"

    var adIdAllReminders_collapsibleTop = "ca-app-pub-3940256099942544/2014213617"
    var adIdAllReminders_collapsibleBottom = "ca-app-pub-3940256099942544/2014213617"

    var adIdClock_collapsibleTop = "ca-app-pub-3940256099942544/2014213617"
    var adIdClock_collapsibleBottom = "ca-app-pub-3940256099942544/2014213617"

    var adIdCompass_collapsibleTop = "ca-app-pub-3940256099942544/2014213617"
    var adIdCompass_collapsibleBottom = "ca-app-pub-3940256099942544/2014213617"

    var adIdExit_collapsibleTop = "ca-app-pub-3940256099942544/2014213617"
    var adIdExit_collapsibleBottom = "ca-app-pub-3940256099942544/2014213617"

    var adIdLanguagesActivity_collapsibleTop = "ca-app-pub-3940256099942544/2014213617"
    var adIdLanguagesActivity_collapsibleBottom = "ca-app-pub-3940256099942544/2014213617"

    var adIdMainActivity_collapsibleTop = "ca-app-pub-3940256099942544/2014213617"
    var adIdMainActivity_collapsibleBottom = "ca-app-pub-3940256099942544/2014213617"

    var adIdMenu_collapsibleTop = "ca-app-pub-3940256099942544/2014213617"
    var adIdMenu_collapsibleBottom = "ca-app-pub-3940256099942544/2014213617"

    var adIdOnboarding_collapsibleTop = "ca-app-pub-3940256099942544/2014213617"
    var adIdOnboarding_collapsibleBottom = "ca-app-pub-3940256099942544/2014213617"

    var adIdPlaySound_collapsibleTop = "ca-app-pub-3940256099942544/2014213617"
    var adIdPlaySound_collapsibleBottom = "ca-app-pub-3940256099942544/2014213617"

    var adIdSleepSound_collapsibleTop = "ca-app-pub-3940256099942544/2014213617"
    var adIdSleepSound_collapsibleBottom = "ca-app-pub-3940256099942544/2014213617"

    var adIdStopWatch_collapsibleTop = "ca-app-pub-3940256099942544/2014213617"
    var adIdStopWatch_collapsibleBottom = "ca-app-pub-3940256099942544/2014213617"

    var adIdTimer_collapsibleTop = "ca-app-pub-3940256099942544/2014213617"
    var adIdTimer_collapsibleBottom = "ca-app-pub-3940256099942544/2014213617"

    var adIdWidget_collapsibleTop = "ca-app-pub-3940256099942544/2014213617"
    var adIdWidget_collapsibleBottom = "ca-app-pub-3940256099942544/2014213617"


}