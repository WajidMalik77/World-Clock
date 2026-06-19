package com.worldclock.app_themes.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.worldclock.app_themes.R
import com.worldclock.app_themes.databinding.ActivityAddAllRemindersBinding
import com.worldclock.app_themes.core.utils.GradientTextHelper
import androidx.core.graphics.toColorInt
import com.worldclock.app_themes.presentation.adapter.ReminderAdapter
import com.worldclock.app_themes.data.database.ReminderDao
import com.worldclock.app_themes.data.database.WorldClockDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.worldclock.app_themes.ads.helpers.ui.NativeAdOrchestrator
import com.worldclock.app_themes.ads.helpers.models.NativeAdConfig
import com.worldclock.app_themes.core.utils.PrefUtil
import com.worldclock.app_themes.core.utils.AdsConstants.LifeTimePref
import com.worldclock.app_themes.ads.helpers.safeShowInterstitialAction
import com.worldclock.app_themes.core.analytics.AppEventLogger

@AndroidEntryPoint
class AddAllRemindersActivity : BaseActivity() {
    @Inject
    lateinit var bannerAdOrchestrator: com.worldclock.app_themes.ads.helpers.ui.BannerAdOrchestrator


    private val binding by lazy { ActivityAddAllRemindersBinding.inflate(layoutInflater) }

    @Inject
    lateinit var nativeAdOrchestrator: NativeAdOrchestrator
    private lateinit var dao: ReminderDao
    private lateinit var adapter: ReminderAdapter
    private var categoryId = -1
    private var categoryTitle = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppEventLogger.trackScreenCreate(this, savedInstanceState, "AddAllRemindersScreen", "activity_lifecycle")
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)

        lifecycleScope.launch {
            bannerAdOrchestrator.loadBannerAd(
                context = this@AddAllRemindersActivity,
                screen = "AddAllRemindersScreen",
                position = "top",
                container = binding.bannerContainer.admobBanner,
                shimmer = binding.bannerContainer.bannerAdShimmer
            )
        }



        lifecycleScope.launch {
            nativeAdOrchestrator.loadNativeAds(
                context = this@AddAllRemindersActivity,
                screen = "AddAllRemindersScreen",
                nativeConfigs = listOf(
                    NativeAdConfig(
                        position = "bottom",
                        container = binding.adsContainer.admobNative,
                        shimmer = binding.adsContainer.nativeAdShimmer
                    )
                )
            )
        }

        categoryId = intent.getIntExtra("category_id", -1)
        categoryTitle = intent.getStringExtra("category_title") ?: ""

        dao = WorldClockDatabase.getDatabase(this).reminderDao()

        binding.toolbar.back.setOnClickListener {
            AppEventLogger.trackButtonClick("AddAllRemindersScreen", "back", "navigate_back", "reminders_flow")
            finish()
        }
        binding.toolbar.title.text = getString(R.string.all_reminders)
        binding.textView1.text = getString(R.string.create_reminder_for, categoryTitle)

        GradientTextHelper.apply(
            binding.textView,
            "#7441D0".toColorInt(),
            "#EF4A9A".toColorInt(),
            direction = GradientTextHelper.Direction.LEFT_RIGHT
        )

        setupRecycler()

        binding.addNewReminder.setOnClickListener {
            AppEventLogger.trackButtonClick("AddAllRemindersScreen", "add_reminder", "navigate", "reminders_flow")
            val navigate = {
                startActivity(
                    Intent(this, AddReminderActiviity::class.java)
                        .putExtra("category_id", categoryId)
                        .putExtra("category_title", categoryTitle)
                )
            }

            val isPremium = PrefUtil(this).getBool("is_premium", false)
                || getSharedPreferences(LifeTimePref, 0).getBoolean("premium", false)

            if (isPremium) {
                navigate()
            } else {
                safeShowInterstitialAction(
                    screenName = "AddAllRemindersScreen",
                    trigger = "add",
                    noCounterNeeded = false,
                    afterAd = navigate
                )
            }
        }
    }

    private fun setupRecycler() {
        adapter = ReminderAdapter(
            onToggle = { reminder, enabled ->
                AppEventLogger.trackButtonClick("AddAllRemindersScreen", "toggle_reminder", "toggle", "reminders_list")
                CoroutineScope(Dispatchers.IO).launch {
                    dao.setEnabled(reminder.id, enabled)
                }
            },
            onEdit = { reminder ->
                AppEventLogger.trackButtonClick("AddAllRemindersScreen", "edit_reminder", "navigate", "reminders_list")
                val navigate = {
                    startActivity(
                        Intent(this@AddAllRemindersActivity, AddReminderActiviity::class.java)
                            .putExtra("reminder_id", reminder.id)
                            .putExtra("category_id", categoryId)
                            .putExtra("category_title", categoryTitle)
                    )
                }

                val isPremium = PrefUtil(this@AddAllRemindersActivity).getBool("is_premium", false)
                    || getSharedPreferences(LifeTimePref, 0).getBoolean("premium", false)

                if (isPremium) {
                    navigate()
                } else {
                    safeShowInterstitialAction(
                        screenName = "AddAllRemindersScreen",
                        trigger = "edit",
                        noCounterNeeded = false,
                        afterAd = navigate
                    )
                }
            },
            onDelete = { reminder ->
                AppEventLogger.trackButtonClick("AddAllRemindersScreen", "delete_reminder", "delete", "reminders_list")
                CoroutineScope(Dispatchers.IO).launch {
                    dao.deleteReminder(reminder)
                }
            }
        )

        binding.recyclerAddReminder.adapter = adapter

        // Observe reminders for this category
        dao.getRemindersByCategory(categoryId).observe(this) { reminders ->
            adapter.submitList(reminders)
            // Show empty state if no reminders
            binding.empty.visibility = if (reminders.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerAddReminder.visibility = if (reminders.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    override fun onDestroy() {
        AppEventLogger.trackScreenDestroy(this, "AddAllRemindersScreen")
        super.onDestroy()
    }
}
