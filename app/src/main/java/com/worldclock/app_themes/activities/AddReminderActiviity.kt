package com.worldclock.app_themes.activities

import android.graphics.Color
import android.media.RingtoneManager
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.worldclock.app_themes.R
import com.worldclock.app_themes.database.ReminderDao
import com.worldclock.app_themes.database.ReminderEntity
import com.worldclock.app_themes.database.WorldClockDatabase
import com.worldclock.app_themes.databinding.ActivityAddReminderActiviityBinding
import com.worldclock.app_themes.dialogs.DatePickerDialog
import com.worldclock.app_themes.dialogs.ListPickerDialog
import com.worldclock.app_themes.dialogs.TimePickerDialog
import com.worldclock.app_themes.utils.cancelReminder
import com.worldclock.app_themes.utils.setReminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class AddReminderActiviity : BaseActivity() {

    private val binding by lazy { ActivityAddReminderActiviityBinding.inflate(layoutInflater) }
    private lateinit var dao: ReminderDao

    private var selectedHour = 12
    private var selectedMinute = 0
    private var isAm = true
    private var selectedSound = "Default"
    private var selectedVibration = "None"
    private var selectedSnooze = "5min"
    private var startDate = ""
    private var endDate = ""
    private val selectedDays = mutableSetOf<Int>()

    private var categoryId = -1
    private var categoryTitle = ""
    private var editingReminderId = -1

    private var selectedSoundUri = ""
    private val soundTitles = mutableListOf<String>()
    private val soundUris = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)

        dao = WorldClockDatabase.getDatabase(this).reminderDao()
        categoryId = intent.getIntExtra("category_id", -1)
        categoryTitle = intent.getStringExtra("category_title") ?: ""
        editingReminderId = intent.getIntExtra("reminder_id", -1)

        binding.toolbar.back.setOnClickListener { finish() }
        binding.toolbar.title.text = getString(R.string.all_reminders)

        loadDeviceSounds()
        setupRepeatDays()
        setupClickListeners()

        if (editingReminderId != -1) loadReminderData(editingReminderId)
    }

    private fun setupClickListeners() {
        binding.cardAlarmTime.setOnClickListener {
            TimePickerDialog { hour, minute, am ->
                selectedHour = hour
                selectedMinute = minute
                isAm = am
                val amPm = getString(if (am) R.string.label_am else R.string.label_pm)
                binding.tvAlarmTime.text = getString(R.string.format_time, hour, minute, amPm)
            }.show(supportFragmentManager, "time")
        }

        binding.cardSchedule.setOnClickListener {
            DatePickerDialog { start, end ->
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                startDate = sdf.format(start.time)
                endDate = sdf.format(end.time)
                binding.tvStartDate.text = "Start Date: $startDate"
                binding.tvEndDate.text = "End Date: $endDate"
            }.show(supportFragmentManager, "date")
        }

        binding.cardSound.setOnClickListener {
            if (soundTitles.isEmpty()) {
                Toast.makeText(this, "No sounds available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val currentIdx = soundUris.indexOf(selectedSoundUri).coerceAtLeast(0)
            ListPickerDialog("Select Sound", soundTitles, currentIdx) { idx, title ->
                selectedSound = title
                selectedSoundUri = soundUris[idx]
                binding.tvSound.text = title
            }.show(supportFragmentManager, "sound")
        }

        binding.cardVibration.setOnClickListener {
            val vibs = listOf("None", "Continuous", "Short Beat", "Long Beat")
            ListPickerDialog(
                "Alarm Vibration", vibs,
                vibs.indexOf(selectedVibration).coerceAtLeast(0)
            ) { _, v ->
                selectedVibration = v
                binding.tvVibration.text = v
            }.show(supportFragmentManager, "vibration")
        }

        binding.cardSnoozeTime.setOnClickListener {
            val snooze = listOf("5min", "10min", "20min", "30min")
            ListPickerDialog(
                "Snooze Time", snooze,
                snooze.indexOf(selectedSnooze).coerceAtLeast(0)
            ) { _, v ->
                selectedSnooze = v
                binding.tvSnoozeTime.text = v
            }.show(supportFragmentManager, "snooze")
        }

        binding.btnAddReminder.setOnClickListener { saveReminder() }
    }

    private fun loadDeviceSounds() {
        try {
            val rm = RingtoneManager(this).apply { setType(RingtoneManager.TYPE_ALARM) }
            val cursor = rm.cursor
            soundTitles.clear()
            soundUris.clear()

            // Default first
            soundTitles.add("Default")
            soundUris.add(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString())

            while (cursor.moveToNext()) {
                soundTitles.add(cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX))
                soundUris.add(rm.getRingtoneUri(cursor.position).toString())
            }
            cursor.close()

            // Set default selection
            selectedSoundUri = soundUris.first()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadReminderData(id: Int) {
        lifecycleScope.launch {
            val reminder = dao.getReminderById(id) ?: return@launch

            binding.etReminderTitle.setText(reminder.title)
            binding.etReminderName.setText(reminder.name)
            binding.tvStartDate.text = "Start Date: ${reminder.startDate}"
            binding.tvEndDate.text = "End Date: ${reminder.endDate}"
            binding.tvSound.text = reminder.sound
            binding.tvVibration.text = reminder.vibration
            binding.tvSnoozeTime.text = reminder.snooze
            binding.tvAlarmTime.text = String.format(
                Locale.getDefault(),
                "%02d:%02d %s",
                reminder.hour,
                reminder.minute,
                if (reminder.isAm) "AM" else "PM"
            )

            // Restore local vars
            selectedHour = reminder.hour
            selectedMinute = reminder.minute
            isAm = reminder.isAm
            selectedSound = reminder.sound
            selectedSoundUri = reminder.soundUri
            selectedVibration = reminder.vibration
            selectedSnooze = reminder.snooze
            startDate = reminder.startDate
            endDate = reminder.endDate

            // Restore day chips
            selectedDays.clear()
            reminder.repeatDays
                .split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .forEach { selectedDays.add(it) }

            refreshDayChips()
        }
    }

    private fun refreshDayChips() {
        val dayViews = listOf(
            binding.tvMon, binding.tvTue, binding.tvWed,
            binding.tvThu, binding.tvFri, binding.tvSat, binding.tvSun
        )
        dayViews.forEachIndexed { index, tv ->
            if (selectedDays.contains(index)) {
                tv.setBackgroundResource(R.drawable.bg_day_selected)
                tv.setTextColor(Color.WHITE)
            } else {
                tv.setBackgroundResource(R.drawable.bg_day_unselected)
                tv.setTextColor(getColor(R.color.black))
            }
        }
    }

    private fun saveReminder() {
        val title = binding.etReminderTitle.text.toString().trim()
        val name = binding.etReminderName.text.toString().trim()

        if (title.isEmpty()) {
            binding.etReminderTitle.error = "Enter a title"
            return
        }

        val reminder = ReminderEntity(
            id = if (editingReminderId != -1) editingReminderId else 0,
            title = title,
            name = name,
            startDate = startDate,
            endDate = endDate,
            hour = selectedHour,
            minute = selectedMinute,
            isAm = isAm,
            sound = selectedSound,
            soundUri = selectedSoundUri,
            vibration = selectedVibration,
            snooze = selectedSnooze,
            repeatDays = selectedDays.sorted().joinToString(","),
            categoryId = categoryId,
            categoryTitle = categoryTitle
        )

        CoroutineScope(Dispatchers.IO).launch {
            if (editingReminderId != -1) {
                cancelReminder(this@AddReminderActiviity, reminder)
                dao.updateReminder(reminder)
            } else {
                dao.insertReminder(reminder)
            }

            if (reminder.isEnabled) {
                setReminder(this@AddReminderActiviity, reminder)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@AddReminderActiviity,
                    if (editingReminderId != -1) "Reminder updated!" else "Reminder saved!",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun setupRepeatDays() {
        val dayViews = listOf(
            binding.tvMon, binding.tvTue, binding.tvWed,
            binding.tvThu, binding.tvFri, binding.tvSat, binding.tvSun
        )
        dayViews.forEachIndexed { index, tv ->
            tv.setOnClickListener {
                if (selectedDays.contains(index)) {
                    selectedDays.remove(index)
                    tv.setBackgroundResource(R.drawable.bg_day_unselected)
                    tv.setTextColor(getColor(R.color.black))
                } else {
                    selectedDays.add(index)
                    tv.setBackgroundResource(R.drawable.bg_day_selected)
                    tv.setTextColor(Color.WHITE)
                }
            }
        }
    }
}