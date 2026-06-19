package com.worldclock.app_themes.ads.helpers.usecases

interface BannerAdRepository {
    fun getBannerVisibility(screen: String, position: String): Boolean
    fun getBannerType(screen: String, position: String): String
}