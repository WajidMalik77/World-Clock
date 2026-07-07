package com.worldclock.app_themes.ads.helpers

import timber.log.Timber
import com.worldclock.app_themes.ads.config.AdControlConfigManager
import com.worldclock.app_themes.ads.config.NativeAdConfigManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdConfigInitializer @Inject constructor(
    private val adControlConfigManager: AdControlConfigManager,
    private val nativeAdConfigManager: NativeAdConfigManager
) {
    private var isConfigLoaded = false
    private var isNativeConfigLoaded = false
    private var isConfigFailed = false
    private var isNativeConfigFailed = false

    private val readyListeners = mutableListOf<() -> Unit>()
    private val failedListeners = mutableListOf<() -> Unit>()

    private var initialized = false

    /**
     * Call once in Application.onCreate()
     */
    fun preloadConfigs(forceReload: Boolean = false) {
        if (initialized && !forceReload) return

        initialized = true
        resetState()

        isConfigLoaded = false
        isNativeConfigLoaded = false
        isConfigFailed = false
        isNativeConfigFailed = false

        adControlConfigManager.setOnConfigAvailableListener {
            isConfigLoaded = true
            notifyIfFinished()
        }

        nativeAdConfigManager.setOnConfigAvailableListener {
            isNativeConfigLoaded = true
            notifyIfFinished()
        }

        adControlConfigManager.fetchAdConfig()
        nativeAdConfigManager.fetchNativeConfig()
    }

    /**
     * Set listeners in any screen; if configs are already ready, it will trigger immediately.
     */
    fun setListener(
        onReady: () -> Unit,
        onFailed: () -> Unit = {}
    ) {
        if (isReady()) {
            onReady()
            return
        }
        if (isFailed()) {
            onFailed()
            return
        }
        readyListeners.add(onReady)
        failedListeners.add(onFailed)

        when {
            isReady() -> notifySuccess()
            isFailed() -> notifyFailure()
        }
    }

    fun isReady(): Boolean = /*isApiConfigLoaded &&*/ isConfigLoaded && isNativeConfigLoaded
    fun isFailed(): Boolean = isConfigFailed || isNativeConfigFailed

    private fun notifyIfFinished() {
        when {
            isReady() -> notifySuccess()
            isFailed() -> notifyFailure()
        }
    }

    private fun notifySuccess() {
        val snapshot = readyListeners.toList()
        clearCallbacks()
        Timber.tag("ConfigTrace").d("Config fetch completed at time=%d", System.currentTimeMillis())
        snapshot.forEach { it.invoke() }
    }

    private fun notifyFailure() {
        val snapshot = failedListeners.toList()
        clearCallbacks()
        snapshot.forEach { it.invoke() }
    }

    private fun clearCallbacks() {
        readyListeners.clear()
        failedListeners.clear()
    }

    private fun resetState() {
        isConfigLoaded = false
        isNativeConfigLoaded = false
        isConfigFailed = false
        isNativeConfigFailed = false
    }
}