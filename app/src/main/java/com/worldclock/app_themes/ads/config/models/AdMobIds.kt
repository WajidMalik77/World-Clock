package com.worldclock.app_themes.ads.config.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdMobIds(
    val banner: String = "",
    val interstitial: String = "",
    @SerialName("splash_interstitial")
    val splashInterstitial: String = "",
    @SerialName("onboarding_interstitial")
    val onboardingInterstitial: String = "",
    @SerialName("language_interstitial")
    val languageInterstitial: String = "",
    val native: String = "",
    @SerialName("splash_native")
    val splashNative: String = "",
    @SerialName("onboarding_native")
    val onboardingNative: String = "",
    @SerialName("language_native")
    val languageNative: String = "",
    @SerialName("app_open")
    val appOpen: String = "",
    @SerialName("splash_app_open")
    val splashAppOpen: String = ""
)
