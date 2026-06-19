package com.worldclock.app_themes.presentation.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.worldclock.app_themes.R
import com.worldclock.app_themes.ads.config.AdControlConfigManager
import com.worldclock.app_themes.ads.config.NativeAdConfigManager
import com.worldclock.app_themes.ads.di.AdConfigEntryPoint
import com.worldclock.app_themes.ads.di.AppOpenEntryPoint
import com.worldclock.app_themes.ads.helpers.AdConfigInitializer
import com.worldclock.app_themes.ads.helpers.loadBottomNative
import com.worldclock.app_themes.ads.helpers.safeShowInterstitialAction
import com.worldclock.app_themes.ads.helpers.models.NativeAdEvent
import com.worldclock.app_themes.ads.managers.UmpConsentManager
import com.worldclock.app_themes.ads.app.AdsManager
import com.worldclock.app_themes.databinding.ActivitySplashBinding
import com.worldclock.app_themes.core.utils.AdsConstants.LifeTimePref
import com.worldclock.app_themes.core.utils.AdsConstants.PrefsName
import com.worldclock.app_themes.core.utils.AdsConstants.isFirstTime
import com.worldclock.app_themes.core.utils.PrefUtil
import com.worldclock.app_themes.core.analytics.AppEventLogger
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume

@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
class Splash : BaseActivity() {

    private val binding by lazy { ActivitySplashBinding.inflate(layoutInflater) }

    @Inject lateinit var umpConsentManager: UmpConsentManager
    @Inject lateinit var adsManager: AdsManager
    @Inject lateinit var adConfigInitializer: AdConfigInitializer

    private var isNext = false
    private var splashNativeLoaded = false
    private var splashNativeEnabled = false
    private var splashNativeExpected = false
    private var splashNativeExpectedPositions: Set<String> = emptySet()

    private var getStartedConsumed = false
    private var splashAppOpenWarmupStarted = false
    private var splashAppOpenHandledBeforeCta = false

    private lateinit var appUpdateManager: AppUpdateManager

