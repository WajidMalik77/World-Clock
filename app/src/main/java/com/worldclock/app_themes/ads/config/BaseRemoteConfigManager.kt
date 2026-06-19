package com.worldclock.app_themes.ads.config

import timber.log.Timber
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseRemoteConfigManager<T>(
    protected val firebaseRemoteConfig: FirebaseRemoteConfig,
    private val remoteKey: String
) {
    protected var configData: T? = null
    private var onConfigAvailableCallback: (() -> Unit)? = null
    protected var isConfigListenerSet = false

    fun fetchConfig() {
        Timber.tag("ConfigTrace").d("fetchConfig start key=$remoteKey manager=${this::class.java.simpleName}")
        firebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val json = firebaseRemoteConfig.getString(remoteKey)
                    Timber.tag("ConfigTrace").d("fetchConfig complete key=$remoteKey success=true jsonLength=${json.length}"
                    )
                    if (json.isNotBlank()) {
                        CoroutineScope(Dispatchers.Default).launch {
                            try {
                                val parsedData = parseJson(json)
                                withContext(Dispatchers.Main) {
                                    configData = parsedData
                                    Timber.tag("ConfigTrace").d("parse result key=$remoteKey parsedNull=${parsedData == null}"
                                    )
                                    postProcessParsedData(json)
                                    onConfigAvailableCallback?.invoke()
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "$remoteKey configuration parse error")
                            }
                        }
                    } else {
                        Timber.tag("ConfigTrace").e("$remoteKey JSON is blank")
                    }
                } else {
                    Timber.e(task.exception, "Remote Config fetch failed for $remoteKey")
                }
            }
            .addOnFailureListener {
                Timber.e(it, "Fetch and activate failed for $remoteKey")
            }
    }

    protected abstract fun parseJson(json: String): T?

    protected open fun postProcessParsedData(json: String) {}

    open fun setOnConfigAvailableListener(callback: () -> Unit) {
        if (isConfigListenerSet) return
        isConfigListenerSet = true
        this.onConfigAvailableCallback = callback
        if (configData != null) callback()
    }

    fun getConfig(): T? {
        if (configData == null) fetchConfig()
        return configData
    }
}
