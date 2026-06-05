package com.worldclock.app_themes.utils

import com.worldclock.app_themes.R
import com.worldclock.app_themes.model.AlarmCategory

fun getAlarmCategories(): List<AlarmCategory> = listOf(
    AlarmCategory(0, "Medicine", 0, R.drawable.ic_medicine, R.color.cat_green),
    AlarmCategory(1, "Workout", 0, R.drawable.ic_workout, R.color.cat_peach),
    AlarmCategory(2, "Important", 0, R.drawable.ic_important, R.color.cat_blue),
    AlarmCategory(3, "Date", 0, R.drawable.ic_date, R.color.cat_pink),
    AlarmCategory(4, "Meal", 0, R.drawable.ic_meal, R.color.cat_light_green),
    AlarmCategory(5, "Birthday", 0, R.drawable.ic_birthday, R.color.cat_yellow),
    AlarmCategory(6, "Event", 0, R.drawable.ic_event, R.color.cat_grey_light),
    AlarmCategory(7, "Custom", 0, R.drawable.ic_custom, R.color.cat_grey),
)