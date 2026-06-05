package com.worldclock.app_themes.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "",
    val name: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val hour: Int = 12,
    val minute: Int = 0,
    val isAm: Boolean = true,
    val sound: String = "Default",
    val soundUri: String = "",
    val vibration: String = "None",
    val snooze: String = "5min",
    val repeatDays: String = "",
    val isEnabled: Boolean = true,
    val categoryId: Int = -1,
    val categoryTitle: String = ""
)
