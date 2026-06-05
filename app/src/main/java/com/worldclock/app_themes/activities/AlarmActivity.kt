package com.worldclock.app_themes.activities

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.worldclock.app_themes.R
import com.worldclock.app_themes.adapter.AlarmAdapter
import com.worldclock.app_themes.database.AlarmDao
import com.worldclock.app_themes.database.AlarmEntity
import com.worldclock.app_themes.database.WorldClockDatabase
import com.worldclock.app_themes.databinding.ActivityAlarmBinding
import com.worldclock.app_themes.utils.GradientTextHelper
import com.worldclock.app_themes.utils.cancelAlarm
import com.worldclock.app_themes.utils.getCurrentWorldClock
import com.worldclock.app_themes.utils.getNextAlarmDateTime
import com.worldclock.app_themes.utils.getTimeUntilAlarm
import com.worldclock.app_themes.utils.isNotificationPermissionGranted
import com.worldclock.app_themes.utils.requestNotificationPermissionIfNeeded
import com.worldclock.app_themes.utils.setAlarm
import com.worldclock.app_themes.utils.showNotificationPermissionSettings
import com.worldclock.app_themes.utils.updateTimes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Calendar

class AlarmActivity : BaseActivity() {
    private val binding by lazy {
        ActivityAlarmBinding.inflate(layoutInflater)
    }
    private lateinit var adapter: AlarmAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)
        binding.toolbar.back.setOnClickListener { finish() }
        binding.toolbar.title.text = getString(R.string.alarm_clock)

        GradientTextHelper.apply(
            binding.emptyName,
            Color.parseColor("#7441D0"),
            Color.parseColor("#EF4A9A"),
            direction = GradientTextHelper.Direction.LEFT_RIGHT
        )

        GradientTextHelper.apply(
            binding.timeTv,
            Color.parseColor("#7441D0"),
            Color.parseColor("#EF4A9A"),
            direction = GradientTextHelper.Direction.LEFT_RIGHT
        )

        checkOverlayPermission()
        val dao = WorldClockDatabase.getDatabase(this@AlarmActivity).alarmDao()

        adapter = AlarmAdapter(
            callbacks = { clock ->
                lifecycleScope.launch {
                    dao.updateAlarm(clock)
                }
                if (clock.isEnabled) {
                    setAlarm(this, clock)
                } else {
                    cancelAlarm(this, clock)
                }

            },
            onLongClick = { clock ->

                showDeleteDialog(clock, dao)
            },
            onEditClick = {
                startActivity(
                    Intent(this, AddAlarmActivity::class.java)
                        .putExtra("alarmId", it.id)
                )

            }
        )
        dao.getAllAlarms().observe(this@AlarmActivity) { alarms ->
            if (alarms.isNotEmpty()) {
                binding.empty.visibility = View.GONE
                binding.notEmpty.visibility = View.VISIBLE
                adapter.updateList(alarms)
                binding.recycler.adapter = adapter

                // ✅ Find the next enabled alarm (and the soonest one)
                val nextAlarm = alarms
                    .filter { it.isEnabled }
                    .minByOrNull { alarm ->
                        val diffMinutes = ChronoUnit.MINUTES.between(
                            LocalDateTime.now(),
                            getNextAlarmDateTime(alarm.hour, alarm.minute, alarm.repeatDays)
                        )
                        if (diffMinutes < 0) Long.MAX_VALUE else diffMinutes
                    }

                if (nextAlarm != null) {
                    binding.dateTv.text = getCurrentWorldClock().date
                    binding.timeTv.text =
                        getTimeUntilAlarm(nextAlarm.hour, nextAlarm.minute, nextAlarm.repeatDays)
                } else {
                    binding.timeTv.text = getString(R.string.no_active_alarms)
                }

            } else {
                binding.empty.visibility = View.VISIBLE
                binding.notEmpty.visibility = View.GONE
            }
        }

        binding.addClock.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                if (!isNotificationPermissionGranted()) {

                    requestPermissions(
                        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                        com.worldclock.app_themes.utils.NOTIFICATION_PERMISSION_CODE
                    )
                } else
                    startActivity(Intent(this, AddAlarmActivity::class.java))
            } else
                startActivity(Intent(this, AddAlarmActivity::class.java))

//            val hour = 7
//            val minute = 30
//            val label = "Morning Alarm"
//            AlarmScheduler.scheduleAlarm(this, hour, minute, label)
//            Toast.makeText(this, "Alarm set for $hour:$minute", Toast.LENGTH_SHORT).show()
        }

    }

    private val NOTIFICATION_PERMISSION_CODE = 101

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                MyApplication.isResume = false

                Log.d("Permission", "Notification permission GRANTED ✅")
            } else {
                Log.d("Permission", "Notification permission DENIED ❌")
                MyApplication.isResume = false

                // Optional: guide user to settings
                showNotificationPermissionSettings()
            }
        }
    }

    fun checkOverlayPermission() {

        // Exact alarm permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                return
            }
        }

        // Notification permission (Android 13+)
        requestNotificationPermissionIfNeeded()

        // Overlay permission
        /*        if (!Settings.canDrawOverlays(this)) {

                    AlertDialog.Builder(this)
                        .setTitle(getString(R.string.permission))
                        .setMessage(getString(R.string.overlay_permission_is_needed_to_work_alarm_properly_on_android_10_please_grant_it))
                        .setPositiveButton(getString(R.string.allow)) { _, _ ->
                            startActivity(
                                Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:$packageName")
                                )
                            )
                        }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show()
                }*/
    }


//    fun checkOverlayPermission() {
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            val alarmManager = getSystemService(AlarmManager::class.java)
//            val canSchedule = alarmManager.canScheduleExactAlarms()
//            if (!canSchedule) {
//                // Ask user to allow exact alarms
//                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
//                startActivity(intent)
//                Log.d(
//                    "AlarmScheduler",
//                    "Exact alarm permission not granted; requesting user to allow it."
//                )
//                return
//            }
//        }
//
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
//            }
//        }
//
//        if (!Settings.canDrawOverlays(this)) {
//            AlertDialog.Builder(this)
//                .setTitle(getString(R.string.permission))
//                .setMessage(getString(R.string.overlay_permission_is_needed_to_work_alarm_properly_on_android_10_please_grant_it)) // Specifying a listener allows you to take an action before dismissing the dialog.
//                // The dialog is automatically dismissed when a dialog button is clicked.
//                .setPositiveButton(
//                    getString(R.string.allow),
//                    DialogInterface.OnClickListener { dialog, which ->
//                        val myIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
//                        startActivity(myIntent)
//                    })
//                .setNegativeButton(getString(R.string.cancel), null)
//                .setIcon(android.R.drawable.ic_dialog_alert)
//                .show()
//
//        }
//    }

    private fun showDeleteDialog(alarm: AlarmEntity, dao: AlarmDao) {
        AlertDialog.Builder(this)
            .setTitle("Delete Alarm")
            .setMessage("Are you sure you want to delete this alarm?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    cancelAlarm(this@AlarmActivity, alarm)
                    dao.deleteAlarm(alarm)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


}