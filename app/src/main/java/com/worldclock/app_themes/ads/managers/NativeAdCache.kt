package com.worldclock.app_themes.ads.managers

import com.google.android.gms.ads.nativead.NativeAd
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Process-level singleton cache for one loaded-but-not-impressed native ad.
 *
 * If a screen matches a native ad but the Activity is destroyed before AdMob records an impression,
 * the next native container gets this ad before any fresh request is made. This keeps an already
 * matched ad from being abandoned just because the user navigated quickly.
 */
@Singleton
class NativeAdCache @Inject constructor() {

    private data class Entry(
        val ad: NativeAd,
        val loadedAt: Long,
        val adUnitId: String,
        val layoutRes: Int,
        val available: Boolean,
        val impressed: Boolean = false,
        val onImpression: (() -> Unit)? = null
    )

    private var singletonEntry: Entry? = null

    /** Kept for old call sites and log readability. Reuse is intentionally no longer key-bound. */
    fun key(adUnitId: String, layoutRes: Int): String = "$adUnitId#$layoutRes"

    /** Returns a reusable unmatched ad, regardless of the next placement's ad unit or layout. */
    @Synchronized
    fun consumeAny(): NativeAd? {
        val entry = singletonEntry ?: return null
        if (!entry.available || entry.impressed) return null
        if (isExpired(entry)) {
            singletonEntry = null
            runCatching { entry.ad.destroy() }
            Timber.d("NativeAdCache: singleton entry expired for ${key(entry.adUnitId, entry.layoutRes)}")
            return null
        }
        singletonEntry = entry.copy(available = false)
        Timber.d(
            "NativeAdCache: consuming singleton native from ${
                key(entry.adUnitId, entry.layoutRes)
            }"
        )
        return entry.ad
    }

    @Synchronized
    fun hasTrackedAd(): Boolean {
        val entry = singletonEntry ?: return false
        if (isExpired(entry)) {
            singletonEntry = null
            runCatching { entry.ad.destroy() }
            return false
        }
        return true
    }

    /** Stores the latest matched native as the singleton tracked ad. */
    @Synchronized
    fun rememberMatched(
        ad: NativeAd,
        adUnitId: String,
        layoutRes: Int,
        available: Boolean = false,
        onImpression: (() -> Unit)? = null
    ) {
        singletonEntry = Entry(
            ad = ad,
            loadedAt = System.currentTimeMillis(),
            adUnitId = adUnitId,
            layoutRes = layoutRes,
            available = available,
            onImpression = onImpression
        )
        Timber.d("NativeAdCache: remembered singleton native ${key(adUnitId, layoutRes)} available=$available")
    }

    @Synchronized
    fun setImpressionCallback(ad: NativeAd?, callback: (() -> Unit)?) {
        val entry = singletonEntry ?: return
        if (ad != null && entry.ad === ad) {
            singletonEntry = entry.copy(onImpression = callback)
        }
    }

    /** Makes the tracked ad available for the next native container if no impression happened. */
    @Synchronized
    fun releaseIfUnimpressed(ad: NativeAd?): Boolean {
        val entry = singletonEntry ?: return false
        if (ad == null || entry.ad !== ad || entry.impressed) return false
        if (isExpired(entry)) {
            singletonEntry = null
            runCatching { entry.ad.destroy() }
            return false
        }
        singletonEntry = entry.copy(available = true)
        Timber.d("NativeAdCache: released unimpressed singleton native for next container")
        return true
    }

    /** Drops a failed or invalid tracked ad without touching unrelated entries. */
    @Synchronized
    fun forget(ad: NativeAd?) {
        val entry = singletonEntry ?: return
        if (ad != null && entry.ad === ad) singletonEntry = null
    }

    /** Once AdMob records an impression, this ad should not be reused again. */
    @Synchronized
    fun markImpressed(ad: NativeAd?) {
        val entry = singletonEntry ?: return
        if (ad != null && entry.ad === ad) {
            singletonEntry = null
            entry.onImpression?.invoke()
            Timber.d("NativeAdCache: singleton native recorded impression")
        }
    }

    @Synchronized
    fun isTrackedUnimpressed(ad: NativeAd?): Boolean {
        val entry = singletonEntry ?: return false
        return ad != null && entry.ad === ad && !entry.impressed
    }

    @Synchronized
    fun isAvailable(ad: NativeAd?): Boolean {
        val entry = singletonEntry ?: return false
        return ad != null && entry.ad === ad && entry.available && !entry.impressed
    }

    private fun isExpired(entry: Entry): Boolean =
        System.currentTimeMillis() - entry.loadedAt > TTL_MS

    companion object {
        // AdMob native ads stay valid ~1h, but show them while fresh; this comfortably covers
        // round-trips to the wallpaper picker / preview and back.
        private const val TTL_MS = 4 * 60 * 1000L
    }
}
