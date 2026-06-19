package com.worldclock.app_themes.ads.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

object DebugToaster {

    fun showAdDebugCard(context: Context?, prefFlag: Boolean, message: String) {
        if (!prefFlag || context == null) return

        Handler(Looper.getMainLooper()).post {
            try {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
            }
        }
    }
}
