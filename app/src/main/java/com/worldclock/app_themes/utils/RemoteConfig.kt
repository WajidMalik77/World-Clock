package com.worldclock.app_themes.utils

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

class RemoteConfig {
    companion object {
        private const val TAG = "RCKEY"

        fun getRCValues(context: Context, callback: () -> Unit) {
            val remoteConfig = Firebase.remoteConfig
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 0L
            }
            remoteConfig.setConfigSettingsAsync(configSettings)

            remoteConfig.fetchAndActivate()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "getRCValues: success")
                    }
                    callback.invoke()
                }
                .addOnFailureListener {
                    Log.d(TAG, "getRCValues failed: $it")
                    callback.invoke()
                }
        }
    }
}