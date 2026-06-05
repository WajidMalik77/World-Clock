package com.worldclock.app_themes.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.worldclock.app_themes.R
import com.zeugmasolutions.localehelper.Locales
import java.util.Locale
import androidx.core.net.toUri
import com.worldclock.app_themes.activities.MyApplication
import com.worldclock.app_themes.database.AlarmEntity
import com.worldclock.app_themes.database.WidgetClockItem
import com.worldclock.app_themes.database.WorldClockItem
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

fun ImageView.loadDrawableImage(@DrawableRes resId: Int) {
    Glide.with(this.context)
        .load(resId)
        .into(this)
}

fun ImageView.loadGifFromRaw(@RawRes resId: Int) {
    val uri = "android.resource://${context.packageName}/$resId".toUri()
    Glide.with(this.context)
        .asGif()
        .load(uri)
        .into(this)
}

data class Lang(val res: Int, val name: String, val locale: Locale)

data class HomeItem(val res: Int, val name: String, val bgColor: Int)

fun Context.getLangData() = arrayListOf(
    Lang(R.drawable.flag_us, "English", Locales.English),
    Lang(R.drawable.flag_hi, "Hindi", Locales.Hindi),
    Lang(R.drawable.flag_es, "Spanish", Locales.Spanish),
    Lang(R.drawable.flag_fr, "French", Locales.French),
    Lang(R.drawable.flag_pt, "Portuguese", Locales.Portuguese),
    Lang(R.drawable.flag_germany, "German", Locales.German),
    Lang(R.drawable.flag_ja, "Japanese", Locales.Japanese),
    Lang(R.drawable.flag_ar, "Arabic", Locales.Arabic),
)

fun Context.getMenuData() = arrayListOf(
    Lang(R.drawable.language, getString(R.string.language), Locales.English),
    Lang(R.drawable.rate_us, getString(R.string.rate_us), Locales.French),
    Lang(R.drawable.feedback_icon, getString(R.string.feedback), Locales.Portuguese),
    Lang(R.drawable.share_app, getString(R.string.share_app), Locales.Spanish),
//    Lang(R.drawable.more_app, getString(R.string.more_apps), Locales.French),
    Lang(R.drawable.feedback, getString(R.string.terms_amp_conditions), Locales.German),
    Lang(R.drawable.privacy_policy, getString(R.string.privacy_policy), Locales.Japanese),
)


// ⭐ Rate App
fun rateApp(context: Context) {
    val packageName = context.packageName
    try {
        val uri = "market://details?id=$packageName".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // Open in browser if Play Store not found
        val uri = "https://play.google.com/store/apps/details?id=$packageName".toUri()
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}

// 🗣️ More Apps by Developer
fun moreApps(context: Context) {
    val uri = "https://play.google.com/store/apps/".toUri()
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

fun shareApp(context: Context) {
    val packageName = context.packageName
    val shareText =
        "Check out this amazing app:\nhttps://play.google.com/store/apps/details?id=$packageName"

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Share App")
        putExtra(Intent.EXTRA_TEXT, shareText)
    }

    context.startActivity(Intent.createChooser(intent, "Share via"))
}


// 📝 Send Feedback Email
fun sendFeedback(context: Context, email: String = "style.uk77@gmail.com") {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri()
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, "App Feedback: ")
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Send Feedback"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// 🔒 Open Privacy Policy Link
fun Context.openPrivacyPolicy(url: String) {
    if (url.isNotBlank()) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No browser found to open link", Toast.LENGTH_SHORT).show()
        }
    } else {
        Toast.makeText(this, "Privacy policy link not available", Toast.LENGTH_SHORT).show()
    }
}


