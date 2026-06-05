package com.worldclock.app_themes.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "world_clocks")
data class WorldClockItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val city: String,
    val country: String,
    val flag: String,
    val currentTime: String,
    val timeZoneId: String,
    val relation: String,   // "Ahead by 3.0h", "Behind by 2.5h", "Same Time"
    val diffHours: Double,   // numeric difference (positive = ahead, negative = behind)
    var isSelected: Boolean = false
)

@Entity(tableName = "widget_clocks")
data class WidgetClockItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val city: String,
    val country: String,
    val flag: String,
    val currentTime: String,
    val timeZoneId: String,
    val relation: String,   // "Ahead by 3.0h", "Behind by 2.5h", "Same Time"
    val diffHours: Double,   // numeric difference (positive = ahead, negative = behind)
    var isSelected: Boolean = false
)
