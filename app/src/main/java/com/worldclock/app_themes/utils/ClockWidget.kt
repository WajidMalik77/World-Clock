package com.worldclock.app_themes.utils

import androidx.annotation.DrawableRes

sealed class ClockWidget {

    data class Analog(
        @DrawableRes val faceRes: Int,
        @DrawableRes val hourRes: Int,
        @DrawableRes val minuteRes: Int,
        @DrawableRes val secRes: Int? = null
    ) : ClockWidget()

    data class Digital(
        @DrawableRes val backgroundRes: Int,
        val timeColor: Int,
        val label: String? = null
    ) : ClockWidget()
}