fun Context.getHomeData() = arrayListOf(
    HomeItem(R.drawable.m1, getString(R.string.clock), R.color.item_purple),
    HomeItem(R.drawable.m2, getString(R.string.alarm), R.color.item_green),
    HomeItem(R.drawable.m3, getString(R.string.stopwatch), R.color.item_pink),
    HomeItem(R.drawable.m4, getString(R.string.timer), R.color.item_orange),
    HomeItem(R.drawable.m5, getString(R.string.compass), R.color.item_rose),
    HomeItem(R.drawable.m6, getString(R.string.widgets), R.color.item_blue),
    HomeItem(R.drawable.m7, getString(R.string.voice_reminder), R.color.item_lavender),
    HomeItem(R.drawable.m8, getString(R.string.sleep_sound), R.color.item_light_blue),

    )

data class OnboardingItem(
    val type: Int,
    val title: String? = null,
    val description: String? = null,
    val imageRes: Int? = null,
    val dotRes: Int? = null
)


const val TYPE_DATA = 0
const val TYPE_AD = 1
fun Activity.openLink(url: String = "https://payments.google.com/payments/apis-secure/u/0/get_legal_document?ldl=en_GB&ldo=0&ldt=buyertos") {
    try {


        val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(browserIntent)
    } catch (e: Exception) {
        // Catch Exception here
    }
}


//data class WorldClockItem(
//    val city: String,
//    val country: String,
//    val flag: String,
//    val currentTime: String,
//    val timeZoneId: String
//)


fun getFlagEmoji(countryCode: String?): String {
    if (countryCode.isNullOrEmpty() || countryCode.length != 2) return "🌐"
    return try {
        val codePoints = countryCode.uppercase().map { it.code + 127397 }
        String(codePoints.toIntArray(), 0, codePoints.size)
    } catch (e: Exception) {
        "🌐"
    }
}

//data class WorldClockItemRelation(
//    val city: String,
//    val country: String,
//    val flag: String,
//    val currentTime: String,
//    val timeZoneId: String,
//    val relation: String,   // "Ahead by 3.0h", "Behind by 2.5h", "Same Time"
//    val diffHours: Double   // numeric difference (positive = ahead, negative = behind)
//)


fun Context.getAllWorldClocksUsingZoneTabWithRelation(): List<WorldClockItem> {
    val clocks = mutableListOf<WorldClockItem>()
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())

    val reader = BufferedReader(InputStreamReader(resources.openRawResource(R.raw.zone_tab)))
    val nowInstant = Instant.now()
    val localZone = ZoneId.systemDefault()
    val localZdt = ZonedDateTime.ofInstant(nowInstant, localZone)

    reader.useLines { lines ->
        lines.forEach { line ->
            if (line.startsWith("#") || line.isBlank()) return@forEach

            val tokens = line.trim().split(Regex("\\s+"))
            if (tokens.size < 3) return@forEach

            val countryCode = tokens[0]
            val zoneId = tokens[2]

            try {
                val zone = ZoneId.of(zoneId)
                val zoneZdt = ZonedDateTime.ofInstant(nowInstant, zone)
                val currentTime = zoneZdt.format(timeFormatter)

                // compute difference using Duration
                val duration =
                    Duration.between(localZdt.toLocalDateTime(), zoneZdt.toLocalDateTime())
                val diffMinutes = duration.toMinutes()
                val diffHoursDecimal = diffMinutes / 60.0

                val relationText = when {
                    diffMinutes > 0 -> getString(
                        R.string.hours_ahead,
                        diffHoursDecimal.toInt().toString()
                    )

                    diffMinutes < 0 -> getString(
                        R.string.hours_behind,
                        (-diffHoursDecimal).toInt().toString()
                    )

                    else -> getString(R.string.same_time)
                }

                val city = zoneId.substringAfterLast("/").replace("_", " ")
                val country = Locale("", countryCode).displayCountry.ifEmpty { countryCode }
                val flag = getFlagEmoji(countryCode)

                clocks.add(
                    WorldClockItem(
                        city = city,
                        country = country,
                        flag = flag,
                        currentTime = currentTime,
                        timeZoneId = zoneId,
                        relation = relationText,
                        diffHours = diffHoursDecimal
                    )
                )
            } catch (_: Exception) {
                // skip invalid zone ids
            }
        }
    }

    return clocks.sortedBy { it.city }
}

