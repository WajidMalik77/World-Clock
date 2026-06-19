package com.worldclock.app_themes.presentation.activities

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.worldclock.app_themes.R
import com.worldclock.app_themes.data.database.AlarmDao
import com.worldclock.app_themes.data.database.AlarmEntity
import com.worldclock.app_themes.data.database.WorldClockDatabase
import com.worldclock.app_themes.databinding.ActivityAddAlarmBinding
import com.worldclock.app_themes.core.utils.GradientTextHelper
import com.worldclock.app_themes.core.utils.cancelAlarm
import com.worldclock.app_themes.core.utils.setAlarm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import androidx.core.graphics.toColorInt
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.worldclock.app_themes.ads.helpers.ui.NativeAdOrchestrator
import com.worldclock.app_themes.ads.helpers.models.NativeAdConfig
import com.worldclock.app_themes.core.utils.PrefUtil
import com.worldclock.app_themes.core.utils.AdsConstants.LifeTimePref
import com.worldclock.app_themes.ads.helpers.safeShowInterstitialAction
import com.worldclock.app_themes.core.analytics.AppEventLogger

@AndroidEntryPoint
class AddAlarmActivity : BaseActivity() {
    @Inject
    lateinit var bannerAdOrchestrator: com.worldclock.app_themes.ads.helpers.ui.BannerAdOrchestrator

    private val binding by lazy { ActivityAddAlarmBinding.inflate(layoutInflater) }

    @Inject
    lateinit var nativeAdOrchestrator: NativeAdOrchestrator

