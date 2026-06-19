package com.worldclock.app_themes.ads.managers

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import timber.log.Timber
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.worldclock.app_themes.ads.utils.AdShowCallback
import com.worldclock.app_themes.ads.utils.AdStateManager
import com.worldclock.app_themes.ads.utils.AdUnitIdSanitizer
import com.worldclock.app_themes.ads.utils.AdsPref
import com.worldclock.app_themes.ads.utils.DebugToaster
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class AppOpenManager private constructor(context: Context) {
    private val appContext: Context = context.applicationContext
    private val appOpenAdRef = AtomicReference<AppOpenAd?>()
    private val loadTimeRef = AtomicLong(0L)
    private val currentActivityRef = AtomicReference<WeakReference<Activity>?>()
    private val isDestroyed = AtomicBoolean(false)
    private val isAdLoading = AtomicBoolean(false)
    private val isAppOpenAdShowing = AtomicBoolean(false)
    private val showAttemptInProgress = AtomicBoolean(false)
    private val bgDispatcher = Dispatchers.IO
    private val scope = CoroutineScope(SupervisorJob() + bgDispatcher)
    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastAdUnitId: String? = null

    /*    private val prefs: SharedPreferences =
            context.getSharedPreferences("app_open_ad_prefs", Context.MODE_PRIVATE)

        private var adLoadCount: Int
            get() = prefs.getInt(KEY_AD_LOAD_COUNT, 0)
            set(value) = prefs.edit { putInt(KEY_AD_LOAD_COUNT, value) }*/

    companion object {
        @Volatile
        private var INSTANCE: AppOpenManager? = null
        private const val TAG_AO = "AppOpenTrace"
        private const val SHOW_CONFIRMATION_TIMEOUT_MS = 2500L
//        private const val KEY_AD_LOAD_COUNT = "app_open_ad_load_count"

        fun getInstance(context: Context): AppOpenManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppOpenManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    fun updateCurrentActivity(activity: Activity?) {
        currentActivityRef.set(activity?.let { WeakReference(it) })
    }

    fun loadAppOpenAd(
        adUnitId: String,
        adsPref: AdsPref,
        fallbackAdUnitId: String = com.worldclock.app_themes.ads.utils.ADS.PROD_ADMOB_APP_OPEN_ID,
        onAdLoaded: () -> Unit? = {},
        onAdFailed: (LoadAdError) -> Unit? = {},
    ) {
        val resolvedAdUnitId = AdUnitIdSanitizer.sanitizeAppOpen(adUnitId, fallbackAdUnitId)
        Timber.tag(TAG_AO).d("load requested id=$resolvedAdUnitId")
        if (isAdAvailable()) {
            Timber.tag(TAG_AO).d("load skipped: ad already available")
            onAdLoaded()
            return
        }

        if (isAdLoading.getAndSet(true)) {
            Timber.tag(TAG_AO).d("load skipped: already in progress")
            return
        }

        if (isDestroyed.get()) {
            Timber.tag(TAG_AO).w("load blocked: manager destroyed")
            isAdLoading.set(false)
            return
        }

        lastAdUnitId = resolvedAdUnitId

        // Build request off main thread
        scope.launch {
            val request = buildAdRequest(adsPref)

            withContext(Dispatchers.Main) {
                loadAdOnMainThread(
                    resolvedAdUnitId,
                    request,
                    adsPref,
                    onAdLoaded = { onAdLoaded.invoke() },
                    onAdFailed = { error ->
                        onAdFailed.invoke(error)
                    }
                )
            }
        }
    }

    private fun buildAdRequest(adsPref: AdsPref): AdRequest {
        val requestBuilder = AdRequest.Builder()

        if (adsPref.isNpa()) {
            val extras = Bundle()
            extras.putString("npa", "1")
            requestBuilder.addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
        }

        return requestBuilder.build()
    }

    private fun loadAdOnMainThread(
        adUnitId: String,
        request: AdRequest,
        adsPref: AdsPref,
        onAdLoaded: () -> Unit,
        onAdFailed: (LoadAdError) -> Unit
    ) {
        AppOpenAd.load(
            appContext,
            adUnitId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    DebugToaster.showAdDebugCard(appContext, adsPref.isDebugToastAppOpenEnabled(), "AppOpen: Loaded")
                    handleAdLoaded(ad, onAdLoaded)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    DebugToaster.showAdDebugCard(appContext, adsPref.isDebugToastAppOpenEnabled(), "AppOpen: Failed to Load")
                    handleAdLoadFailed(error, onAdFailed)
                }
            }
        )
    }

    private fun handleAdLoaded(
        ad: AppOpenAd,
        onAdLoaded: () -> Unit
    ) {
        Timber.d("Ad Loaded: AppOpenAd")
        if (isDestroyed.get()) {
            ad.fullScreenContentCallback = null
            return
        }

        // Lock-free updates
        appOpenAdRef.set(ad)
        loadTimeRef.set(System.currentTimeMillis())
        isAdLoading.set(false)
//        adLoadCount++

        onAdLoaded()
    }

    private fun handleAdLoadFailed(
        error: LoadAdError,
        onAdFailed: (LoadAdError) -> Unit
    ) {
        Timber.d("Ad Failed: ${error.message}")

        isAdLoading.set(false)
//        appOpenDialog?.safeDismiss()

        onAdFailed(error)
    }

    fun showAppOpenAdIfAvailable(adsPref: AdsPref, callback: AdShowCallback) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { showAppOpenAdIfAvailable(adsPref, callback) }
        } else {
            showAppOpenAdOnMainThread(adsPref, callback)
        }
    }

    fun showAppOpenAdIfAvailable(
        activity: Activity,
        adsPref: AdsPref,
        callback: AdShowCallback
    ) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { showAppOpenAdIfAvailable(activity, adsPref, callback) }
        } else {
            showAppOpenAdOnMainThread(activity, adsPref, callback)
        }
    }

    private fun showAppOpenAdOnMainThread(adsPref: AdsPref, callback: AdShowCallback) {
        val skipReason = getSkipReason(requireCurrentActivity = true)
        if (skipReason != null) {
            Timber.tag(TAG_AO).d("show blocked: $skipReason")
            callback.onAdFailedToShow(skipReason)
            return
        }
        val activity = currentActivityRef.get()?.get() ?: run {
            Timber.tag(TAG_AO).d("show blocked: no active activity at schedule")
            callback.onAdFailedToShow("No active activity")
            return
        }

        showAdNow(activity, adsPref, callback)
    }

    private fun showAppOpenAdOnMainThread(
        activity: Activity,
        adsPref: AdsPref,
        callback: AdShowCallback
    ) {
        val skipReason = getSkipReason(requireCurrentActivity = false)
        if (skipReason != null) {
            Timber.tag(TAG_AO).d("show blocked: $skipReason")
            callback.onAdFailedToShow(skipReason)
            return
        }

        showAdNow(activity, adsPref, callback)
    }

    private fun showAdNow(activity: Activity, adsPref: AdsPref, callback: AdShowCallback) {
        if (activity.isFinishing || activity.isDestroyed) {
            Timber.tag(TAG_AO).d("show blocked: activity is finishing or destroyed")
            callback.onAdFailedToShow("Activity is finishing or destroyed")
            return
        }

        val ad = appOpenAdRef.get() ?: run {
            Timber.tag(TAG_AO).d("show blocked: ad ref null")
            callback.onAdFailedToShow("Ad not available")
            return
        }

        if (isAppOpenAdShowing.get() || !showAttemptInProgress.compareAndSet(false, true)) {
            Timber.tag(TAG_AO).d("show blocked: already showing")
            callback.onAdFailedToShow("Ad already showing")
            return
        }

        // Set callback
        ad.fullScreenContentCallback = createFullScreenCallback(activity, adsPref, callback)
        scheduleShowConfirmationTimeout(callback)

        try {
            Timber.tag(TAG_AO).d("show executing ad.show()")
            ad.show(activity)
        } catch (e: Exception) {
            handleShowException(e, callback)
        }
    }

    private fun scheduleShowConfirmationTimeout(callback: AdShowCallback) {
        mainHandler.postDelayed({
            if (showAttemptInProgress.get() && !isAppOpenAdShowing.get()) {
                Timber.tag(TAG_AO).w("show attempt timed out before SDK confirmed display")
                showAttemptInProgress.set(false)
                callback.onAdFailedToShow("Ad show timed out")
            }
        }, SHOW_CONFIRMATION_TIMEOUT_MS)
    }

    private fun getSkipReason(requireCurrentActivity: Boolean): String? {
        return when {
            isDestroyed.get() -> "AppOpenManager is destroyed"
            isAppOpenAdShowing.get() -> "An App Open Ad is already showing"
            showAttemptInProgress.get() -> "An App Open Ad show attempt is already in progress"
            requireCurrentActivity && currentActivityRef.get()?.get() == null -> "No active activity"
            !isAdAvailable() -> "No App Open Ad loaded"
            wasLoadTimeLessThanNHoursAgo().not() -> "Ad expired"
            else -> null
        }
    }

    private fun createFullScreenCallback(
        activity: Activity,
        adsPref: AdsPref,
        callback: AdShowCallback
    ): FullScreenContentCallback {
        return object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastAppOpenEnabled(), "AppOpen: Shown")
                handleAdShown(callback)
            }

            override fun onAdDismissedFullScreenContent() {
                handleAdDismissed(callback)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                DebugToaster.showAdDebugCard(activity, adsPref.isDebugToastAppOpenEnabled(), "AppOpen: Failed to Show")
                handleShowFailure(adError, callback)
            }

            override fun onAdImpression() {}
        }
    }

    private fun handleAdShown(callback: AdShowCallback) {
        Timber.tag(TAG_AO).d("show success")
        showAttemptInProgress.set(false)
        isAppOpenAdShowing.set(true)
        AdStateManager.isAppOpenAdShowing = true
        appOpenAdRef.set(null)

        callback.onAdShown()
    }

    private fun handleAdDismissed(callback: AdShowCallback) {
        Timber.tag(TAG_AO).d("show dismissed")
        showAttemptInProgress.set(false)
        isAppOpenAdShowing.set(false)
        AdStateManager.isAppOpenAdShowing = false

        callback.onAdDismissed()
    }

    private fun handleShowFailure(adError: AdError, callback: AdShowCallback) {
        Timber.tag(TAG_AO).w("show failed code=${adError.code} message=${adError.message}")
        appOpenAdRef.set(null)
        showAttemptInProgress.set(false)
        isAppOpenAdShowing.set(false)
        AdStateManager.isAppOpenAdShowing = false

        callback.onAdFailedToShow("Ad failed: [${adError.code}] ${adError.message}")
    }

    private fun handleShowException(e: Exception, callback: AdShowCallback) {
        Timber.tag(TAG_AO).e(e, "show exception")
        showAttemptInProgress.set(false)
        isAppOpenAdShowing.set(false)
        AdStateManager.isAppOpenAdShowing = false

        callback.onAdFailedToShow("Exception: ${e.localizedMessage ?: "Unknown"}")
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAdRef.get() != null && wasLoadTimeLessThanNHoursAgo()
    }

    fun isAdLoading(): Boolean {
        return isAdLoading.get()
    }

    fun hasUsableAd(): Boolean {
        return isAdAvailable()
    }

    fun isShowingOrShowingSoon(): Boolean {
        return isAppOpenAdShowing.get() || showAttemptInProgress.get()
    }

    private fun wasLoadTimeLessThanNHoursAgo(): Boolean {
        val dateDifference = System.currentTimeMillis() - loadTimeRef.get()
        val numMilliSecondsPerHour = 3600000L
        return dateDifference < numMilliSecondsPerHour * 4
    }

    fun destroy() {
        isDestroyed.set(true)
        scope.cancel()
        mainHandler.removeCallbacksAndMessages(null)
        appOpenAdRef.getAndSet(null)?.fullScreenContentCallback = null
//        appOpenDialog?.safeDismiss()
//        appOpenDialog = null
        currentActivityRef.set(null)
    }

}
