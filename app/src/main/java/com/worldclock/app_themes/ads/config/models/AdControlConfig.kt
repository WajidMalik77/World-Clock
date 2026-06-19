package com.worldclock.app_themes.ads.config.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdControlConfig(
    val app: AppConfig = AppConfig(),
    val banner: BannerV2Config = BannerV2Config(),
    val interstitial: InterstitialV2Config = InterstitialV2Config(),
    @SerialName("AdIds")
    val adIds: RemoteAdIdsConfig = RemoteAdIdsConfig(),
    @SerialName("native")
    val nativeV2: NativeAdSettingsConfig = NativeAdSettingsConfig()
)
