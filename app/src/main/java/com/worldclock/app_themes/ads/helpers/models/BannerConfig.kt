package com.worldclock.app_themes.ads.helpers.models

import android.view.View
import android.widget.FrameLayout
import androidx.annotation.Keep

@Keep
data class BannerConfig(
    val position: String,
    val container: FrameLayout,
    val shimmer: View,
    val adId: String? = null
)