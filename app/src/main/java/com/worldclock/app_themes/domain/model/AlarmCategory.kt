package com.worldclock.app_themes.domain.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

data class AlarmCategory(
    val id: Int,
    val title: String,
    val reminderCount: Int = 0,
    @DrawableRes val imageRes: Int,
    @ColorRes val bgColorRes: Int
) {
    val subtitle: String get() = "$reminderCount Reminder${if (reminderCount != 1) "s" else ""}"
}