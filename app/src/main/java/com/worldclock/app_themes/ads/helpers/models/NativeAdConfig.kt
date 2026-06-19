package com.worldclock.app_themes.ads.helpers.models

import android.widget.FrameLayout
import androidx.annotation.Keep

@Keep
data class NativeAdConfig(
    val position: String,
    val container: FrameLayout,
    val shimmer: FrameLayout
)