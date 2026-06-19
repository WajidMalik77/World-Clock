package com.worldclock.app_themes.ads.helpers.usecases

import com.worldclock.app_themes.ads.config.models.NativeAdColorConfig

interface NativeAdRepository {
    fun getNativeVisibility(screen: String, position: String): Boolean
    fun shouldNativePreload(screen: String, position: String): Boolean
    fun getNativeAdSize(screen: String, position: String): Int
    fun getNativeAdColorConfig(screen: String, position: String): NativeAdColorConfig?
}