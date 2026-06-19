package com.worldclock.app_themes.domain.states

import com.worldclock.app_themes.domain.model.SleepSound

sealed class SleepSoundResult {
    data class Success(
        val sounds: List<SleepSound>,
        val hasMore: Boolean = true
    ) : SleepSoundResult()

    data class Error(val message: String) : SleepSoundResult()
    object Loading : SleepSoundResult()
}