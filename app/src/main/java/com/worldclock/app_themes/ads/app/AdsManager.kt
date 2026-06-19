package com.worldclock.app_themes.ads.app

import android.app.Application
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.MobileAds
import com.worldclock.app_themes.BuildConfig
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdsManager @Inject constructor(
    private val context: Application,
    private val premiumRepository: PremiumRepository
) {
    private val initDeferred = CompletableDeferred<Unit>()
    @Volatile
    private var initStarted = false
    fun initializeIfNeeded() {
        if (premiumRepository.isPremiumUser()) {
            initDeferred.complete(Unit) // Unblock anyone waiting
            return
        }

        if (initStarted) return

        synchronized(this) {
            if (initStarted) return
            initStarted = true
        }

        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                if (BuildConfig.DEBUG) {
                    MobileAds.setRequestConfiguration(
                        RequestConfiguration.Builder()
                            .setTestDeviceIds(listOf("849438CCA7757CF5A5EC5E31A609FA2F"))
                            .build()
                    )
                }
                MobileAds.initialize(context) { status ->
                    Timber.i("MobileAds initialized: $status")
                    initDeferred.complete(Unit)
                }
            }.onFailure { e ->
                Timber.e(e, "MobileAds initialization failed")
                initDeferred.completeExceptionally(e)
            }
        }
    }

    suspend fun awaitInitialization() {
        initDeferred.await()
    }
}