    private var alarmId: Int = -1
    private lateinit var dao: AlarmDao
    private var isVib = false
    private var repeatedStrings = StringBuilder()
    private var selectedSnooze = 5
    private var isAm = true  // ✅ AM/PM state

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppEventLogger.trackScreenCreate(this, savedInstanceState, "AddAlarmScreen", "activity_lifecycle")
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)

        lifecycleScope.launch {
            bannerAdOrchestrator.loadBannerAd(
                context = this@AddAlarmActivity,
                screen = "AddAlarmScreen",
                position = "top",
                container = binding.bannerContainer.admobBanner,
                shimmer = binding.bannerContainer.bannerAdShimmer
            )
        }



        lifecycleScope.launch {
            nativeAdOrchestrator.loadNativeAds(
                context = this@AddAlarmActivity,
                screen = "AddAlarmScreen",
                nativeConfigs = listOf(
                    NativeAdConfig(
                        position = "bottom",
                        container = binding.adsContainer.admobNative,
                        shimmer = binding.adsContainer.nativeAdShimmer
                    )
                )
            )
        }

        GradientTextHelper.apply(
            binding.save,
            "#7441D0".toColorInt(),
            "#EF4A9A".toColorInt(),
            direction = GradientTextHelper.Direction.LEFT_RIGHT
        )

        setupSnoozeChips()
        setupAmPm()  // ✅

        binding.hourPicker.apply {
            minValue = 1
            maxValue = 12
            value = Calendar.getInstance().get(Calendar.HOUR).takeIf { it != 0 } ?: 12
        }

        binding.minutePicker.apply {
            minValue = 0
            maxValue = 59
            value = Calendar.getInstance().get(Calendar.MINUTE)
            displayedValues = (0..59).map { String.format("%02d", it) }.toTypedArray()
        }

        // ✅ Set initial AM/PM based on current time
        isAm = Calendar.getInstance().get(Calendar.AM_PM) == Calendar.AM
        updateAmPm()

        binding.toolbar.back.setOnClickListener {
            AppEventLogger.trackButtonClick("AddAlarmScreen", "back", "navigate_back", "alarm_flow")
            finish()
        }
        binding.toolbar.title.text = getString(R.string.new_alarm)
        dao = WorldClockDatabase.getDatabase(this).alarmDao()
        alarmId = intent.getIntExtra("alarmId", -1)
        if (alarmId != -1) loadAlarmData()

        binding.cancel.setOnClickListener {
            AppEventLogger.trackButtonClick("AddAlarmScreen", "cancel", "dismiss", "alarm_flow")
            finish()
        }

        binding.vibRadio.setOnClickListener {
            AppEventLogger.trackButtonClick("AddAlarmScreen", "vibration", "toggle", "alarm_settings")
            isVib = !isVib
            if (isVib) binding.vibRadio.setImageResource(R.drawable.alarm_selected)
            else binding.vibRadio.setImageResource(R.drawable.alarm_unselected)
        }

        binding.save.setOnClickListener {
            AppEventLogger.trackButtonClick("AddAlarmScreen", "save", "save_alarm", "alarm_flow")
            val saveAction = {
                val hour12 = binding.hourPicker.value
                val minute = binding.minutePicker.value

                // ✅ Convert 12h + AM/PM → 24h
                val hour24 = when {
                    isAm && hour12 == 12 -> 0
                    !isAm && hour12 != 12 -> hour12 + 12
                    else -> hour12
                }

                val alarm = AlarmEntity(
                    label = binding.labelEt.text.toString(),
                    hour = hour24,
                    minute = minute,
                    vibrate = isVib,
                    repeatDays = repeatedStrings.toString(),
                    alarmSound = binding.alarmSoundTxt.text.toString(),
                    snoozeMinutes = selectedSnooze
                )

                CoroutineScope(Dispatchers.IO).launch {
                    val oldAlarm = dao.getAlarmById(alarmId)
                    if (oldAlarm != null) {
                        val updatedAlarm = oldAlarm.copy(
                            label = alarm.label,
                            hour = alarm.hour,
                            minute = alarm.minute,
                            vibrate = alarm.vibrate,
                            repeatDays = alarm.repeatDays,
                            alarmSound = alarm.alarmSound,
                            snoozeMinutes = alarm.snoozeMinutes
                        )
                        dao.updateAlarm(updatedAlarm)
                        cancelAlarm(this@AddAlarmActivity, oldAlarm)
                        setAlarm(this@AddAlarmActivity, updatedAlarm)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@AddAlarmActivity, "Alarm updated!", Toast.LENGTH_SHORT)
                                .show()
                            finish()
                        }
                    } else {
                        val newId = dao.insertAlarm(alarm)
                        val newAlarm = alarm.copy(id = newId.toInt())
                        withContext(Dispatchers.Main) {
                            if (newAlarm.isEnabled) setAlarm(this@AddAlarmActivity, newAlarm)
                            else cancelAlarm(this@AddAlarmActivity, newAlarm)
                            Toast.makeText(this@AddAlarmActivity, "Alarm added!", Toast.LENGTH_SHORT)
                                .show()
                            finish()
                        }
                    }
                }
                Unit
            }

            val isPremium = PrefUtil(this).getBool("is_premium", false)
                || getSharedPreferences(LifeTimePref, 0).getBoolean("premium", false)

            if (isPremium) {
                saveAction()
            } else {
                safeShowInterstitialAction(
                    screenName = "AddAlarmScreen",
                    trigger = "save",
                    noCounterNeeded = false,
                    afterAd = saveAction
                )
            }
        }
        setupDaySelector()
    }

    // ✅ AM/PM setup
    private fun setupAmPm() {
        binding.llAm.setOnClickListener {
            AppEventLogger.trackButtonClick("AddAlarmScreen", "am", "select", "alarm_time")
            isAm = true
            updateAmPm()
        }
        binding.llPm.setOnClickListener {
            AppEventLogger.trackButtonClick("AddAlarmScreen", "pm", "select", "alarm_time")
            isAm = false
            updateAmPm()
        }
    }

    private fun updateAmPm() {
        binding.ivAmCheck.setImageResource(
            if (isAm) R.drawable.ic_radio_checked else R.drawable.ic_radio_unchecked
        )
        binding.ivPmCheck.setImageResource(
            if (isAm) R.drawable.ic_radio_unchecked else R.drawable.ic_radio_checked
        )
    }

    private fun loadAlarmData() {
        lifecycleScope.launch {
            val alarm = dao.getAlarmById(alarmId)
            if (alarm != null) {
                binding.labelEt.setText(alarm.label)
                binding.vibRadio.setImageResource(
                    if (alarm.vibrate) R.drawable.alarm_selected else R.drawable.alarm_unselected
                )
                repeatedStrings.append(alarm.repeatDays)
                binding.dayName.text = repeatedStrings.toString()
                setupDaySelectorUpdates(alarm.repeatDays)

                // ✅ Restore AM/PM from 24h hour
                isAm = alarm.hour < 12
                binding.hourPicker.value = when {
                    alarm.hour == 0 -> 12
                    alarm.hour > 12 -> alarm.hour - 12
                    else -> alarm.hour
                }
                updateAmPm()

                binding.minutePicker.value = alarm.minute
                binding.alarmSoundTxt.text = alarm.alarmSound

                selectedSnooze = alarm.snoozeMinutes
                val chipMap = mapOf(
                    5 to binding.snooze5,
                    10 to binding.snooze10,
                    15 to binding.snooze15,
                    20 to binding.snooze20
                )
                chipMap.values.forEach {
                    it.setTextColor(ContextCompat.getColor(this@AddAlarmActivity, R.color.black))
                }
                chipMap[alarm.snoozeMinutes]?.let {
                    it.setBackgroundResource(R.drawable.chip_selected)
                    it.setTextColor(Color.WHITE)
                }
            }
        }
    }

    private fun setDayName(pos: Int) = when (pos) {
        1 -> "SUN,"; 2 -> "MON,"; 3 -> "TUE,"; 4 -> "WED,"
        5 -> "THU,"; 6 -> "FRI,"; 7 -> "SAT,"; else -> ""
    }

    private fun setupSnoozeChips() {
        val chips = mapOf(
            binding.snooze5 to 5,
            binding.snooze10 to 10,
            binding.snooze15 to 15,
            binding.snooze20 to 20
        )
        chips.forEach { (chip, minutes) ->
            chip.setOnClickListener {
                AppEventLogger.trackButtonClick("AddAlarmScreen", "snooze_${minutes}_minutes", "select", "alarm_snooze")
                selectedSnooze = minutes
                chips.keys.forEach {
                    it.setTextColor(ContextCompat.getColor(this, R.color.black))
                }
                chip.setBackgroundResource(R.drawable.chip_selected)
                chip.setTextColor(Color.WHITE)
            }
        }
    }

    private fun setupDaySelector() {
        val dayViews = listOf(
            binding.days.daySun, binding.days.dayMon, binding.days.dayTue,
            binding.days.dayWed, binding.days.dayThu, binding.days.dayFri, binding.days.daySat
        )
        dayViews.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                AppEventLogger.trackButtonClick("AddAlarmScreen", "repeat_day_${index + 1}", "toggle", "alarm_repeat")
                textView.isSelected = !textView.isSelected
                if (textView.isSelected) {
                    repeatedStrings.append(setDayName(index + 1))
                    textView.setBackgroundResource(R.drawable.gradient_ring)
                    GradientTextHelper.apply(
                        textView,
                        "#7441D0".toColorInt(),
                        "#EF4A9A".toColorInt()
                    )
                } else {
                    val day = setDayName(index + 1)
                    repeatedStrings.replace(
                        repeatedStrings.indexOf(day),
                        repeatedStrings.indexOf(day) + day.length,
                        ""
                    )
                    textView.background = null
                    textView.paint.shader = null
                    textView.setTextColor(Color.parseColor("#B1B1B1"))
                }
                binding.dayName.text = repeatedStrings.toString()
            }
        }
    }

    private fun setupDaySelectorUpdates(repeatDays: String) {
        val dayViews = listOf(
            binding.days.daySun, binding.days.dayMon, binding.days.dayTue,
            binding.days.dayWed, binding.days.dayThu, binding.days.dayFri, binding.days.daySat
        )
        for (index in 0..6) {
            val dayName = setDayName(index + 1)
            if (repeatedStrings.contains(dayName)) {
                dayViews[index].isSelected = true
                dayViews[index].setBackgroundResource(R.drawable.gradient_ring)
                GradientTextHelper.apply(
                    dayViews[index],
                    "#7441D0".toColorInt(),
                    "#EF4A9A".toColorInt()
                )
            } else {
                dayViews[index].isSelected = false
                dayViews[index].background = null
                dayViews[index].paint.shader = null
                dayViews[index].setTextColor("#B1B1B1".toColorInt())
            }
        }
    }

    override fun onDestroy() {
        AppEventLogger.trackScreenDestroy(this, "AddAlarmScreen")
        super.onDestroy()
    }
}
