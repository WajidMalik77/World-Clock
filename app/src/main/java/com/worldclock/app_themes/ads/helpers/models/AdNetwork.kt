package com.worldclock.app_themes.ads.helpers.models

enum class AdNetwork { NONE, ADMOB, FACEBOOK }

data class AdWaterfallPlan(val primary: AdNetwork, val fallback: AdNetwork?) {
    fun networksInOrder(): List<AdNetwork> =
        listOfNotNull(primary, fallback).filter { it != AdNetwork.NONE }.distinct()
}
