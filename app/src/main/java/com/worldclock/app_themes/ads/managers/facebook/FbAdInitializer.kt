package com.worldclock.app_themes.ads.managers.facebook

import android.content.Context
import com.facebook.ads.AdSettings
import com.facebook.ads.AudienceNetworkAds
import com.worldclock.app_themes.BuildConfig
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

object FbAdInitializer {
    private val initialized = AtomicBoolean(false)

    fun initialize(context: Context) {
        if (!initialized.compareAndSet(false, true)) return

        if (BuildConfig.DEBUG) {
            AdSettings.setTestMode(true)
        }

        AudienceNetworkAds.buildInitSettings(context)
            .withInitListener { result ->
                Timber.tag("FbAdsInit").d("FAN init success=${result.isSuccess} msg=${result.message}")
            }
            .initialize()
    }
}
