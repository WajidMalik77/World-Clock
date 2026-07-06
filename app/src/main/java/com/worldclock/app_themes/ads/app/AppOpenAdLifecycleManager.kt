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
import com.worldclock.app_themes.ads.helpers.models.AdNetwork
import com.worldclock.app_themes.ads.helpers.models.AdWaterfallPlan
import com.worldclock.app_themes.ads.managers.AppOpenManager
import com.worldclock.app_themes.ads.managers.facebook.FbAdInitializer
import com.worldclock.app_themes.ads.managers.facebook.FbAppOpenManager
import com.worldclock.app_themes.ads.utils.ADS
import com.worldclock.app_themes.ads.utils.ADS.TEST_ADMOB_APP_OPEN_ID
import com.worldclock.app_themes.ads.utils.ADS.TEST_ADMOB_APP_OPEN_SPLASH_ID
import com.worldclock.app_themes.ads.utils.ADS.TEST_FB_APP_OPEN_AD_ID
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
        private const val RESUME_APP_OPEN_WAIT_TIMEOUT_MS = 8000L
        private const val SPLASH_APP_OPEN_WAIT_TIMEOUT_MS = 12_000L
    }

    private var currentActivity: Activity? = null
    private lateinit var appOpenManager: AppOpenManager
    private val fbAppOpenManager: FbAppOpenManager by lazy { FbAppOpenManager.getInstance(adsPref) }
    private var backgroundedAtMillis: Long = 0L
    private var suppressResumeUntilMillis: Long = 0L
    private val resumeFlowInProgress = AtomicBoolean(false)
    private val lifecycleAttached = AtomicBoolean(false)
    private var resumeLoadingDialog: LoadingDialog? = null
    private val resumeDialogHandler = Handler(Looper.getMainLooper())
    private var resumeLoadingTimeoutRunnable: Runnable? = null

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
        preloadSplashAdInternal(onPrepared = onPrepared)
    }

    private fun resolveAdUnitId(network: AdNetwork, type: String): String = when (network) {
        AdNetwork.FACEBOOK -> if (BuildConfig.DEBUG) {
            TEST_FB_APP_OPEN_AD_ID
        } else if (type == "splash") {
            adControlConfigManager.getProdFbAppOpenSplashAdUnitId()
        } else {
            adControlConfigManager.getProdFbAppOpenResumeAdUnitId()
        }
        else -> if (BuildConfig.DEBUG) {
            if (type == "splash") TEST_ADMOB_APP_OPEN_SPLASH_ID else TEST_ADMOB_APP_OPEN_ID
        } else if (type == "splash") {
            adControlConfigManager.getProdAppOpenSplashAdUnitId(ADS.PROD_ADMOB_APP_OPEN_SPLASH_ID)
        } else {
            adControlConfigManager.getProdAppOpenResumeAdUnitId(ADS.PROD_ADMOB_APP_OPEN_ID)
        }
    }

    private fun hasUsableAd(network: AdNetwork, type: String): Boolean = when (network) {
        AdNetwork.FACEBOOK -> fbAppOpenManager.hasUsableAd(resolveAdUnitId(network, type))
        else -> appOpenManager.hasUsableAd(
            adUnitId = resolveAdUnitId(network, type),
            fallbackAdUnitId = if (type == "splash") ADS.PROD_ADMOB_APP_OPEN_SPLASH_ID else ADS.PROD_ADMOB_APP_OPEN_ID
        )
    }

    private fun isAdLoading(network: AdNetwork): Boolean = when (network) {
        AdNetwork.FACEBOOK -> fbAppOpenManager.isAdLoading()
        else -> appOpenManager.isAdLoading()
    }

    private fun hasUsableAd(plan: AdWaterfallPlan, type: String): Boolean =
        plan.networksInOrder().any { hasUsableAd(it, type) }

    private fun loadOn(
        network: AdNetwork,
        adUnitId: String,
        fallbackAdUnitId: String,
        onLoaded: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        when (network) {
            AdNetwork.FACEBOOK -> {
                FbAdInitializer.initialize(appContext)
                fbAppOpenManager.loadAppOpenAd(appContext, adUnitId, onLoaded, onFailed)
            }
            else -> appOpenManager.loadAppOpenAd(
                adUnitId = adUnitId,
                adsPref = adsPref,
                fallbackAdUnitId = fallbackAdUnitId,
                onAdLoaded = { onLoaded() },
                onAdFailed = { error -> onFailed(error.message) }
            )
        }
    }

    private fun preloadSplashAdInternal(
        onPrepared: ((Boolean) -> Unit)? = null
    ) {
        try {
            val canRequestAds = canRequestAds()
            if (!canRequestAds || premiumRepository.isPremiumUser() || !adControlConfigManager.shouldShowAppOpenSplash()) {
                onPrepared?.invoke(false)
                return
            }
            val plan = adControlConfigManager.getAppOpenWaterfallPlan("splash")
            if (plan == null) {
                onPrepared?.invoke(false)
                return
            }
            val adId = resolveAdUnitId(plan.primary, "splash")
            if (adId.isBlank()) {
                onPrepared?.invoke(false)
                return
            }
            initializeAppOpenManager()
            if (hasUsableAd(plan, "splash")) {
                onPrepared?.invoke(true)
                return
            }
            if (isAdLoading(plan.primary)) {
                onPrepared?.invoke(true)
                return
            }
            loadOn(
                network = plan.primary,
                adUnitId = adId,
                fallbackAdUnitId = ADS.PROD_ADMOB_APP_OPEN_SPLASH_ID,
                onLoaded = { onPrepared?.invoke(true) },
                onFailed = { message ->
                    val fallback = plan.fallback
                    val fallbackId = fallback?.let { resolveAdUnitId(it, "splash") }
                    if (fallback != null && !fallbackId.isNullOrBlank()) {
                        Timber.tag(TAG_AO).w("Splash primary=${plan.primary} load failed ($message), trying fallback=$fallback")
                        loadOn(
                            network = fallback,
                            adUnitId = fallbackId,
                            fallbackAdUnitId = ADS.PROD_ADMOB_APP_OPEN_SPLASH_ID,
                            onLoaded = { onPrepared?.invoke(true) },
                            onFailed = { handleSplashLoadFailure(it, onPrepared) }
                        )
                    } else {
                        handleSplashLoadFailure(message, onPrepared)
                    }
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "Error preloading splash app open ad")
            onPrepared?.invoke(false)
        }
    }

    private fun handleSplashLoadFailure(
        message: String,
        onPrepared: ((Boolean) -> Unit)?
    ) {
        Timber.tag(TAG_AO).w("Splash app-open load failed: $message")
        onPrepared?.invoke(false)
    }

    private fun pollForLoadedAdAndShow(
        plan: AdWaterfallPlan,
        type: String,
        totalWaitMs: Long,
        intervalMs: Long,
        onAdShown: (() -> Unit)?,
        onFinished: () -> Unit
    ) {
        val handler = Handler(Looper.getMainLooper())
        val deadline = System.currentTimeMillis() + totalWaitMs
        val tick = object : Runnable {
            override fun run() {
                if (hasUsableAd(plan, type)) {
                    showAppOpenAdDirect(plan, type, onAdShown, onFinished)
                } else if (System.currentTimeMillis() >= deadline || plan.networksInOrder().none { isAdLoading(it) }) {
                    if (hasUsableAd(plan, type)) {
                        showAppOpenAdDirect(plan, type, onAdShown, onFinished)
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

    private fun pollResumeForLoadedAd(plan: AdWaterfallPlan, totalWaitMs: Long, intervalMs: Long) {
        val deadline = System.currentTimeMillis() + totalWaitMs
        val tick = object : Runnable {
            override fun run() {
                if (hasUsableAd(plan, "resume")) {
                    showResumeAppOpenAd(plan)
                } else if (System.currentTimeMillis() >= deadline || plan.networksInOrder().none { isAdLoading(it) }) {
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
            val plan = adControlConfigManager.getAppOpenWaterfallPlan("splash")
            if (plan == null) {
                onFinished()
                return
            }
            val adId = resolveAdUnitId(plan.primary, "splash")
            if (adId.isBlank()) {
                onFinished()
                return
            }
            if (hasUsableAd(plan, "splash")) {
                showAppOpenAdDirect(plan, "splash", onAdShown, onFinished)
                return
            }
            if (isAdLoading(plan.primary)) {
                // Extended wait so an in-flight load that completes a moment after the
                // splash budget elapsed still gets shown, instead of dropping the ad.
                pollForLoadedAdAndShow(
                    plan = plan,
                    type = "splash",
                    totalWaitMs = SPLASH_APP_OPEN_WAIT_TIMEOUT_MS,
                    intervalMs = 250L,
                    onAdShown = onAdShown,
                    onFinished = onFinished
                )
                return
            }
            loadOn(
                network = plan.primary,
                adUnitId = adId,
                fallbackAdUnitId = ADS.PROD_ADMOB_APP_OPEN_SPLASH_ID,
                onLoaded = {
                    val act = currentActivity
                    if (act != null && isOnSplashScreen(act)) {
                        showAppOpenAdDirect(plan, "splash", onAdShown, onFinished)
                    } else {
                        Timber.w("Splash App Open loaded late, discarding to prevent showing over Home")
                        onFinished()
                    }
                },
                onFailed = { message ->
                    val fallback = plan.fallback
                    val fallbackId = fallback?.let { resolveAdUnitId(it, "splash") }
                    if (fallback != null && !fallbackId.isNullOrBlank()) {
                        Timber.tag(TAG_AO).w("Splash primary=${plan.primary} load failed ($message), trying fallback=$fallback")
                        loadOn(
                            network = fallback,
                            adUnitId = fallbackId,
                            fallbackAdUnitId = ADS.PROD_ADMOB_APP_OPEN_SPLASH_ID,
                            onLoaded = {
                                val act = currentActivity
                                if (act != null && isOnSplashScreen(act)) {
                                    showAppOpenAdDirect(plan, "splash", onAdShown, onFinished)
                                } else {
                                    onFinished()
                                }
                            },
                            onFailed = { onFinished() }
                        )
                    } else {
                        onFinished()
                    }
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
        resumeFlowInProgress.set(false)
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
        plan: AdWaterfallPlan,
        type: String,
        onAdShown: (() -> Unit)? = null,
        onFinished: (() -> Unit)? = null
    ) {
        try {
            val readyNetwork = plan.networksInOrder().firstOrNull { hasUsableAd(it, type) } ?: plan.primary
            val callback = object : AdShowCallback {
                override fun onAdShown() {
                    Timber.d("App Open ad shown (network=$readyNetwork)")
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
            }
            if (readyNetwork == AdNetwork.FACEBOOK) {
                val activity = currentActivity
                if (activity != null) {
                    fbAppOpenManager.showAppOpenAdIfAvailable(activity, callback)
                } else {
                    onFinished?.invoke()
                }
            } else {
                appOpenManager.showAppOpenAdIfAvailable(adsPref, callback)
            }
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

        val plan = adControlConfigManager.getAppOpenWaterfallPlan("resume")
        if (plan == null) {
            Timber.tag(TAG_AO).w("blocked: resume waterfall plan is null")
            resumeFlowInProgress.set(false)
            return
        }

        val resumeAdUnitId = resolveAdUnitId(plan.primary, "resume")
        if (resumeAdUnitId.isBlank()) {
            Timber.tag(TAG_AO).w("blocked: resume ad unit id is blank")
            resumeFlowInProgress.set(false)
            return
        }

        if (hasUsableAd(plan, "resume")) {
            Timber.tag(TAG_AO).d("resume path: using cached app-open ad, no loading dialog needed")
            showResumeAppOpenAd(plan)
            return
        }

        if (isAdLoading(plan.primary)) {
            Timber.tag(TAG_AO).d("resume load already in progress, waiting up to ${RESUME_APP_OPEN_WAIT_TIMEOUT_MS}ms")
            showResumeLoadingIfPossible()
            pollResumeForLoadedAd(plan, totalWaitMs = RESUME_APP_OPEN_WAIT_TIMEOUT_MS, intervalMs = 250L)
            return
        }

        Timber.tag(TAG_AO).d("loading resume app-open ad id=$resumeAdUnitId")
        showResumeLoadingIfPossible()
        loadOn(
            network = plan.primary,
            adUnitId = resumeAdUnitId,
            fallbackAdUnitId = ADS.PROD_ADMOB_APP_OPEN_ID,
            onLoaded = {
                Timber.tag(TAG_AO).d("resume load success, calling show")
                showResumeAppOpenAd(plan)
            },
            onFailed = { message ->
                val fallback = plan.fallback
                val fallbackId = fallback?.let { resolveAdUnitId(it, "resume") }
                if (fallback != null && !fallbackId.isNullOrBlank()) {
                    Timber.tag(TAG_AO).w("resume primary=${plan.primary} load failed ($message), trying fallback=$fallback")
                    loadOn(
                        network = fallback,
                        adUnitId = fallbackId,
                        fallbackAdUnitId = ADS.PROD_ADMOB_APP_OPEN_ID,
                        onLoaded = {
                            Timber.tag(TAG_AO).d("resume fallback load success, calling show")
                            showResumeAppOpenAd(plan)
                        },
                        onFailed = {
                            Timber.tag(TAG_AO).w("resume fallback load failed: $it")
                            dismissResumeLoading()
                            resumeFlowInProgress.set(false)
                        }
                    )
                } else {
                    Timber.tag(TAG_AO).w("resume load failed: $message")
                    dismissResumeLoading()
                    resumeFlowInProgress.set(false)
                }
            }
        )
    }

    private fun showResumeAppOpenAd(plan: AdWaterfallPlan) {
        dismissResumeLoading()
        showAppOpenAdDirect(
            plan = plan,
            type = "resume",
            onFinished = {
                resumeFlowInProgress.set(false)
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
        resumeLoadingTimeoutRunnable = Runnable {
            if (resumeLoadingDialog != null) {
                Timber.tag(TAG_AO).w("resume loading dialog timeout -> dismissing")
                dismissResumeLoading()
                resumeFlowInProgress.set(false)
            }
        }.also { timeout ->
            resumeDialogHandler.postDelayed(timeout, RESUME_APP_OPEN_WAIT_TIMEOUT_MS)
        }
    }

    private fun dismissResumeLoading() {
        resumeLoadingTimeoutRunnable?.let { resumeDialogHandler.removeCallbacks(it) }
        resumeLoadingTimeoutRunnable = null
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
