package com.worldclock.app_themes.ads.helpers.usecases

import android.content.Context

interface ConnectivityChecker {
    fun isConnected(context: Context): Boolean
}