fun Context.getAllWidgetClocksUsingZoneTabWithRelation(): List<WidgetClockItem> {
    val clocks = mutableListOf<WidgetClockItem>()
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())

    val reader = BufferedReader(InputStreamReader(resources.openRawResource(R.raw.zone_tab)))
    val nowInstant = Instant.now()
    val localZone = ZoneId.systemDefault()
    val localZdt = ZonedDateTime.ofInstant(nowInstant, localZone)

    reader.useLines { lines ->
        lines.forEach { line ->
            if (line.startsWith("#") || line.isBlank()) return@forEach

            val tokens = line.trim().split(Regex("\\s+"))
            if (tokens.size < 3) return@forEach

            val countryCode = tokens[0]
            val zoneId = tokens[2]

            try {
                val zone = ZoneId.of(zoneId)
                val zoneZdt = ZonedDateTime.ofInstant(nowInstant, zone)
                val currentTime = zoneZdt.format(timeFormatter)

                // compute difference using Duration
                val duration =
                    Duration.between(localZdt.toLocalDateTime(), zoneZdt.toLocalDateTime())
                val diffMinutes = duration.toMinutes()
                val diffHoursDecimal = diffMinutes / 60.0

                val relationText = when {
                    diffMinutes > 0 -> getString(
                        R.string.hours_ahead,
                        diffHoursDecimal.toInt().toString()
                    )

                    diffMinutes < 0 -> getString(
                        R.string.hours_behind,
                        (-diffHoursDecimal).toInt().toString()
                    )

                    else -> getString(R.string.same_time)
                }

                val city = zoneId.substringAfterLast("/").replace("_", " ")
                val country = Locale("", countryCode).displayCountry.ifEmpty { countryCode }
                val flag = getFlagEmoji(countryCode)

                clocks.add(
                    WidgetClockItem(
                        city = city,
                        country = country,
                        flag = flag,
                        currentTime = currentTime,
                        timeZoneId = zoneId,
                        relation = relationText,
                        diffHours = diffHoursDecimal
                    )
                )
            } catch (_: Exception) {
                // skip invalid zone ids
            }
        }
    }

    return clocks.sortedBy { it.city }
}


fun Context.getAllWorldClocksUsingZoneTab(): List<WorldClockItem> {
    val clocks = mutableListOf<WorldClockItem>()
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")

    val reader = BufferedReader(InputStreamReader(resources.openRawResource(R.raw.zone_tab)))
    val now = ZonedDateTime.now()

    reader.useLines { lines ->
        lines.forEach { line ->
            if (line.startsWith("#") || line.isBlank()) return@forEach

            val parts = line.split(Regex("\\s+"))
            if (parts.size >= 3) {
                val countryCode = parts[0]
                val zoneId = parts[2]

                try {
                    val zone = java.time.ZoneId.of(zoneId)
                    val zoneTime = now.withZoneSameInstant(zone)
                    val currentTime = zoneTime.format(formatter)

                    val city = zoneId.substringAfterLast("/").replace("_", " ")
                    val country = Locale("", countryCode).displayCountry
                    val flag = getFlagEmoji(countryCode)

                    clocks.add(
                        WorldClockItem(
                            city = city,
                            country = country,
                            flag = flag,
                            currentTime = currentTime,
                            timeZoneId = zoneId,
                            relation = "",
                            diffHours = 0.0
                        )
                    )
                } catch (_: Exception) {
                }
            }
        }
    }

    return clocks.sortedBy { it.city }
}


data class CurrentLocationClock(
    val region: String,
    val countryName: String,
    val flag: String,
    val time: String,
    val date: String,
    val timeZoneId: String
)


