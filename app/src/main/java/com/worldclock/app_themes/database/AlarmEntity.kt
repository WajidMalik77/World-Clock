package com.worldclock.app_themes.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val label: String = "",
    val hour: Int,
    val minute: Int,
    var isEnabled: Boolean = true,
    val repeatDays: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val vibrate: Boolean,
    val alarmSound: String = "Radar (Default)",
    val snoozeMinutes: Int = 5

)
