package com.worldclock.app_themes.core.utils

import android.app.Dialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import com.worldclock.app_themes.R
import com.worldclock.app_themes.databinding.DialogAddWidgetBinding
import java.util.Calendar

class AddWidgetDialog(
    private val context: Context,
    private val widget: ClockWidget
) {

    companion object {
        private const val TAG = "AddWidgetDialog"
    }

    fun show() {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        val binding = DialogAddWidgetBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)

        dialog.window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Render full clock preview with needles
        val previewSize = context.resources.displayMetrics.widthPixels / 2
        binding.ivClockPreview.setImageBitmap(renderClockPreview(previewSize))

        binding.btnCancel.setOnClickListener { dialog.dismiss() }

        binding.btnAddAutomatically.setOnClickListener {
            dialog.dismiss()
            saveThemeAndAddWidget()
        }

        dialog.show()
    }

    private fun renderClockPreview(size: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val cal = Calendar.getInstance()
        val sec = cal.get(Calendar.SECOND)
        val min = cal.get(Calendar.MINUTE)
        val hour = cal.get(Calendar.HOUR)

        when (widget) {
            is ClockWidget.Analog -> {
                val view = ClockCanvasView(
                    context = context,
                    startDegHr = hour * 30f + min * 0.5f,
                    startDegMin = min * 6f + sec * 0.1f,
                    startDegSec = sec * 6f,
                    clockImage = widget.faceRes,
                    hourImage = widget.hourRes,
                    minImage = widget.minuteRes,
                    secImage = widget.secRes
                )
                view.measure(
                    View.MeasureSpec.makeMeasureSpec(
                        size,
                        View.MeasureSpec.EXACTLY
                    ),
                    View.MeasureSpec.makeMeasureSpec(
                        size,
                        View.MeasureSpec.EXACTLY
                    )
                )
                view.layout(0, 0, size, size)
                view.draw(canvas)
            }

            is ClockWidget.Digital -> {
                ContextCompat.getDrawable(context, widget.backgroundRes)?.apply {
                    setBounds(0, 0, size, size); draw(canvas)
                }
            }
        }
        return bmp
    }

    private fun saveThemeAndAddWidget() {
        Log.d(TAG, "saveThemeAndAddWidget() called")

        // Save all resource IDs
        context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE).edit().apply {
            when (widget) {
                is ClockWidget.Analog -> {
                    putInt("selected_face_res", widget.faceRes)
                    putInt("selected_hour_res", widget.hourRes)
                    putInt("selected_min_res", widget.minuteRes)
                    putInt("selected_sec_res", widget.secRes ?: R.drawable.needle_sec_01)
                    putBoolean("is_digital", false)
                }

                is ClockWidget.Digital -> {
                    putInt("selected_face_res", widget.backgroundRes)
                    putBoolean("is_digital", true)
                }
            }
            apply()
        }
        Log.d(TAG, "SharedPreferences saved")

        // Check Android version
        Log.d(TAG, "Android SDK = ${Build.VERSION.SDK_INT}, O = ${Build.VERSION_CODES.O}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val provider = ComponentName(context, ClockThemeWidgetProvider::class.java)

            Log.d(TAG, "Provider = ${provider.className}")

            val isSupported = appWidgetManager.isRequestPinAppWidgetSupported
            Log.d(TAG, "isRequestPinAppWidgetSupported = $isSupported")

            if (isSupported) {
                try {
                    val result = appWidgetManager.requestPinAppWidget(provider, null, null)
                    Log.d(TAG, "requestPinAppWidget result = $result")
                    if (result) {
                        Toast.makeText(context, "Check your home screen!", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(
                            context,
                            "Pin request failed. Try: Long press home screen → Widgets",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "requestPinAppWidget exception: ${e.message}", e)
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                Log.d(TAG, "Pin not supported by launcher")
                Toast.makeText(
                    context,
                    "Your launcher doesn't support auto-add.\nLong press home screen → Widgets → find this app",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Log.d(TAG, "Android < O, manual only")
            Toast.makeText(
                context,
                "Long press home screen → Widgets → find this app",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}