fun getCurrentWorldClock(): CurrentLocationClock {
    val timeZone = TimeZone.getDefault()
    val zoneId = java.time.ZoneId.of(timeZone.id)
    val now = ZonedDateTime.now(zoneId)

    val time = now.format(DateTimeFormatter.ofPattern("hh:mm:ss a"))
    val date = now.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy"))

    // Example: Asia/Karachi → region = Asia, city = Karachi
    val parts = timeZone.id.split("/")
    val region = parts.firstOrNull()?.replace("_", " ") ?: "Unknown"
    val city = parts.getOrNull(1)?.replace("_", " ") ?: "Unknown"

    // Map region to country code
    val regionCountryMap = mapOf(
        "Asia/Karachi" to "PK",
        "Asia/Dubai" to "AE",
        "Europe/London" to "GB",
        "America/New_York" to "US",
        "Asia/Calcutta" to "IN",
        "Asia/Tokyo" to "JP",
        "Europe/Paris" to "FR",
        "Asia/Riyadh" to "SA"
    )

    val countryCode = regionCountryMap[timeZone.id] ?: "🌐"
    val countryName = if (countryCode != "🌐") Locale("", countryCode).displayCountry else region
    val flag = getFlagEmoji(countryCode)

    return CurrentLocationClock(
        region = region,
        countryName = countryName,
        flag = flag,
        time = time,
        date = date,
        timeZoneId = timeZone.id
    )
}

fun updateWidgetTimes(list: List<WidgetClockItem>): List<WidgetClockItem> {
    val now = ZonedDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")

    return list.map { item ->
        try {
            val zone = ZoneId.of(item.timeZoneId)
            val zoneTime = now.withZoneSameInstant(zone)
            val currentTime = zoneTime.format(formatter)

            item.copy(currentTime = currentTime)
        } catch (e: Exception) {
            item
        }
    }
}


fun updateTimes(list: List<WorldClockItem>): List<WorldClockItem> {
    val now = ZonedDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")

    return list.map { item ->
        try {
            val zone = ZoneId.of(item.timeZoneId)
            val zoneTime = now.withZoneSameInstant(zone)
            val currentTime = zoneTime.format(formatter)

            item.copy(currentTime = currentTime)
        } catch (e: Exception) {
            item
        }
    }
}

fun showTimePicker(context: Context, onTimeSelected: (hour: Int, minute: Int) -> Unit) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    val timePickerDialog = TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            onTimeSelected(selectedHour, selectedMinute)
        },
        hour,
        minute,
        false // false → 12-hour format, true → 24-hour format
    )

    timePickerDialog.show()
}

fun getNextAlarmDateTime(hour: Int, minute: Int, repeatDays: String? = null): LocalDateTime {
    val now = LocalDateTime.now()
    var next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
    if (next.isBefore(now)) next = next.plusDays(1)

    if (!repeatDays.isNullOrEmpty()) {
        val days = repeatDays.split(",").mapNotNull {
            try {
                DayOfWeek.valueOf(it.uppercase())
            } catch (e: Exception) {
                null
            }
        }

        if (days.isNotEmpty()) {
            for (i in 0..6) {
                if (days.contains(next.dayOfWeek) && next.isAfter(now)) break
                next = next.plusDays(1)
            }
        }
    }
    return next
}

