package com.worldclock.app_themes.core.utils


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.worldclock.app_themes.data.database.WorldClockDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {

            val db = WorldClockDatabase.getDatabase(context)

            CoroutineScope(Dispatchers.IO).launch {
                db.alarmDao().getAllAlarmsSync()
                    .filter { it.isEnabled }
                    .forEach { setAlarm(context, it) }
                db.reminderDao().getAllRemindersSync()
                    .filter { it.isEnabled }
                    .forEach { setReminder(context, it) }
            }
        }
    }
}
