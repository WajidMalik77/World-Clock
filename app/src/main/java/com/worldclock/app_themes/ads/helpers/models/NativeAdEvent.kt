package com.worldclock.app_themes.ads.helpers.models

import com.google.android.gms.ads.LoadAdError

sealed class NativeAdEvent {
    data class Loaded(val position: String) : NativeAdEvent()
    data class Impression(val position: String) : NativeAdEvent()
    data class Failed(val position: String, val error: LoadAdError) : NativeAdEvent()
    data class Off(val position: String) : NativeAdEvent()
    object AllOffFromConfig : NativeAdEvent()
}
