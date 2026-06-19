package com.worldclock.app_themes.ads.helpers.usecases

import android.content.Context
import com.worldclock.app_themes.ads.utils.Constants
import javax.inject.Inject

class ConnectivityCheckerImpl @Inject constructor() : ConnectivityChecker {
    override fun isConnected(context: Context): Boolean {
        return Constants.isInternetAvailable(context)
    }
}