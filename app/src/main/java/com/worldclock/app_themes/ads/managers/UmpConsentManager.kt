package com.worldclock.app_themes.ads.managers

import android.app.Activity
import android.content.Context
import timber.log.Timber
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class UmpConsentManager @Inject constructor() {
    companion object {
        private const val TAG_UMP = "UmpConsent"
    }

    suspend fun gatherConsent(activity: Activity): Boolean = suspendCancellableCoroutine { continuation ->
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)

        // For testing purposes, you can enable debug settings if needed.
        // val debugSettings = ConsentDebugSettings.Builder(activity)
        //     .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
        //     .build()

        val params = ConsentRequestParameters.Builder()
            // .setConsentDebugSettings(debugSettings)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    activity
                ) { loadAndShowError ->
                    if (loadAndShowError != null) {
                        Timber.e("UMP loadAndShowError: %s", loadAndShowError.message)
                    }

                    // Consent has been gathered (either not required, or obtained)
                    Timber.tag(TAG_UMP).d("Consent gathered. canRequestAds: ${consentInformation.canRequestAds()}")
                    if (continuation.isActive) {
                        continuation.resume(consentInformation.canRequestAds())
                    }
                }
            },
            { requestConsentError ->
                Timber.e("UMP requestConsentError: %s", requestConsentError.message)
                if (continuation.isActive) {
                    continuation.resume(consentInformation.canRequestAds())
                }
            }
        )
    }

    fun canRequestAds(activity: Activity): Boolean {
        return UserMessagingPlatform.getConsentInformation(activity).canRequestAds()
    }

    fun canRequestAdsFromCache(context: Context): Boolean {
        return runCatching {
            UserMessagingPlatform.getConsentInformation(context).canRequestAds()
        }.getOrDefault(false)
    }
}
