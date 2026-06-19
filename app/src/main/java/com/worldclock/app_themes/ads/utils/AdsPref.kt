package com.worldclock.app_themes.ads.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import com.worldclock.app_themes.ads.utils.Constants.PRODUCT_LIFETIME
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdsPref @Inject constructor(
    @ApplicationContext context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_ads", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_PREMIUM = "is_premium"
        private const val KEY_TRIAL_START_TIME = "trial_start_time"
        private const val KEY_NPA = "npa"
    }
    private var cachedIsPremium: Boolean? = null

    fun getIsPremiumStatus(): Boolean {
        return try {
            // Read latest persisted value to avoid stale in-memory state after purchase.
            val isPremium = sharedPreferences.getBoolean(KEY_IS_PREMIUM, false)
            cachedIsPremium = isPremium
            isPremium
        } catch (e: Exception) {
            false
        }
    }

    fun setIsPremiumStatus(isPremium: Boolean) {
        cachedIsPremium = isPremium
        sharedPreferences.edit { putBoolean(KEY_IS_PREMIUM, isPremium) }
    }

    private var cachedPurchasedProducts: Set<String>? = null

    fun getPurchasedProducts(): Set<String> {
        if (cachedPurchasedProducts != null) {
            return cachedPurchasedProducts ?: emptySet()
        }

        val products = sharedPreferences.getStringSet("purchased_products", null)
            ?.toSet()
            ?: emptySet()

        cachedPurchasedProducts = products
        return products
    }

    fun setPurchasedProductsSet(products: Set<String>?) {
        if (products == null) return

        if (products.isEmpty()) {
            cachedPurchasedProducts = emptySet()
            sharedPreferences.edit { putStringSet("purchased_products", emptySet()) }
            return
        }

        val result = if (products.contains(PRODUCT_LIFETIME)) {
            setOf(PRODUCT_LIFETIME)
        } else {
            products.toSet()
        }

        cachedPurchasedProducts = result
        sharedPreferences.edit { putStringSet("purchased_products", result) }
    }

    // Rarely accessed methods - no caching needed
    fun setTrialStartTime(startTime: Long) {
        sharedPreferences.edit { putLong(KEY_TRIAL_START_TIME, startTime) }
    }

    fun getTrialStartTime(): Long {
        return sharedPreferences.getLong(KEY_TRIAL_START_TIME, 0L)
    }

    fun setNpaStatus(npa: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_NPA, npa) }
    }

    fun isNpa(): Boolean {
        return sharedPreferences.getBoolean(KEY_NPA, false)
    }

    fun isDebugToastBannerEnabled(): Boolean =
        sharedPreferences.getBoolean("debug_toast_banner", false)

    fun setDebugToastBannerEnabled(enabled: Boolean) {
        sharedPreferences.edit { putBoolean("debug_toast_banner", enabled) }
    }

    fun isDebugToastNativeEnabled(): Boolean =
        sharedPreferences.getBoolean("debug_toast_native", false)

    fun setDebugToastNativeEnabled(enabled: Boolean) {
        sharedPreferences.edit { putBoolean("debug_toast_native", enabled) }
    }

    fun isDebugToastInterstitialEnabled(): Boolean =
        sharedPreferences.getBoolean("debug_toast_interstitial", false)

    fun setDebugToastInterstitialEnabled(enabled: Boolean) {
        sharedPreferences.edit { putBoolean("debug_toast_interstitial", enabled) }
    }

    fun isDebugToastAppOpenEnabled(): Boolean =
        sharedPreferences.getBoolean("debug_toast_appopen", false)

    fun setDebugToastAppOpenEnabled(enabled: Boolean) {
        sharedPreferences.edit { putBoolean("debug_toast_appopen", enabled) }
    }
}