fun Context.getTimeUntilAlarm(hour: Int?, minute: Int?, repeatDays: String? = null): String {
    // 🛡️ Handle invalid or unset values
    if (hour == null || minute == null) return "No alarm time set yet"

    val now = LocalDateTime.now()
    var nextAlarm = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)

    // If the time today already passed, move to next day
    if (nextAlarm.isBefore(now)) {
        nextAlarm = nextAlarm.plusDays(1)
    }

    // Handle repeat days like "MON,TUE"
    if (!repeatDays.isNullOrEmpty()) {
        val days = repeatDays.split(",").mapNotNull {
            try {
                DayOfWeek.valueOf(it.uppercase())
            } catch (e: Exception) {
                null
            }
        }

        if (days.isNotEmpty()) {
            var candidate = nextAlarm
            for (i in 0..6) {
                if (days.contains(candidate.dayOfWeek) && candidate.isAfter(now)) break
                candidate = candidate.plusDays(1)
            }
            nextAlarm = candidate
        }
    }

    val diffMinutes = ChronoUnit.MINUTES.between(now, nextAlarm)
    if (diffMinutes <= 0) return getString(R.string.alarm_time_is_now)

    val hours = diffMinutes / 60
    val minutes = diffMinutes % 60

    return when {
        hours > 0 && minutes > 0 -> "Alarm in $hours hours $minutes minutes"
        hours > 0 -> "Alarm in $hours hours"
        minutes > 0 -> "Alarm in $minutes minutes"
        else -> getString(R.string.alarm_time_is_now)
    }
}

fun getDayConstant(day: String): Int {
    return when (day.uppercase()) {
        "SUN," -> Calendar.SUNDAY
        "MON," -> Calendar.MONDAY
        "TUE," -> Calendar.TUESDAY
        "WED," -> Calendar.WEDNESDAY
        "THU," -> Calendar.THURSDAY
        "FRI," -> Calendar.FRIDAY
        "SAT," -> Calendar.SATURDAY
        else -> Calendar.MONDAY
    }
}

private fun parseDayToCalendar(day: String): Int? {
    return when (day.trim().uppercase()) {
        "SUN", "SUNDAY" -> Calendar.SUNDAY
        "MON", "MONDAY" -> Calendar.MONDAY
        "TUE", "TUES", "TUESDAY" -> Calendar.TUESDAY
        "WED", "WEDNESDAY" -> Calendar.WEDNESDAY
        "THU", "THUR", "THURSDAY" -> Calendar.THURSDAY
        "FRI", "FRIDAY" -> Calendar.FRIDAY
        "SAT", "SATURDAY" -> Calendar.SATURDAY
        else -> null
    }
}

/**
 * Returns how many days to add from `now` to next targetDay (Calendar.DAY_OF_WEEK).
 * returns 0..6; if today and target time already passed, returns 7
 */
private fun daysUntilNext(
    now: Calendar,
    targetDayOfWeek: Int,
    targetHour: Int,
    targetMinute: Int
): Int {
    val todayDow = now.get(Calendar.DAY_OF_WEEK)
    var delta = (targetDayOfWeek - todayDow + 7) % 7
    if (delta == 0) {
        // same weekday -> check time
        val candidate = now.clone() as Calendar
        candidate.set(Calendar.HOUR_OF_DAY, targetHour)
        candidate.set(Calendar.MINUTE, targetMinute)
        candidate.set(Calendar.SECOND, 0)
        candidate.set(Calendar.MILLISECOND, 0)
        if (candidate.timeInMillis <= now.timeInMillis) {
            delta = 7
        }
    }
    return delta
}

fun canScheduleExactAlarms(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }
}

fun requestExactAlarmPermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        intent.data = Uri.parse("package:${context.packageName}")
        context.startActivity(intent)
    }
}

fun Context.isNotificationPermissionGranted(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true // Automatically granted below Android 13
    }
}

const val NOTIFICATION_PERMISSION_CODE = 101

fun Activity.requestNotificationPermissionIfNeeded() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

        if (!isNotificationPermissionGranted()) {
            MyApplication.isResume = false
            requestPermissions(
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_CODE
            )
        }
    }
}

fun Activity.showNotificationPermissionSettings() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        MyApplication.isResume = false
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }
        startActivity(intent)
    }
}


