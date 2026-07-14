package com.worldclock.app_themes.ads.config.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val ads: Int = 1,
    @SerialName("app_open")
    val appOpen: AppOpenV2Config = AppOpenV2Config(),
    val screens: ScreensV2 = ScreensV2(),
    @SerialName("should_force_update")
    val shouldForceUpdate: Int = 0
)

@Serializable
data class AppOpenV2Config(
    val splash: Int = 1,
    val resume: Int = 1,
    @SerialName("resume_min_background_seconds")
    val resumeMinBackgroundSeconds: Int = 2,
    val waterfall: Int = 1
)

@Serializable
data class ScreensV2(
    val first: LaunchScreens = LaunchScreens(languages = 1, intro = 1, premium = 1),
    val second: LaunchScreens = LaunchScreens(languages = 1, intro = 0, premium = 1),
    val goPro: Int? = null,
    @SerialName("settingspremium")
    val settingsPremium: Int? = null,
    @SerialName("settings_premium")
    val settingsPremiumSnake: Int? = null
)

@Serializable
data class BannerV2Config(
    val enabled: Int = 1,
    val waterfall: Int = 1,
    val placements: Map<String, Int> = mapOf(
        "languages_top" to 0,
        "languages_bottom" to 0,
        "intro_top" to 0,
        "intro_bottom" to 0,
        "home_top" to 0,
        "home_bottom" to 0,
        "exit_top" to 0,
        "exit_bottom" to 0,
        "menu_top" to 0,
        "menu_bottom" to 0,
        "settings_bottom" to 0,
        "splash_top" to 0,
        "splash_bottom" to 0,
        "clock_top" to 0,
        "clock_bottom" to 0,
        "add_clock_top" to 0,
        "add_clock_bottom" to 0,
        "alarm_top" to 0,
        "alarm_bottom" to 0,
        "add_alarm_top" to 0,
        "add_alarm_bottom" to 0,
        "stopwatch_top" to 0,
        "stopwatch_bottom" to 0,
        "timer_top" to 0,
        "timer_bottom" to 0,
        "compass_top" to 0,
        "compass_bottom" to 0,
        "widget_top" to 0,
        "widget_bottom" to 0,
        "add_widget_top" to 0,
        "add_widget_bottom" to 0,
        "all_reminders_top" to 0,
        "all_reminders_bottom" to 0,
        "add_reminder_top" to 0,
        "add_reminder_bottom" to 0,
        "sleep_sound_top" to 0,
        "sleep_sound_bottom" to 0,
        "play_sound_top" to 0,
        "play_sound_bottom" to 0,
        "add_all_reminders_top" to 0,
        "add_all_reminders_bottom" to 0
    )
)

@Serializable
data class InterstitialV2Config(
    val enabled: Int = 1,
    val waterfall: Int = 1,
    @SerialName("click_interval")
    val clickInterval: Int = 2,
    @SerialName("isInterFirstCount")
    val isInterFirstCount: Int = 1,
    @SerialName("cooldown_seconds")
    val cooldownSeconds: Int = 3,
    val placements: Map<String, Int> = mapOf(
        "splash" to 0,
        "language_first_done" to 0,
        "language_second_done" to 0,
        "intro_finish" to 0,
        "premium_close" to 0,
        "purchase_continue_with_ads" to 0,
        "purchase_close" to 0,
        "home_back" to 0,
        "home_clock" to 0,
        "home_alarm" to 0,
        "home_stopwatch" to 0,
        "home_timer" to 0,
        "home_compass" to 0,
        "home_widget" to 0,
        "home_reminders" to 0,
        "home_sleep_sound" to 0,
        "clock_add_clock" to 0,
        "add_clock_save" to 0,
        "alarm_add_alarm" to 0,
        "alarm_edit_alarm" to 0,
        "add_alarm_save" to 0,
        "all_reminders_category" to 0,
        "add_all_reminders_add" to 0,
        "add_all_reminders_edit" to 0,
        "add_reminder_save" to 0,
        "sleep_sound_play" to 0
    )
)

@Serializable
data class RemoteAdIdsConfig(
    val appOpen: Map<String, String> = emptyMap(),
    val interstitial: Map<String, String> = emptyMap(),
    val banner: Map<String, String> = emptyMap(),
    @SerialName("native")
    val nativeIds: Map<String, String> = emptyMap()
)

@Serializable
data class FbAdIdsConfig(
    val appOpen: Map<String, String> = emptyMap(),
    val interstitial: Map<String, String> = emptyMap(),
    val banner: Map<String, String> = emptyMap(),
    @SerialName("native")
    val nativeIds: Map<String, String> = emptyMap()
)
