package com.worldclock.app_themes.ads.helpers.models

import android.widget.FrameLayout
import com.google.android.gms.ads.LoadAdError
import com.worldclock.app_themes.ads.config.models.NativeAdColorConfig

data class AdLoadParams(
    val adContainer: FrameLayout,
    val shimmerContainer: FrameLayout?,
    val adUnitId: String,
    val layoutRes: Int,
    val shimmerLayoutRes: Int,
    val colorConfig: NativeAdColorConfig?,
    val shouldPreloadNext: Boolean = true,
    val onLoaded: (() -> Unit)?,
    val onFailed: ((LoadAdError) -> Unit)?,
    val onImpression: (() -> Unit)? = null
)
