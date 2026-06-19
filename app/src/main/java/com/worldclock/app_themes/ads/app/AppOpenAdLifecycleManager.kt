package com.worldclock.app_themes.ads.app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import timber.log.Timber
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import com.worldclock.app_themes.BuildConfig
import com.worldclock.app_themes.ads.config.AdControlConfigManager
import com.worldclock.app_themes.ads.dialogs.LoadingDialog
import com.worldclock.app_themes.ads.managers.AppOpenManager
import com.worldclock.app_themes.ads.utils.ADS
import com.worldclock.app_themes.ads.utils.ADS.TEST_ADMOB_APP_OPEN_ID
import com.worldclock.app_themes.ads.utils.ADS.TEST_ADMOB_APP_OPEN_SPLASH_ID
import com.worldclock.app_themes.ads.utils.AdShowCallback
import com.worldclock.app_themes.ads.utils.AdStateManager
import com.worldclock.app_themes.ads.utils.AdsPref
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppOpenAdLifecycleManager @Inject constructor(
    private val adsPref: AdsPref,
    private val premiumRepository: PremiumRepository,
    private val adControlConfigManager: AdControlConfigManager,
    @param:ApplicationContext private val appContext: Context
) : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {
    companion object {
        private const val TAG_AO = "AppOpenTrace"
    }

    private var currentActivity: Activity? = null
    private lateinit var appOpenManager: AppOpenManager
    private var backgroundedAtMillis: Long = 0L
    private var suppressResumeUntilMillis: Long = 0L
    private val resumeFlowInProgress = AtomicBoolean(false)
    private val lifecycleAttached = AtomicBoolean(false)
    private var resumeLoadingDialog: LoadingDialog? = null
    private val resumeDialogHandler = Handler(Looper.getMainLooper())

    fun suppressResumeAppOpenFor(durationMillis: Long) {
        suppressResumeUntilMillis = System.currentTimeMillis() + durationMillis.coerceAtLeast(0L)
        Timber.tag(TAG_AO).d("resume app-open suppressed until=$suppressResumeUntilMillis")
    }

    fun initialize() {
        initializeAppOpenManager()
    }

    fun attachToAppLifecycle(application: Application) {
        if (!lifecycleAttached.compareAndSet(false, true)) return
        initializeAppOpenManager()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        application.registerActivityLifecycleCallbacks(this)
        Timber.tag(TAG_AO).d("App-open lifecycle attached")
    }

    fun preloadSplashAd(onPrepared: ((Boolean) -> Unit)? = null) {
        preloadSplashAdInternal(allowRetry = true, onPrepared = onPrepared)
    }

    private fun preloadSplashAdInternal(
        allowRetry: Boolean,
        onPrepared: ((Boolean) -> Unit)? = null
    ) {
        try {
            val canRequestAds = canRequestAds()
            if (!canRequestAds || premiumRepository.isPremiumUser() || !adControlConfigManager.shouldShowAppOpenSplash()) {
                onPrepared?.invoke(false)
                return
            }
            val adId = if (BuildConfig.DEBUG) {
                TEST_ADMOB_APP_OPEN_SPLASH_ID
            } else {
                adControlConfigManager.getProdAppOpenSplashAdUnitId(ADS.PROD_ADMOB_APP_OPEN_SPLASH_ID)
            }
            if (adId.isBlank()) {
                onPrepared?.invoke(false)
                return
            }
            initializeAppOpenManager()
            if (appOpenManager.hasUsableAd()) {
                onPrepared?.invoke(true)
                return
            }
            if (appOpenManager.isAdLoading()) {
                onPrepared?.invoke(true)
                return
            }
            appOpenManager.loadAppOpenAd(
                adUnitId = adId,
                adsPref = adsPref,
                fallbackAdUnitId = ADS.PROD_ADMOB_APP_OPEN_SPLASH_ID,
                onAdLoaded = { onPrepared?.invoke(true) },
                onAdFailed = { error ->
                    handleSplashLoadFailure(error, allowRetry, onPrepared)
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "Error preloading splash app open ad")
            onPrepared?.invoke(false)
        }
    }

    private fun handleSplashLoadFailure(
        error: com.google.android.gms.ads.LoadAdError,
        allowRetry: Boolean,
        onPrepared: ((Boolean) -> Unit)?
    ) {
        val retryable = allowRetry && error.code != 3 // 3 = NO_FILL, retry won't help
        if (!retryable) {
            onPrepared?.invoke(false)
            return
        }
        Timber.tag(TAG_AO).w("Splash app-open load failed code=${error.code}, retrying once")
        Handler(Looper.getMainLooper()).postDelayed({
            preloadSplashAdInternal(allowRetry = false, onPrepared = onPrepared)
        }, 600L)
    }

    private fun pollForLoadedAdAndShow(
        totalWaitMs: Long,
        intervalMs: Long,
        onAdShown: (() -> Unit)?,
        onFinished: () -> Unit
    ) {
        val handler = Handler(Looper.getMainLooper())
        val deadline = System.currentTimeMillis() + totalWaitMs
        val tick = object : Runnable {
            override fun run() {
                if (appOpenManager.hasUsableAd()) {
                    showAppOpenAdDirect(onAdShown, onFinished)
                } else if (System.currentTimeMillis() >= deadline || !appOpenManager.isAdLoading()) {
                    if (appOpenManager.hasUsableAd()) {
                        showAppOpenAdDirect(onAdShown, onFinished)
                    } else {
                        onFinished()
                    }
                } else {
                    handler.postDelayed(this, intervalMs)
                }
            }
        }
        handler.postDelayed(tick, intervalMs)
    }

    private fun pollResumeForLoadedAd(totalWaitMs: Long, intervalMs: Long) {
        val deadline = System.currentTimeMillis() + totalWaitMs
        val tick = object : Runnable {
            override fun run() {
                if (appOpenManager.hasUsableAd()) {
                    showAppOpenAdDirect(
                        onFinished = {
                            dismissResumeLoading()
                            resumeFlowInProgress.set(false)
                        }
                    )
                } else if (System.currentTimeMillis() >= deadline || !appOpenManager.isAdLoading()) {
                    dismissResumeLoading()
                    resumeFlowInProgress.set(false)
                } else {
                    resumeDialogHandler.postDelayed(this, intervalMs)
                }
            }
        }
        resumeDialogHandler.postDelayed(tick, intervalMs)
    }

    fun showSplashAppOpenIfAvailable(
        onAdShown: (() -> Unit)? = null,
        onFinished: () -> Unit
    ) {
        try {
            val canRequestAds = canRequestAds()
            if (!canRequestAds || premiumRepository.isPremiumUser() || !adControlConfigManager.shouldShowAppOpenSplash()) {
                onFinished()
                return
            }
            initializeAppOpenManager()
            val adId = if (BuildConfig.DEBUG) {
                TEST_ADMOB_APP_OPEN_SPLASH_ID
            } else {
                adControlConfigManager.getProdAppOpenSplashAdUnitId(ADS.PROD_ADMOB_APP_OPEN_SPLASH_ID)
            }
            if (adId.isBlank()) {
                onFinished()
                return
            }
            if (appOpenManager.hasUsableAd()) {
                showAppOpenAdDirect(onAdShown, onFinished)
                return
            }
            if (appOpenManager.isAdLoading()) {
                // Extended wait so an in-flight load that completes a moment after the
                // splash budget elapsed still gets shown, instead of dropping the ad.
                pollForLoadedAdAndShow(
                    totalWaitMs = 4000L,
                    intervalMs = 250L,
                    onAdShown = onAdShown,
                    onFinished = onFinished
                )
                return
            }
            appOpenManager.loadAppOpenAd(
                adUnitId = adId,
                adsPref = adsPref,
                fallbackAdUnitId = ADS.PROD_ADMOB_APP_OPEN_SPLASH_ID,
                onAdLoaded = {
                    val act = currentActivity
                    if (act != null && isOnSplashScreen(act)) {
                        showAppOpenAdDirect(onAdShown, onFinished)
                    } else {
                        Timber.w("Splash App Open loaded late, discarding to prevent showing over Home")
                        onFinished()
                    }
                },
                onAdFailed = {
                    onFinished()
                    Unit
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "Error showing splash app-open")
            onFinished()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        try {
            Timber.tag(TAG_AO).d("onStart entered")
            if (!shouldShowAppOpenAd()) {
                Timber.tag(TAG_AO).d("blocked: shouldShowAppOpenAd=false")
                return
            }

            val activity = currentActivity ?: run {
                Timber.tag(TAG_AO).d("blocked: currentActivity=null")
                return
            }
            if (activity.localClassName == "com.google.android.gms.ads.AdActivity") {
                Timber.tag(TAG_AO).d("blocked: current activity is AdActivity")
                return
            }

            if (isOnSplashScreen(activity)) {
                Timber.d("Skipping App Open ad - on SplashScreen")
                return
            }

            val minBackgroundSeconds = adControlConfigManager.getResumeMinBackgroundSeconds()
            if (minBackgroundSeconds > 0 && backgroundedAtMillis > 0L) {
                val elapsedSeconds = (System.currentTimeMillis() - backgroundedAtMillis) / 1000
                if (elapsedSeconds < minBackgroundSeconds) {
                    Timber.d("Skipping resume app open ad; elapsed=$elapsedSeconds sec, min=$minBackgroundSeconds sec")
                    return
                }
            }

            Timber.tag(TAG_AO).d("passed lifecycle gates, loading/showing resume app-open")
            ensureResumeAppOpenLoadedAndShow()
        } catch (e: Exception) {
            Timber.e(e, "Error in onStart lifecycle event")
            // Silently handle to prevent crash
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        backgroundedAtMillis = System.currentTimeMillis()
        Timber.tag(TAG_AO).d("onStop backgroundedAtMillis=$backgroundedAtMillis")
        dismissResumeLoading()
        super.onStop(owner)
    }

    private fun shouldShowAppOpenAd(): Boolean {
        return try {
            if (System.currentTimeMillis() < suppressResumeUntilMillis) {
                Timber.tag(TAG_AO).d("shouldShow check blocked: temporary suppression active")
                return false
            }
            val act = currentActivity
            if (act != null && isOnSplashScreen(act)) {
                Timber.tag(TAG_AO).d("shouldShow check blocked: Splash activity is foreground")
                return false
            }
            val canRequestAds = canRequestAds()
            val isPremium = premiumRepository.isPremiumUser()
            val managerReady = ::appOpenManager.isInitialized
            val anyAdShowing = AdStateManager.isAnyAdShowing()
            val resumeEnabled = adControlConfigManager.shouldShowAppOpenResume()
            Timber.tag(TAG_AO).d("shouldShow check: canRequestAds=$canRequestAds premium=$isPremium managerReady=$managerReady anyAdShowing=$anyAdShowing resumeEnabled=$resumeEnabled"
            )
            canRequestAds && !isPremium && managerReady && !anyAdShowing && resumeEnabled
        } catch (e: Exception) {
            Timber.e(e, "Error checking if should show app open ad")
            false
        }
    }

    private fun canRequestAds(): Boolean {
        return true
    }

    private fun isOnSplashScreen(activity: Activity): Boolean {
        val name = activity::class.java.simpleName
        return name == "Splash" || name == "SplashActivity"
    }

    private fun showAppOpenAdDirect(
        onAdShown: (() -> Unit)? = null,
        onFinished: (() -> Unit)? = null
    ) {
        try {
            appOpenManager.showAppOpenAdIfAvailable(adsPref, object : AdShowCallback {
                override fun onAdShown() {
                    Timber.d("App Open ad shown")
                    onAdShown?.invoke()
                }

                override fun onAdFailedToShow(adError: String) {
                    if (adError.contains("already showing", ignoreCase = true)) {
                        Timber.d("App Open show skipped: $adError")
                    } else {
                        Timber.w("App Open ad failed: $adError")
                    }
                    onFinished?.invoke()
                }

                override fun onAdDismissed() {
                    onFinished?.invoke()
                }
            })
        } catch (e: Exception) {
            Timber.e(e, "Error showing app open ad direct")
            onFinished?.invoke()
        }
    }

    private fun initializeAppOpenManager() {
        try {
            if (!premiumRepository.isPremiumUser()) {
                appOpenManager = AppOpenManager.getInstance(appContext as Application)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error initializing app open manager")
        }
    }

    // ActivityLifecycleCallbacks
    override fun onActivityResumed(activity: Activity) {
        try {
            currentActivity = activity
            Timber.tag(TAG_AO).d("onActivityResumed ${activity::class.java.simpleName}")
            if (::appOpenManager.isInitialized) {
                appOpenManager.updateCurrentActivity(activity)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in onActivityResumed")
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        try {
            // ProcessLifecycleOwner#onStart can run before Activity#onResume.
            // Keep an early foreground activity reference so resume app-open logic can run.
            currentActivity = activity
            Timber.tag(TAG_AO).d("onActivityStarted ${activity::class.java.simpleName}")
            if (::appOpenManager.isInitialized) {
                appOpenManager.updateCurrentActivity(activity)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in onActivityStarted")
        }
    }

    private fun ensureResumeAppOpenLoadedAndShow() {
        if (!resumeFlowInProgress.compareAndSet(false, true)) {
            Timber.tag(TAG_AO).d("blocked: resume show flow already in progress")
            return
        }

        val resumeAdUnitId = if (BuildConfig.DEBUG) {
            TEST_ADMOB_APP_OPEN_ID
        } else {
            adControlConfigManager.getProdAppOpenResumeAdUnitId(ADS.PROD_ADMOB_APP_OPEN_ID)
        }
        if (resumeAdUnitId.isBlank()) {
            Timber.tag(TAG_AO).w("blocked: resume ad unit id is blank")
            resumeFlowInProgress.set(false)
            return
        }

        if (appOpenManager.hasUsableAd()) {
            Timber.tag(TAG_AO).d("resume path: using cached app-open ad, no loading dialog needed")
            showAppOpenAdDirect(
                onFinished = {
                    dismissResumeLoading()
                    resumeFlowInProgress.set(false)
                }
            )
            return
        }

        if (appOpenManager.isAdLoading()) {
            Timber.tag(TAG_AO).d("resume load already in progress, waiting up to 3s")
            showResumeLoadingIfPossible()
            pollResumeForLoadedAd(totalWaitMs = 3000L, intervalMs = 250L)
            return
        }

        Timber.tag(TAG_AO).d("loading resume app-open ad id=$resumeAdUnitId")
        showResumeLoadingIfPossible()
        appOpenManager.loadAppOpenAd(
            adUnitId = resumeAdUnitId,
            adsPref = adsPref,
            fallbackAdUnitId = ADS.PROD_ADMOB_APP_OPEN_ID,
            onAdLoaded = {
                Timber.tag(TAG_AO).d("resume load success, calling show")
                showAppOpenAdDirect(
                    onFinished = {
                        dismissResumeLoading()
                        resumeFlowInProgress.set(false)
                    }
                )
            },
            onAdFailed = {
                Timber.tag(TAG_AO).w("resume load failed code=${it.code} message=${it.message}")
                dismissResumeLoading()
                resumeFlowInProgress.set(false)
                Unit
            }
        )
    }

    private fun showResumeLoadingIfPossible() {
        val activity = currentActivity
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            Timber.tag(TAG_AO).d("resume loading dialog skipped: no valid activity")
            return
        }
        dismissResumeLoading()
        resumeLoadingDialog = LoadingDialog(activity).also {
            Timber.tag(TAG_AO).d("resume loading dialog shown")
            it.show()
        }
        resumeDialogHandler.postDelayed({
            if (resumeLoadingDialog != null) {
                Timber.tag(TAG_AO).w("resume loading dialog timeout -> dismissing")
                dismissResumeLoading()
            }
        }, 7000L)
    }

    private fun dismissResumeLoading() {
        resumeDialogHandler.removeCallbacksAndMessages(null)
        resumeLoadingDialog?.dismiss()
        resumeLoadingDialog = null
        Timber.tag(TAG_AO).d("resume loading dialog dismissed")
    }
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        try {
            // Clean up reference if this was the current activity
            if (currentActivity == activity) {
                currentActivity = null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in onActivityDestroyed")
        }
    }
}
