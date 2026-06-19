package com.worldclock.app_themes.core.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.worldclock.app_themes.core.utils.AdsConstants.PrefsName
import com.worldclock.app_themes.core.utils.AdsConstants.isFirstTime
import java.util.Locale

object AppEventLogger {
    const val EVENT_BUTTON_CLICK = "button_click"
    const val EVENT_SCREEN_VIEW = "screen_view"
    const val EVENT_FUNNEL_STEP = "funnel_step"

    const val PARAM_SCREEN_NAME = "screen_name"
    const val PARAM_BUTTON_ID = "button_id"
    const val PARAM_ACTION = "action"
    const val PARAM_FLOW = "flow"
    const val PARAM_SOURCE = "source"
    const val PARAM_FUNNEL_NAME = "funnel_name"
    const val PARAM_STEP_NAME = "step_name"
    const val PARAM_STATUS = "status"
    const val PARAM_USER_SEGMENT = "user_segment"

    const val USER_SEGMENT_NEW = "new_user"
    const val USER_SEGMENT_OLD = "old_user"

    private const val UNKNOWN = "unknown"
    private const val SCREEN_DEDUPE_WINDOW_MS = 500L

    @Volatile
    private var lastScreenName: String? = null

    @Volatile
    private var lastScreenLoggedAtMs: Long = 0L

    fun trackScreenCreate(
        context: Context,
        savedInstanceState: Bundle?,
        screenName: String,
        flow: String,
        legacyEntryEventName: String? = null
    ) {
        if (savedInstanceState != null) return
        trackScreenView(screenName, flow, "lifecycle")
        trackExplorationEvent(
            userSegment = resolveUserSegment(context),
            screen = screenName,
            step = "on_create",
            source = "lifecycle"
        )
        legacyEntryEventName?.let(::trackDirectEvent)
    }

    fun trackScreenDestroy(
        context: Context,
        screenName: String,
        source: String = "lifecycle"
    ) {
        trackExplorationEvent(
            userSegment = resolveUserSegment(context),
            screen = screenName,
            step = "on_destroy",
            source = source
        )
    }

    fun trackButtonClick(
        screenName: String,
        buttonId: String,
        action: String? = null,
        flow: String? = null,
        source: String? = null
    ) {
        val normalizedButton = normalizeValue(buttonId)
        if (normalizedButton == UNKNOWN) return

        logEvent(
            eventName = EVENT_BUTTON_CLICK,
            params = listOf(
                PARAM_SCREEN_NAME to normalizeValue(screenName),
                PARAM_BUTTON_ID to normalizedButton,
                PARAM_ACTION to normalizeOptional(action),
                PARAM_FLOW to normalizeOptional(flow),
                PARAM_SOURCE to normalizeOptional(source)
            )
        )
    }

    fun trackScreenView(
        screenName: String,
        flow: String? = null,
        source: String? = null
    ) {
        val normalizedScreen = normalizeValue(screenName)
        val now = System.currentTimeMillis()
        if (lastScreenName == normalizedScreen && now - lastScreenLoggedAtMs < SCREEN_DEDUPE_WINDOW_MS) {
            return
        }
        lastScreenName = normalizedScreen
        lastScreenLoggedAtMs = now

        logEvent(
            eventName = EVENT_SCREEN_VIEW,
            params = listOf(
                PARAM_SCREEN_NAME to normalizedScreen,
                PARAM_FLOW to normalizeOptional(flow),
                PARAM_SOURCE to normalizeOptional(source)
            )
        )
    }

    fun trackFunnelStep(
        funnelName: String,
        stepName: String,
        status: String = "hit",
        screenName: String? = null,
        source: String? = null
    ) {
        val normalizedFunnel = normalizeValue(funnelName)
        val normalizedStep = normalizeValue(stepName)
        if (normalizedFunnel == UNKNOWN || normalizedStep == UNKNOWN) return

        logEvent(
            eventName = EVENT_FUNNEL_STEP,
            params = listOf(
                PARAM_FUNNEL_NAME to normalizedFunnel,
                PARAM_STEP_NAME to normalizedStep,
                PARAM_STATUS to normalizeOptional(status),
                PARAM_SCREEN_NAME to normalizeOptional(screenName),
                PARAM_SOURCE to normalizeOptional(source)
            )
        )
    }

    fun trackExplorationEvent(
        userSegment: String,
        screen: String,
        step: String,
        source: String? = null
    ) {
        val normalizedUserSegment = normalizeValue(userSegment)
        val normalizedScreen = normalizeValue(screen)
        val normalizedStep = normalizeValue(step)
        if (normalizedUserSegment == UNKNOWN || normalizedScreen == UNKNOWN || normalizedStep == UNKNOWN) return

        val eventName = normalizeValue("${normalizedUserSegment}_${normalizedScreen}_$normalizedStep")
        logEvent(
            eventName = eventName,
            params = listOf(
                PARAM_USER_SEGMENT to normalizedUserSegment,
                PARAM_SCREEN_NAME to normalizedScreen,
                PARAM_STEP_NAME to normalizedStep,
                PARAM_SOURCE to normalizeOptional(source)
            )
        )
    }

    fun trackDirectEvent(eventName: String, params: List<Pair<String, String?>> = emptyList()) {
        val normalizedEvent = normalizeValue(eventName)
        if (normalizedEvent == UNKNOWN) return
        logEvent(normalizedEvent, params)
    }

    fun resolveUserSegment(context: Context): String {
        val isReturningUser = context.getSharedPreferences(PrefsName, Context.MODE_PRIVATE)
            .getBoolean(isFirstTime, false)
        return if (isReturningUser) USER_SEGMENT_OLD else USER_SEGMENT_NEW
    }

    private fun logEvent(
        eventName: String,
        params: List<Pair<String, String?>>
    ) {
        runCatching {
            Firebase.analytics.logEvent(eventName) {
                params.forEach { (key, value) ->
                    val normalizedValue = value?.let(::normalizeValue)
                    if (!normalizedValue.isNullOrBlank() && normalizedValue != UNKNOWN) {
                        param(key, normalizedValue)
                    }
                }
            }
        }
    }

    private fun normalizeOptional(value: String?): String? {
        if (value.isNullOrBlank()) return null
        return normalizeValue(value)
    }

    private fun normalizeValue(raw: String): String {
        val snake = raw.trim()
            .replace(Regex("([a-z0-9])([A-Z])"), "$1_$2")
            .replace(Regex("[^A-Za-z0-9]+"), "_")
            .trim('_')
            .lowercase(Locale.US)
        return snake.ifBlank { UNKNOWN }.take(100)
    }
}