    private val appUpdateLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            startMainActivity()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)
        AppEventLogger.trackScreenCreate(this, savedInstanceState, "Splash", "activity_lifecycle")
        AppEventLogger.trackFunnelStep("app_startup", "splash_created", screenName = "Splash", source = "lifecycle")
        appUpdateManager = AppUpdateManagerFactory.create(this)

        binding.getStarted.visibility = View.GONE
        binding.action.visibility = View.GONE
        binding.getStartedActionSpacer.visibility = View.GONE
        binding.imgLoading.visibility = View.GONE

        binding.getStarted.setOnClickListener {
            if (getStartedConsumed) return@setOnClickListener
            getStartedConsumed = true
            AppEventLogger.trackButtonClick("Splash", "get_started", "continue", "splash_flow")
            AppEventLogger.trackFunnelStep("app_startup", "get_started_clicked", screenName = "Splash", source = "button")
            showLoadingAfterGetStartedClick()
            continueAfterAds()
        }

        binding.splashImage.setImageResource(R.drawable.ic_splash)

        startProgressBar()
        startSplashFlow()
    }

    private fun startProgressBar() {
        val progressBar = binding.splashProgress
        val handler = Handler(Looper.getMainLooper())
        var progress = 0
        val runnable = object : Runnable {
            override fun run() {
                if (progress <= 100) {
                    progressBar.progress = progress
                    progress += 2
                    handler.postDelayed(this, 80)
                }
            }
        }
        handler.post(runnable)
    }

    private fun startSplashFlow() {
        lifecycleScope.launch {

            // Phase 1: Run App Update + UMP consent + config preloads in parallel
            val updateInfoAsync = async { checkAppUpdateSuspending() }
            val umpConsentAsync = async { umpConsentManager.gatherConsent(this@Splash) }
            val configAsync = async(Dispatchers.IO) {
                runCatching {
                    adConfigInitializer.preloadConfigs(false)
                    FirebaseRemoteConfig.getInstance().fetchAndActivate().await()
                }
            }

            val updateInfo = updateInfoAsync.await()
            val canRequestAds = umpConsentAsync.await()
            configAsync.await()

            // Phase 1.5: Mandatory Update Check
            if (updateInfo != null && shouldShowImmediateUpdate(updateInfo)) {
                startImmediateUpdate(updateInfo)
                return@launch // Stop ad flow, wait for update UI
            }

            // Phase 2: Initialize MobileAds if consent granted
            if (canRequestAds) {
                adsManager.initializeIfNeeded()
                adsManager.awaitInitialization()
            }

            // Phase 3: Evaluate splash native gate
            configureSplashNativeGate()
            if (!splashNativeEnabled) {
                binding.action.visibility = View.VISIBLE
            }

            val adControlConfigManager = EntryPointAccessors.fromActivity(
                this@Splash,
                AdConfigEntryPoint::class.java
            ).adControlConfigManager()

            // If neither native nor app open ad is expected, navigate immediately
            if (!splashNativeExpected && !adControlConfigManager.shouldShowAppOpenSplash()) {
                startMainActivity()
                return@launch
            }

            // Phase 4: Ad request strategy. If native splash is enabled, render/native-impress
            // and app-open load in parallel. Native impression is the trigger to show app-open,
            // and the CTA is revealed only after app-open finishes or is unavailable.
            if (splashNativeEnabled) {
                Timber.tag(TAG_SPLASH_ADS).d("parallel load start: splash native + splash app-open")
                startSplashAppOpenWarmupForCta()
                val completed = withTimeoutOrNull(SPLASH_AD_SEQUENCE_TIMEOUT_MS) {
                    loadSplashNativeThenShowAppOpenSuspending()
                }
                if (completed == null) {
                    Timber.tag(TAG_SPLASH_ADS).w("splash ad sequence timed out; showing CTA")
                    showGetStartedCta()
                }
            } else {
                withTimeoutOrNull(15_000) {
                    preloadSplashAppOpenAdSuspending()
                }
            }

            // Phase 5: Show CTA or auto-navigate
            lifecycle.whenStarted {
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    if (splashNativeEnabled) {
                        showGetStartedCta()
                    } else {
                        continueAfterAds()
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------------------
    // Ad helpers
    // ---------------------------------------------------------------------------

    private suspend fun preloadSplashAppOpenAdSuspending() = suspendCancellableCoroutine<Unit> { cont ->
        val appOpenManager = EntryPointAccessors.fromActivity(
            this@Splash,
            AppOpenEntryPoint::class.java
        ).appOpenAdLifecycleManager()
        appOpenManager.preloadSplashAd {
            if (cont.isActive) cont.resume(Unit)
        }
    }

    private suspend fun preloadSplashNativeAdSuspending() = suspendCancellableCoroutine<Unit> { cont ->
        if (!splashNativeEnabled) {
            loadBottomNative(
                screen = "SplashScreen",
                topNativeLayout = binding.adsContainerTop.root,
                bottomLayout = binding.adsContainerBottom.root,
                loadBannerAds = false
            )
            if (cont.isActive) cont.resume(Unit)
            return@suspendCancellableCoroutine
        }

        var resolved = false
        val pendingPositions = splashNativeExpectedPositions.toMutableSet()
        fun resumeOnce() {
            if (!resolved && cont.isActive) {
                resolved = true
                cont.resume(Unit)
            }
        }
        fun markPositionResolved(position: String) {
            pendingPositions.remove(position.lowercase())
            if (pendingPositions.isEmpty()) {
                resumeOnce()
            }
        }

        loadBottomNative(
            screen = "SplashScreen",
            topNativeLayout = binding.adsContainerTop.root,
            bottomLayout = binding.adsContainerBottom.root,
            loadBannerAds = false,
            onEvent = { event ->
                when (event) {
                    is NativeAdEvent.Loaded -> {
                        splashNativeLoaded = true
                        startSplashAppOpenWarmupForCta()
                        lifecycleScope.launch {
                            delay(SPLASH_NATIVE_IMPRESSION_GRACE_MS)
                            markPositionResolved(event.position)
                        }
                    }
                    is NativeAdEvent.Impression -> {
                        markPositionResolved(event.position)
                    }
                    is NativeAdEvent.Failed -> markPositionResolved(event.position)
                    is NativeAdEvent.Off -> markPositionResolved(event.position)
                    NativeAdEvent.AllOffFromConfig -> resumeOnce()
                }
            }
        )
    }

    private suspend fun loadSplashNativeThenShowAppOpenSuspending() = suspendCancellableCoroutine<Unit> { cont ->
        if (!splashNativeEnabled) {
            if (cont.isActive) cont.resume(Unit)
            return@suspendCancellableCoroutine
        }

        var appOpenFlowStarted = false
        var resolved = false

        cont.invokeOnCancellation {
            resolved = true
        }

        fun finishOnce() {
            if (resolved) return
            resolved = true
            if (!isFinishing && !isDestroyed) {
                showGetStartedCta()
            }
            if (cont.isActive) cont.resume(Unit)
        }

        fun showAppOpenOnce() {
            if (appOpenFlowStarted) return
            appOpenFlowStarted = true
            Timber.tag(TAG_SPLASH_ADS).d("splash app-open show requested after native signal")
            showSplashAppOpenThen { finishOnce() }
        }

        loadBottomNative(
            screen = "SplashScreen",
            topNativeLayout = binding.adsContainerTop.root,
            bottomLayout = binding.adsContainerBottom.root,
            loadBannerAds = false,
            onEvent = { event ->
                when (event) {
                    is NativeAdEvent.Loaded -> {
                        splashNativeLoaded = true
                        Timber.tag(TAG_SPLASH_ADS).d("splash native loaded position=${event.position}; waiting for impression")
                        lifecycleScope.launch {
                            delay(SPLASH_NATIVE_IMPRESSION_GRACE_MS)
                            Timber.tag(TAG_SPLASH_ADS).d("splash native impression grace elapsed position=${event.position}; requesting app-open")
                            showAppOpenOnce()
                        }
                    }
                    is NativeAdEvent.Impression -> {
                        Timber.tag(TAG_SPLASH_ADS).d("splash native impression position=${event.position}; requesting app-open")
                        showAppOpenOnce()
                    }
                    is NativeAdEvent.Failed,
                    is NativeAdEvent.Off,
                    NativeAdEvent.AllOffFromConfig -> {
                        Timber.tag(TAG_SPLASH_ADS).d("splash native unavailable event=${event::class.java.simpleName}; requesting app-open")
                        showAppOpenOnce()
                    }
                }
            }
        )
    }

    private fun configureSplashNativeGate() {
        val nativeConfigManager = EntryPointAccessors.fromActivity(
            this,
            AdConfigEntryPoint::class.java
        ).nativeAdConfigManager()

        if (nativeConfigManager.getConfig() == null) {
            splashNativeExpected = false
            splashNativeEnabled = false
            return
        }

        val expectedPositions = listOf("top", "bottom").filter { position ->
            nativeConfigManager.isNativeVisible("SplashScreen", position)
        }
        splashNativeExpectedPositions = expectedPositions.map { it.lowercase() }.toSet()
        splashNativeExpected = expectedPositions.isNotEmpty()
        splashNativeEnabled  = splashNativeExpected
    }

    // ---------------------------------------------------------------------------
    // UI transitions
    // ---------------------------------------------------------------------------

    private fun showGetStartedCta() {
        binding.splashProgress.visibility = View.GONE
        binding.imgLoading.visibility     = View.GONE
        binding.action.visibility         = View.VISIBLE
        binding.getStartedActionSpacer.visibility = View.VISIBLE
        binding.getStarted.visibility     = View.VISIBLE
    }

    private fun startSplashAppOpenWarmupForCta() {
        if (splashAppOpenWarmupStarted) return
        splashAppOpenWarmupStarted = true

        lifecycleScope.launch {
            if (
                splashNativeEnabled &&
                !getStartedConsumed &&
                lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
            ) {
                preloadSplashAppOpenAdSuspending()
            }
        }
    }

    private fun showLoadingAfterGetStartedClick() {
        binding.getStarted.isEnabled      = false
        binding.getStarted.visibility     = View.GONE
        binding.action.visibility         = View.GONE
        binding.getStartedActionSpacer.visibility = View.GONE
        binding.imgLoading.visibility     = View.VISIBLE
    }

    private fun continueAfterAds() {
        if (splashNativeEnabled && splashAppOpenHandledBeforeCta) {
            startMainActivity(showInterstitial = false)
            return
        }
        showSplashAppOpenThen { appOpenAdShown ->
            startMainActivity(showInterstitial = !appOpenAdShown)
        }
    }

    private fun showSplashAppOpenThen(afterAd: (appOpenAdShown: Boolean) -> Unit) {
        val appOpenManager = EntryPointAccessors.fromActivity(
            this,
            AppOpenEntryPoint::class.java
        ).appOpenAdLifecycleManager()

        var appOpenAdShown = false

        appOpenManager.showSplashAppOpenIfAvailable(
            onAdShown = {
                appOpenAdShown = true
                Timber.tag(TAG_SPLASH_ADS).d("splash app-open shown")
            }
        ) {
            if (splashNativeEnabled) {
                splashAppOpenHandledBeforeCta = true
            }
            Timber.tag(TAG_SPLASH_ADS).d("splash app-open finished shown=$appOpenAdShown")
            afterAd(appOpenAdShown)
        }
    }

    // ---------------------------------------------------------------------------
    // Navigation
    // ---------------------------------------------------------------------------

    private fun startMainActivity(showInterstitial: Boolean = false) {
        if (isNext) return
        isNext = true

        val isPremium = PrefUtil(this).getBool("is_premium", false)
            || getSharedPreferences(LifeTimePref, 0).getBoolean("premium", false)

        val proceed = {
            val cfg = EntryPointAccessors.fromActivity(
                this,
                AdConfigEntryPoint::class.java
            ).adControlConfigManager()

            startActivity(cfg.getNextScreenIntent(this, "splash"))
            finish()
        }

        if (isPremium) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        if (showInterstitial) {
            safeShowInterstitialAction(
                screenName = "SplashScreen",
                trigger = "splash",
                noCounterNeeded = true,
                afterAd = proceed
            )
        } else {
            proceed()
        }
    }

    private suspend fun checkAppUpdateSuspending(): AppUpdateInfo? = suspendCancellableCoroutine { cont ->
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { info ->
                if (cont.isActive) cont.resume(info)
            }
            .addOnFailureListener {
                if (cont.isActive) cont.resume(null)
            }
    }

    private fun shouldShowImmediateUpdate(info: AppUpdateInfo): Boolean {
        return info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
            info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
    }

    private fun startImmediateUpdate(info: AppUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                info,
                appUpdateLauncher,
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
            )
        } catch (e: Exception) {
            startMainActivity()
        }
    }

    override fun onDestroy() {
        AppEventLogger.trackScreenDestroy(this, "Splash")
        super.onDestroy()
    }

    companion object {
        private const val TAG_SPLASH_ADS = "SplashAdSequence"
        private const val SPLASH_NATIVE_IMPRESSION_GRACE_MS = 1200L
        private const val SPLASH_AD_SEQUENCE_TIMEOUT_MS = 24_000L
    }
}