@SuppressLint("ScheduleExactAlarm")
fun setAlarm(context: Context, alarm: AlarmEntity) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val days = alarm.repeatDays
        .split(",")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    if (days.isEmpty()) {
        // one-time alarm: compute next occurrence (today or tomorrow)
        val now = Calendar.getInstance()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now.timeInMillis) add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarmId", alarm.id)
            putExtra("label", alarm.label)
            putExtra("vibrate", alarm.vibrate)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Log.d(
            "AlarmScheduler",
            "Setting one-time alarm id=${alarm.id} time=${Date(cal.timeInMillis)}"
        )
//        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)

        if (canScheduleExactAlarms(context)) {
            Log.d(
                "AlarmScheduler",
                "canScheduleExactAlarms"
            )
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(cal.timeInMillis, pendingIntent),
                pendingIntent
            )
        } else {
            Log.d(
                "AlarmScheduler",
                "requestExactAlarmPermission"
            )
            requestExactAlarmPermission(context)
        }


    } else {
        val now = Calendar.getInstance()
        days.forEach { dayStr ->
            val dayConst = parseDayToCalendar(dayStr)
            if (dayConst == null) {
                Log.w("AlarmScheduler", "Unknown day string: '$dayStr' for alarm ${alarm.id}")
                return@forEach
            }

            val addDays = daysUntilNext(now, dayConst, alarm.hour, alarm.minute)
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, addDays)
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val requestCode = alarm.id * 10 + dayConst
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("alarmId", alarm.id)
                putExtra("label", alarm.label)
                putExtra("vibrate", alarm.vibrate)
                putExtra("repeatDay", dayConst)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            Log.d(
                "AlarmScheduler",
                "Setting repeating alarm id=${alarm.id} day=$dayStr (dow=$dayConst) requestCode=$requestCode time=${
                    Date(cal.timeInMillis)
                } addDays=$addDays"
            )
//            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
            if (canScheduleExactAlarms(context)) {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(cal.timeInMillis, pendingIntent),
                    pendingIntent
                )
            } else {
                requestExactAlarmPermission(context)
            }

        }
    }
}

fun cancelAlarm(context: Context, alarm: AlarmEntity) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val days = alarm.repeatDays.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    if (days.isEmpty()) {

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("alarmId", alarm.id)  // same extras as schedule
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id, // use SAME requestCode
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        Log.d("AlarmScheduler", "Canceled one-time alarm id=${alarm.id}")

    } else {

        days.forEach { dayStr ->

            val dayConst = parseDayToCalendar(dayStr)
            if (dayConst == null) {
                Log.e("AlarmScheduler", "Invalid day $dayStr")
                return@forEach
            }

            val requestCode = alarm.id * 10 + dayConst

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("alarmId", alarm.id)  // must match schedule
                putExtra("day", dayStr)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)

            Log.d(
                "AlarmScheduler",
                "Canceled repeating alarm id=${alarm.id} day=$dayStr rc=$requestCode"
            )
        }
    }
}

//fun cancelAlarm(context: Context, alarm: AlarmEntity) {
//    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//    val days = alarm.repeatDays.split(",").map { it.trim() }.filter { it.isNotEmpty() }
//
//    if (days.isEmpty()) {
//        val pendingIntent = PendingIntent.getBroadcast(
//            context,
//            alarm.id,
//            Intent(context, AlarmReceiver::class.java),
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//        Log.d("AlarmScheduler", "Canceling one-time alarm id=${alarm.id}")
//        alarmManager.cancel(pendingIntent)
//    } else {
//        days.forEach { dayStr ->
//            val dayConst = parseDayToCalendar(dayStr)
//            if (dayConst == null) return@forEach
//            val requestCode = alarm.id * 10 + dayConst
//            val pendingIntent = PendingIntent.getBroadcast(
//                context,
//                requestCode,
//                Intent(context, AlarmReceiver::class.java),
//                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//            )
//            Log.d("AlarmScheduler", "Canceling repeating alarm id=${alarm.id} day=$dayStr (dow=$dayConst) requestCode=$requestCode")
//            alarmManager.cancel(pendingIntent)
//        }
//    }
//}







