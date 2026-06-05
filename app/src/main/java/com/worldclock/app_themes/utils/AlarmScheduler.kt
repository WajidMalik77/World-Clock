package com.worldclock.app_themes.utils


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import java.util.Calendar

object AlarmScheduler {

    fun scheduleAlarm(context: Context, hour: Int, minute: Int, label: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("label", label)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            label.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DATE, 1)
        }

        try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
                        )
                    } else {
                        val permIntent = Intent(
                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                        )
                        permIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(permIntent)
                    }
                }

                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
                    )
                }

                else -> {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
                    )
                }
            }
        } catch (e: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    fun cancelAlarm(context: Context, label: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, label.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
