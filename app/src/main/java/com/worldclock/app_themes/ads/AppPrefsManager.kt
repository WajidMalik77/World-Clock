package com.worldclock.app_themes.ads

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME = "app_preferences"

private val Context.dataStore by preferencesDataStore(name = PREFS_NAME)

@Singleton
class AppPrefsManager @Inject constructor(@param:ApplicationContext private val context: Context) {

    private val ds = context.dataStore
    private val _selectedLanguageCode = MutableStateFlow(DEFAULT_LANGUAGE_CODE)
    val selectedLanguageCodeFlow: StateFlow<String> = _selectedLanguageCode.asStateFlow()
    private val viewScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        viewScope.launch {
            ds.data.collect { prefs ->
                _selectedLanguageCode.value = prefs[KEY_SELECTED_LANGUAGE_CODE] ?: DEFAULT_LANGUAGE_CODE
            }
        }
    }

    fun isFirstLaunch(): Boolean = runBlocking(Dispatchers.IO) {
        ds.data.map { it[KEY_FIRST_LAUNCH] != false }.first()
    }

    fun setFirstLaunchComplete() {
        viewScope.launch {
            ds.edit { it[KEY_FIRST_LAUNCH] = false }
        }
    }

    fun isFirstLaunchIntro(): Boolean = runBlocking(Dispatchers.IO) {
        ds.data.map { it[KEY_FIRST_LAUNCH_INTRO] != false }.first()
    }

    fun setFirstLaunchIntroComplete() {
        viewScope.launch {
            ds.edit { it[KEY_FIRST_LAUNCH_INTRO] = false }
        }
    }

    fun isFirstLaunchPremium(): Boolean = runBlocking(Dispatchers.IO) {
        ds.data.map { it[KEY_FIRST_LAUNCH_PREMIUM] != false }.first()
    }

    fun setFirstLaunchPremiumComplete() {
        viewScope.launch {
            ds.edit { it[KEY_FIRST_LAUNCH_PREMIUM] = false }
        }
    }

    fun resetInterstitialCounter() {
        viewScope.launch {
            ds.edit { it[KEY_COUNTER_INTERSTITIAL] = 0 }
        }
    }

    fun incrementAdCounter() {
        viewScope.launch {
            ds.edit {
                val current = it[KEY_COUNTER_INTERSTITIAL] ?: 0
                it[KEY_COUNTER_INTERSTITIAL] = current + 1
            }
        }
    }

    fun getAdCounter(): Int = runBlocking(Dispatchers.IO) {
        ds.data.map { it[KEY_COUNTER_INTERSTITIAL] ?: 0 }.first()
    }

    fun getLastInterstitialShownAtMillis(): Long = runBlocking(Dispatchers.IO) {
        ds.data.map { it[KEY_LAST_INTERSTITIAL_SHOWN_AT] ?: 0L }.first()
    }

    fun setLastInterstitialShownNow() {
        val now = System.currentTimeMillis()
        viewScope.launch {
            ds.edit { it[KEY_LAST_INTERSTITIAL_SHOWN_AT] = now }
        }
    }

    fun isFirstHomeInterstitialClickPending(): Boolean = runBlocking(Dispatchers.IO) {
        ds.data.map { it[KEY_FIRST_HOME_INTERSTITIAL_PENDING] != false }.first()
    }

    fun markFirstHomeInterstitialClickConsumed() {
        viewScope.launch {
            ds.edit { it[KEY_FIRST_HOME_INTERSTITIAL_PENDING] = false }
        }
    }

    fun getGenerationClickCount(): Int = runBlocking(Dispatchers.IO) {
        ds.data.map { it[KEY_GENERATION_CLICK_COUNT] ?: 0 }.first()
    }

    fun incrementGenerationClickCount() {
        viewScope.launch {
            ds.edit {
                val current = it[KEY_GENERATION_CLICK_COUNT] ?: 0
                it[KEY_GENERATION_CLICK_COUNT] = current + 1
            }
        }
    }

    fun incrementGenerationClickCountAndGet(): Int = runBlocking(Dispatchers.IO) {
        var updated = 0
        ds.edit {
            val current = it[KEY_GENERATION_CLICK_COUNT] ?: 0
            updated = current + 1
            it[KEY_GENERATION_CLICK_COUNT] = updated
        }
        updated
    }

    fun setSelectedLanguageCode(languageCode: String) {
        viewScope.launch {
            ds.edit { it[KEY_SELECTED_LANGUAGE_CODE] = languageCode }
        }
    }

    fun getSelectedLanguageCode(): String = runBlocking(Dispatchers.IO) {
        ds.data.map { it[KEY_SELECTED_LANGUAGE_CODE] ?: DEFAULT_LANGUAGE_CODE }.first()
    }

    companion object {
        private val KEY_FIRST_LAUNCH = booleanPreferencesKey("first_launch")
        private val KEY_FIRST_LAUNCH_PREMIUM = booleanPreferencesKey("first_launch_premium")
        private val KEY_FIRST_LAUNCH_INTRO = booleanPreferencesKey("first_launch_intro")
        private val KEY_SELECTED_LANGUAGE_CODE = stringPreferencesKey("selected_language_code")
        private val KEY_COUNTER_INTERSTITIAL = intPreferencesKey("counter_interstitial")
        private val KEY_LAST_INTERSTITIAL_SHOWN_AT = longPreferencesKey("last_interstitial_shown_at")
        private val KEY_FIRST_HOME_INTERSTITIAL_PENDING = booleanPreferencesKey("first_home_interstitial_pending")
        private val KEY_GENERATION_CLICK_COUNT = intPreferencesKey("generation_click_count")

        private const val DEFAULT_LANGUAGE_CODE = "en"
    }
}
