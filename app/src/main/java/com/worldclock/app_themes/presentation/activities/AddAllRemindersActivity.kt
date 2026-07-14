package com.worldclock.app_themes.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
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
import com.worldclock.app_themes.ads.preload.AdLoadMode
import com.worldclock.app_themes.ads.preload.BannerPreload
import com.worldclock.app_themes.ads.preload.InterstitialAdManager
import com.worldclock.app_themes.ads.preload.InterstitialScreen
import com.worldclock.app_themes.ads.preload.NativePreload
import com.worldclock.app_themes.ads.preload.PreloadController
import com.worldclock.app_themes.ads.utils.GetFirebase
import com.worldclock.app_themes.ads.utils.Utils
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




        PreloadController.observeBanner(this,
            BannerPreload.adBannerTopLiveData,
            binding.bannerContainer.bannerTopContainer,binding.bannerContainer.adVeiwTop,binding.bannerContainer.adTextAdvertisementTop,
            GetFirebase.banner_ad_addallreminders_top,"top",
            window,
            NativePreload.adNativeTopLiveData){

        }

        PreloadController.observeBanner(this,
            BannerPreload.adBannerBottomLiveData,
            binding.adsContainer.bannerBottomContainer,binding.adsContainer.adVeiwBottom,binding.adsContainer.adTextAdvertisementBottom,
            GetFirebase.banner_ad_addallreminders_bottom,"bottom",
            window,
            NativePreload.adNativeBottomLiveData){

        }

        categoryId = intent.getIntExtra("category_id", -1)
        categoryTitle = intent.getStringExtra("category_title") ?: ""

        dao = WorldClockDatabase.getDatabase(this).reminderDao()

        binding.toolbar.back.setOnClickListener {
            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_allreminders_top,"top",this@AddAllRemindersActivity,
                GetFirebase.adIdAllReminders_bannerTop, GetFirebase.adIdAllReminders_nativeTop)

            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_allreminders_bottom,"bottom",this@AddAllRemindersActivity,
                GetFirebase.adIdAllReminders_bannerBottom, GetFirebase.adIdAllReminders_nativeBottom)

            InterstitialAdManager.showIfReady(
                this@AddAllRemindersActivity,
                InterstitialScreen.OTHER,
                GetFirebase.adIdOther_interstitial,
                if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                GetFirebase.transition_AddAllRemindersBack,
                GetFirebase.counter_interval,
                Utils.isPremium,
                GetFirebase.enable_interstitial_ads,
                {
                    startActivity(Intent(this@AddAllRemindersActivity, AllRemindersActivity::class.java))
                    finish()
                },
                {
                    startActivity(Intent(this@AddAllRemindersActivity, AllRemindersActivity::class.java))
                    finish()
                })
            AppEventLogger.trackButtonClick("AddAllRemindersScreen", "back", "navigate_back", "reminders_flow")
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
                PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_addreminder_top,"top",this@AddAllRemindersActivity,
                    GetFirebase.adIdAddReminder_bannerTop, GetFirebase.adIdAddReminder_nativeTop)

                PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_addreminder_bottom,"bottom",this@AddAllRemindersActivity,
                    GetFirebase.adIdAddReminder_bannerBottom, GetFirebase.adIdAddReminder_nativeBottom)

                InterstitialAdManager.showIfReady(this, InterstitialScreen.OTHER, GetFirebase.adIdOther_interstitial,
                    if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                    GetFirebase.transition_AddAllRemindersForward, GetFirebase.counter_interval,
                    Utils.isPremium, GetFirebase.enable_interstitial_ads,{
                        navigate()
                    },{
                        navigate()
                    })

            }
        }


        onBackPressedDispatcher.addCallback(this){
            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_allreminders_top,"top",this@AddAllRemindersActivity,
                GetFirebase.adIdAllReminders_bannerTop, GetFirebase.adIdAllReminders_nativeTop)

            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_allreminders_bottom,"bottom",this@AddAllRemindersActivity,
                GetFirebase.adIdAllReminders_bannerBottom, GetFirebase.adIdAllReminders_nativeBottom)

            InterstitialAdManager.showIfReady(
                this@AddAllRemindersActivity,
                InterstitialScreen.OTHER,
                GetFirebase.adIdOther_interstitial,
                if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                GetFirebase.transition_AddAllRemindersBack,
                GetFirebase.counter_interval,
                Utils.isPremium,
                GetFirebase.enable_interstitial_ads,
                {
                    startActivity(Intent(this@AddAllRemindersActivity, AllRemindersActivity::class.java))
                    finish()
                },
                {
                    startActivity(Intent(this@AddAllRemindersActivity, AllRemindersActivity::class.java))
                    finish()
                })
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
                    PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_addreminder_top,"top",this@AddAllRemindersActivity,
                        GetFirebase.adIdAddReminder_bannerTop, GetFirebase.adIdAddReminder_nativeTop)

                    PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_addreminder_bottom,"bottom",this@AddAllRemindersActivity,
                        GetFirebase.adIdAddReminder_bannerBottom, GetFirebase.adIdAddReminder_nativeBottom)

                    InterstitialAdManager.showIfReady(this, InterstitialScreen.OTHER, GetFirebase.adIdOther_interstitial,
                        if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                        GetFirebase.transition_AddAllRemindersForward, GetFirebase.counter_interval,
                        Utils.isPremium, GetFirebase.enable_interstitial_ads,{
                            navigate()
                        },{
                            navigate()
                        })

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
