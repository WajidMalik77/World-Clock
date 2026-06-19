package com.worldclock.app_themes.core.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.worldclock.app_themes.R
import java.util.Calendar
import androidx.core.graphics.createBitmap

class ClockThemeWidgetProvider : AppWidgetProvider() {

    companion object {
        private const val TAG = "ClockWidget"
        const val ACTION_TICK = "com.worldclock.app_themes.CLOCK_WIDGET_TICK"

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
            val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
            val faceRes = prefs.getInt("selected_face_res", R.drawable.face_01_orange_ring)
            val hourRes = prefs.getInt("selected_hour_res", R.drawable.needle_hour_01)
            val minRes = prefs.getInt("selected_min_res", R.drawable.needle_min_01)

            val cal = Calendar.getInstance()
            val sec = cal.get(Calendar.SECOND)
            val min = cal.get(Calendar.MINUTE)
            val hour = cal.get(Calendar.HOUR)

            val hourAngle = hour * 30f + min * 0.5f
            val minuteAngle = min * 6f + sec * 0.1f

            Log.d(
                TAG,
                "Updating widget — time=$hour:$min:$sec hourAngle=$hourAngle minAngle=$minuteAngle"
            )

            val size = 400
            val bmp = createBitmap(size, size)
            val cv = Canvas(bmp)

            // 1. Draw face
            drawDrawable(context, faceRes, size, cv)

            // 2. Draw hour hand
            drawNeedle(context, hourRes, hourAngle, size, cv)

            // 3. Draw minute hand
            drawNeedle(context, minRes, minuteAngle, size, cv)

            // 4. Centre dot
            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FF3B00")
            }
            cv.drawCircle(size / 2f, size / 2f, size * 0.018f, dotPaint)

            val views = RemoteViews(context.packageName, R.layout.widget_clock_theme)
            views.setImageViewBitmap(R.id.widgetClockImage, bmp)
            appWidgetManager.updateAppWidget(widgetId, views)

            Log.d(TAG, "Widget updated successfully")
        }

        private fun drawDrawable(context: Context, resId: Int, size: Int, canvas: Canvas) {
            try {
                val d = ContextCompat.getDrawable(context, resId) ?: return
                val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                val c = Canvas(bmp)
                d.setBounds(0, 0, size, size)
                d.draw(c)
                canvas.drawBitmap(bmp, 0f, 0f, null)
            } catch (e: Exception) {
                Log.e(TAG, "drawDrawable failed: ${e.message}")
            }
        }

        private fun drawNeedle(
            context: Context,
            resId: Int,
            angleDeg: Float,
            size: Int,
            canvas: Canvas
        ) {
            try {
                val d = ContextCompat.getDrawable(context, resId) ?: return

                val iW = d.intrinsicWidth.takeIf { it > 0 } ?: size
                val iH = d.intrinsicHeight.takeIf { it > 0 } ?: size
                val scale = size.toFloat() / maxOf(iW, iH)
                val w = (iW * scale).toInt().coerceAtLeast(1)
                val h = (iH * scale).toInt().coerceAtLeast(1)

                val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val c = Canvas(bmp)
                d.setBounds(0, 0, w, h)
                d.draw(c)

                val matrix = Matrix()
                matrix.postRotate(angleDeg, w / 2f, h / 2f)
                val rotated = Bitmap.createBitmap(bmp, 0, 0, w, h, matrix, true)

                canvas.drawBitmap(
                    rotated,
                    (size - rotated.width) / 2f,
                    (size - rotated.height) / 2f,
                    null
                )
            } catch (e: Exception) {
                Log.e(TAG, "drawNeedle resId=$resId failed: ${e.message}")
            }
        }

        fun scheduleMinuteUpdates(context: Context) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pi = getTickIntent(context)
            am.setRepeating(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 60_000L,
                60_000L, pi
            )
            Log.d(TAG, "AlarmManager scheduled")
        }

        fun cancelUpdates(context: Context) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            am.cancel(getTickIntent(context))
            Log.d(TAG, "AlarmManager cancelled")
        }

        private fun getTickIntent(context: Context): PendingIntent {
            val intent = Intent(context, ClockThemeWidgetProvider::class.java).apply {
                action = ACTION_TICK
            }
            return PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate — ${appWidgetIds.size} widgets")
        appWidgetIds.forEach { updateWidget(context, appWidgetManager, it) }
        scheduleMinuteUpdates(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        scheduleMinuteUpdates(context)
        Log.d(TAG, "onEnabled")
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelUpdates(context)
        Log.d(TAG, "onDisabled")
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TICK) {
            Log.d(TAG, "ACTION_TICK received — redrawing all widgets")
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(
                    context,
                    ClockThemeWidgetProvider::class.java
                )
            )
            ids.forEach { updateWidget(context, manager, it) }
        }
    }
}