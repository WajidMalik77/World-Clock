package com.worldclock.app_themes.ads.app

import android.content.Context
import timber.log.Timber
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.worldclock.app_themes.ads.helpers.AdConfigInitializer
import com.worldclock.app_themes.ads.managers.UmpConsentManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppInitializer @Inject constructor(
    private val firebaseInitializer: FirebaseInitializer,
    private val adConfigInitializer: AdConfigInitializer,
    private val adsManager: AdsManager,
    private val billingManager: BillingManager,
    private val appOpenAdLifecycleManager: AppOpenAdLifecycleManager,
    private val umpConsentManager: UmpConsentManager,
    private val premiumRepository: PremiumRepository,
    @param:ApplicationContext private val appContext: Context
) {
    companion object {
        private const val TAG_INIT = "AppInitTrace"
    }
    private val initializationState = AtomicBoolean(false)

    fun initialize(force: Boolean = false) {
        Timber.tag(TAG_INIT).d("AppInitializer.initialize(force=$force) called")
        if (!force && !initializationState.compareAndSet(false, true)) {
            Timber.tag(TAG_INIT).d("AppInitializer skipped: already initializing")
            Timber.w("Initialization already in progress")
            return
        }

        // Firebase init must never crash app
        try {
            firebaseInitializer.initialize()
            Timber.tag(TAG_INIT).d("firebaseInitializer.initialize() success")
        } catch (e: Exception) {
            Timber.tag(TAG_INIT).e(e, "firebaseInitializer.initialize() failed")
            Timber.e(e, "Firebase initialization failed")
        }

        CoroutineScope(
            Dispatchers.Default + SupervisorJob()
        ).launch {
            try {
                runCatching {
                    billingManager.initialize()
                    Timber.tag(TAG_INIT).d("billingManager.initialize() success")
                }.onFailure {
                    Timber.tag(TAG_INIT).e(it, "billingManager.initialize() failed")
                    Timber.e(it, "Billing initialization failed")
                }

                runCatching {
                    appOpenAdLifecycleManager.initialize()
                    Timber.tag(TAG_INIT).d("appOpenAdLifecycleManager.initialize() success")
                }.onFailure {
                    Timber.tag(TAG_INIT).e(it, "appOpenAdLifecycleManager.initialize() failed")
                    Timber.e(it, "App open lifecycle manager initialization failed")
                }

                // Phase 0: If UMP consent is already cached as obtained/not-required,
                // pre-warm MobileAds + preload the splash app-open ad now so the SplashActivity
                // gate doesn't have to wait for init + load before showing.
                runCatching {
                    if (!premiumRepository.isPremiumUser() &&
                        umpConsentManager.canRequestAdsFromCache(appContext)
                    ) {
                        Timber.tag(TAG_INIT).d("Phase0: UMP cached -> pre-warming MobileAds")
                        adsManager.initializeIfNeeded()
                        adsManager.awaitInitialization()
                    } else {
                        Timber.tag(TAG_INIT).d("Phase0: skipping pre-warm (premium or UMP not cached)")
                    }
                }.onFailure {
                    Timber.tag(TAG_INIT).w(it, "Phase0 pre-warm failed")
                }

                val configResult = runCatching {
                    adConfigInitializer.preloadConfigs(force)
                }
                Timber.tag(TAG_INIT).d("adConfigInitializer.preloadConfigs() invoked")

                adConfigInitializer.setListener(
                    onReady = {
                        Timber.tag(TAG_INIT).d("Ad config ready callback fired")
                    },
                    onFailed = {
                        Timber.tag(TAG_INIT).w("Ad config failed callback fired")
                        Timber.w("Ad config initialization failed, skipping splash app-open preload")
                    }
                )

                runCatching {
                    withContext(Dispatchers.IO) {
                        FirebaseRemoteConfig.getInstance()
                            .fetchAndActivate()
                            .await()
                    }
                }.onFailure {
                    Timber.w(it, "Remote Config fetchAndActivate failed (likely offline)")
                }

                // Log failures, but NEVER crash app
                configResult.exceptionOrNull()?.let {
                    Timber.e(it, "Config preload failed (offline or service unavailable)")
                }

                Timber.tag(TAG_INIT).d("App-start ad initialization completed")
            } catch (e: Exception) {
                Timber.tag(TAG_INIT).e(e, "AppInitializer background initialization failed")
                Timber.e(e, "App initialization failed")
            } finally {
                Timber.tag(TAG_INIT).d("AppInitializer background initialization finished")
                initializationState.set(false)
            }
        }
    }
}
