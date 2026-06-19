package com.worldclock.app_themes.ads.config.models

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class RemoteNativeConfigWrapper(
    @SerialName("native_ads")
    val native_ads: NativeAdSettingsConfig = NativeAdSettingsConfig()
)

@Keep
@Serializable
data class NativeAdSettingsConfig(
    val enabled: Int = 1,
    val preload: Int = 0,
    @SerialName("default_style")
    val default_config: NativeAdUnitConfig = NativeAdUnitConfig(
        enabled = 1,
        size = 1,
        color_config = NativeAdColorConfig(
            backgroundColorHex = "#FFF4F8FF",
            cornerRadiusDp = 12,
            strokeWidthDp = 1,
            strokeColorHex = "#22000000",
            headlineColorHex = "#111111",
            bodyTextColorHex = "#333333",
            ctaBackgroundColorHex = "#1A82FC",
            ctaTextColorHex = "#FFFFFF",
            ctaCornerRadiusDp = 48,
            ctaTextSizeSp = 18
        )
    ),
    val placements: Map<String, NativePlacement> = mapOf(
        "splash_top" to NativePlacement(value = 0),
        "splash_bottom" to NativePlacement(value = 3),
        "languages_bottom" to NativePlacement(value = 2),
        "intro_bottom" to NativePlacement(value = 4),
        "intro_full_screen" to NativePlacement(value = 1),
        "home_center" to NativePlacement(value = 5),
        "home_bottom" to NativePlacement(value = 0),
        "exit_bottom" to NativePlacement(value = 0),
        "settings_bottom" to NativePlacement(value = 1),
        "clock_bottom" to NativePlacement(value = 1),
        "add_clock_bottom" to NativePlacement(value = 1),
        "alarm_bottom" to NativePlacement(value = 1),
        "add_alarm_bottom" to NativePlacement(value = 1),
        "stopwatch_bottom" to NativePlacement(value = 1),
        "timer_bottom" to NativePlacement(value = 1),
        "compass_bottom" to NativePlacement(value = 1),
        "widget_bottom" to NativePlacement(value = 1),
        "add_widget_bottom" to NativePlacement(value = 1),
        "all_reminders_bottom" to NativePlacement(value = 1),
        "add_reminder_bottom" to NativePlacement(value = 1),
        "sleep_sound_bottom" to NativePlacement(value = 1),
        "play_sound_bottom" to NativePlacement(value = 1),
        "add_all_reminders_bottom" to NativePlacement(value = 1)
    )
)

@Keep
@Serializable
data class NativePlacement(
    val value: Int? = null,
    val top: Int? = null,
    var bottom: Int? = null,
    val center: Int? = null,
    val style: NativeAdColorConfig? = null,
    @SerialName("full_screen")
    var fullScreen: Int? = null,
    val recycler: Int? = null
)

@Keep
@Serializable
data class NativeAdUnitConfig(
    val enabled: Int? = null,
    val preload: Int? = null,
    val size: Int? = null,
    @SerialName("show_after")
    val showAfter: Int? = null,
    @SerialName("native_limit")
    val nativeLimit: Int? = null,
    @SerialName("color_config")
    val color_config: NativeAdColorConfig? = null
)

@Keep
@Serializable
data class NativeAdColorConfig(
    val mode: Int? = null,
    @SerialName("background_color")
    val backgroundColorHex: String? = null,
    @SerialName("corner_radius_dp")
    val cornerRadiusDp: Int? = null,
    @SerialName("stroke_width_dp")
    val strokeWidthDp: Int? = null,
    @SerialName("stroke_color")
    val strokeColorHex: String? = null,
    @SerialName("headline_color")
    val headlineColorHex: String? = null,
    @SerialName("body_text_color")
    val bodyTextColorHex: String? = null,
    @SerialName("cta_background_color")
    val ctaBackgroundColorHex: String? = null,
    @SerialName("cta_text_color")
    val ctaTextColorHex: String? = null,
    @SerialName("cta_corner_radius_dp")
    val ctaCornerRadiusDp: Int? = null,
    @SerialName("cta_text_size_sp")
    val ctaTextSizeSp: Int? = null,
    // v2 compatibility keys
    @SerialName("DarkBackgroundColor")
    val darkBackgroundColorHex: String? = null,
    @SerialName("LightBackgroundColor")
    val lightBackgroundColorHex: String? = null,
    @SerialName("dark_background_color")
    val darkBackgroundColorHexSnake: String? = null,
    @SerialName("light_background_color")
    val lightBackgroundColorHexSnake: String? = null,
    @SerialName("DarkStrokeColor")
    val darkStrokeColorHex: String? = null,
    @SerialName("LightStrokeColor")
    val lightStrokeColorHex: String? = null,
    @SerialName("dark_stroke_color")
    val darkStrokeColorHexSnake: String? = null,
    @SerialName("light_stroke_color")
    val lightStrokeColorHexSnake: String? = null,
    @SerialName("DarkHeadlineColor")
    val darkHeadlineColorHex: String? = null,
    @SerialName("LightHeadlineColor")
    val lightHeadlineColorHex: String? = null,
    @SerialName("dark_headline_color")
    val darkHeadlineColorHexSnake: String? = null,
    @SerialName("light_headline_color")
    val lightHeadlineColorHexSnake: String? = null,
    @SerialName("DarkBodyColor")
    val darkBodyColorHex: String? = null,
    @SerialName("LightBodyColor")
    val lightBodyColorHex: String? = null,
    @SerialName("dark_body_text_color")
    val darkBodyColorHexSnake: String? = null,
    @SerialName("light_body_text_color")
    val lightBodyColorHexSnake: String? = null,
    @SerialName("DarkCtaBackgroundColor")
    val darkCtaBackgroundColorHex: String? = null,
    @SerialName("LightCtaBackgroundColor")
    val lightCtaBackgroundColorHex: String? = null,
    @SerialName("dark_cta_background_color")
    val darkCtaBackgroundColorHexSnake: String? = null,
    @SerialName("light_cta_background_color")
    val lightCtaBackgroundColorHexSnake: String? = null,
    @SerialName("DarkCtaTextColor")
    val darkCtaTextColorHex: String? = null,
    @SerialName("LightCtaTextColor")
    val lightCtaTextColorHex: String? = null,
    @SerialName("dark_cta_text_color")
    val darkCtaTextColorHexSnake: String? = null,
    @SerialName("light_cta_text_color")
    val lightCtaTextColorHexSnake: String? = null,
    @SerialName("corner_radius")
    val cornerRadius: Int? = null,
    @SerialName("stroke_width")
    val strokeWidth: Int? = null,
    @SerialName("cta_radius")
    val ctaRadius: Int? = null,
    @SerialName("cta_text_size")
    val ctaTextSize: Int? = null,
    @SerialName("cta_bg_color")
    val ctaBgColorHex: String? = null
)
