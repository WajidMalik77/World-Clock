package com.worldclock.app_themes.ads.helpers.usecases

import android.content.Context
import com.worldclock.app_themes.ads.helpers.models.AdEligibility

interface CheckAdEligibilityUseCase {
    suspend operator fun invoke(context: Context): AdEligibility
}