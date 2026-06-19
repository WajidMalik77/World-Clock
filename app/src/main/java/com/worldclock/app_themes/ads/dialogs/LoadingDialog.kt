package com.worldclock.app_themes.ads.dialogs

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.view.ContextThemeWrapper
import android.view.Window
import androidx.core.graphics.drawable.toDrawable
import com.worldclock.app_themes.R

class LoadingDialog(private val activity: Activity) {

    private var dialog: Dialog =
        Dialog(ContextThemeWrapper(activity, R.style.Theme_WorldClock)).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.loading_ad_dialog)

            // Dim the background a bit for focus
            window?.apply {
                setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                setDimAmount(0.45f)
            }

            setCancelable(false)
        }

    fun show() {
        if (activity.isFinishing || activity.isDestroyed) return

        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    fun dismiss() {
        if (dialog.isShowing) {
            try {
                dialog.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
