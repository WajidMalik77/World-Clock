package com.worldclock.app_themes.ads.utils

object GetFirebase {

    var toastForAds = false
    var enable_interstitial_ads = false
    var use_counter = false
    var MIN_INTERVAL_MS = 1000
    var time_delay_for_preloaded_interstitial: Long = 0
    var time_delay_for_ondemand_interstitial: Long = 0

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
    var native_ad_background_for_splash: Int = 0
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
    var native_ad_background_for_onboardingscreen: Int = 0
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
    var native_ad_background_for_languagescreen: Int = 0
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
    var native_ad_background_for_otherscreens: Int = 0
    var native_ad_backgroundcolor_for_otherscreens: String = "#FFFFFF"

    // ══════════════════════════════════════
    // NATIVE AD STYLING — REELS
    // ══════════════════════════════════════
    var native_ad_buttonheight_for_reels: Double = 35.0
    var native_ad_buttontextcolor_for_reels: String = "#FFFFFF"
    var native_ad_buttontextheight_for_reels: Double = 12.0
    var native_ad_buttoncolor_for_reels: String = "#FF5722"
    var native_ad_headlinecolor_for_reels: String = "#000000"
    var native_ad_headlinetextheight_for_reels: Double = 14.0
    var native_ad_othertextcolor_for_reels: String = "#808080"
    var native_ad_bodytextheight_for_reels: Double = 12.0
    var native_ad_reels_screen_iconsize: Int = 30
    var native_ad_background_for_reels: Int = 0
    var native_ad_backgroundcolor_for_reels: String = "#FFFFFF"

    // ══════════════════════════════════════
    // AD IDs
    // ══════════════════════════════════════
    var adIdSplash_interstitial = ""
    var adIdLanguage_interstitial = ""
    var adIdOnboarding_interstitial = ""
    var adIdPremium_interstitial = ""
    var adIdOther_interstitial = ""


}