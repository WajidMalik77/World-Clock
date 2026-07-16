package com.worldclock.app_themes.presentation.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.worldclock.app_themes.R
import com.worldclock.app_themes.presentation.adapter.AddClockAdapter
import com.worldclock.app_themes.data.database.WorldClockDao
import com.worldclock.app_themes.data.database.WorldClockDatabase
import com.worldclock.app_themes.databinding.ActivityAddClockBinding
import com.worldclock.app_themes.core.utils.getAllWorldClocksUsingZoneTabWithRelation
import com.worldclock.app_themes.core.utils.updateTimes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
import com.worldclock.app_themes.core.utils.getCurrentWorldClock

@AndroidEntryPoint
class AddClockActivity : BaseActivity() {
    @Inject
    lateinit var bannerAdOrchestrator: com.worldclock.app_themes.ads.helpers.ui.BannerAdOrchestrator

    private val binding by lazy {
        ActivityAddClockBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var nativeAdOrchestrator: NativeAdOrchestrator

    private lateinit var dao: WorldClockDao
    private lateinit var adapter: AddClockAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppEventLogger.trackScreenCreate(
            this,
            savedInstanceState,
            "AddClockScreen",
            "activity_lifecycle"
        )
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)


        dao = WorldClockDatabase.getDatabase(this).worldClockDao()


        PreloadController.observeBanner(this,
            BannerPreload.adBannerTopLiveData,
            binding.bannerContainer.bannerTopContainer,binding.bannerContainer.adVeiwTop,binding.bannerContainer.adTextAdvertisementTop,
            GetFirebase.banner_ad_addclock_top,"top",
            window,
            NativePreload.adNativeTopLiveData){

        }

        PreloadController.observeBanner(this,
            BannerPreload.adBannerBottomLiveData,
            binding.adsContainer.bannerBottomContainer,binding.adsContainer.adVeiwBottom,binding.adsContainer.adTextAdvertisementBottom,
            GetFirebase.banner_ad_addclock_bottom,"bottom",
            window,
            NativePreload.adNativeBottomLiveData){

        }
        val imm =
            getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager


        CoroutineScope(Dispatchers.IO).launch {
            val existingCount = dao.getCount()  // 👈 check existing data

            if (existingCount == 0) {
                // generate and insert only if database is empty
                val currentClock = getCurrentWorldClock()
                val clocks = getAllWorldClocksUsingZoneTabWithRelation().filter { it.country.lowercase() != currentClock.countryName.lowercase() }
                dao.insertAll(clocks)
                Log.d("DB", "Inserted ${clocks.size} clocks")
            } else {
                Log.d("DB", "Database already has data, skipping insert.")
            }

            withContext(Dispatchers.Main) {
                adapter = AddClockAdapter { clock ->
                    lifecycleScope.launch {
                        dao.updateClock(clock)
                    }
                }
                binding.toolbar.title.text = getString(R.string.add_city)
                binding.toolbar.back.setOnClickListener {
                    AppEventLogger.trackButtonClick(
                        "AddClockScreen",
                        "back",
                        "navigate_back",
                        "clock_flow"
                    )
                    finish()
                }
                loadClocks()

                binding.addNewClock.setOnClickListener {
                    AppEventLogger.trackButtonClick(
                        "AddClockScreen",
                        "save",
                        "save_city",
                        "clock_flow"
                    )
                    val selectedItems = adapter.getSelectedItems()
                    if (selectedItems.isEmpty()) return@setOnClickListener

                    val navigate = {
                        lifecycleScope.launch {
                            selectedItems.forEach { dao.updateClock(it) }
                            startActivity(Intent(this@AddClockActivity, ClockActivity::class.java))
                            finish()
                        }
                        Unit
                    }

                    val isPremium = PrefUtil(this@AddClockActivity).getBool("is_premium", false)
                            || getSharedPreferences(LifeTimePref, 0).getBoolean("premium", false)

                    if (isPremium) {
                        navigate()
                    } else {
                        PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_clockactivity_top,"top",this@AddClockActivity,
                            GetFirebase.adIdClock_bannerTop, GetFirebase.adIdClock_nativeTop)

                        PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_clockactivity_bottom,"bottom",this@AddClockActivity,
                            GetFirebase.adIdClock_bannerBottom, GetFirebase.adIdClock_nativeBottom)



                        InterstitialAdManager.showIfReady(
                            this@AddClockActivity,
                            InterstitialScreen.OTHER,
                            GetFirebase.adIdOther_interstitial,
                            if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                            GetFirebase.transition_AddClockBack,
                            GetFirebase.counter_interval,
                            Utils.isPremium,
                            GetFirebase.enable_interstitial_ads,
                            {
                           navigate()
                            },
                            {
                           navigate()
                            })

                    }
                }


                binding.searchIcon.setOnClickListener {
                    AppEventLogger.trackButtonClick(
                        "AddClockScreen",
                        "search",
                        "toggle",
                        "clock_search"
                    )
                    if (binding.searchBar.isGone) {
                        // Expand with animation
                        binding.searchBar.visibility = View.VISIBLE
                        binding.searchIcon.setImageResource(R.drawable.close)

                        binding.searchBar.alpha = 0f
                        binding.searchBar.animate()
                            .alpha(1f)
                            .setDuration(300)
                            .withEndAction {
                                binding.searchBar.requestFocus()

                                imm?.showSoftInput(
                                    binding.searchBar,
                                    InputMethodManager.SHOW_IMPLICIT
                                )
                            }
                            .start()
                    } else {
                        binding.searchIcon.setImageResource(R.drawable.search)
                        imm?.hideSoftInputFromWindow(
                            window.decorView.windowToken,
                            0
                        )
                        // Collapse with animation
                        binding.searchBar.animate()
                            .alpha(0f)
                            .setDuration(300)
                            .withEndAction {
                                binding.searchBar.visibility = View.GONE
                                binding.searchBar.text?.clear()
                            }
                            .start()
                    }
                }
            }
        }

        onBackPressedDispatcher.addCallback(this){

            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_clockactivity_top,"top",this@AddClockActivity,
                GetFirebase.adIdClock_bannerTop, GetFirebase.adIdClock_nativeTop)

            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_clockactivity_bottom,"bottom",this@AddClockActivity,
                GetFirebase.adIdClock_bannerBottom, GetFirebase.adIdClock_nativeBottom)


            InterstitialAdManager.showIfReady(
                this@AddClockActivity,
                InterstitialScreen.OTHER,
                GetFirebase.adIdOther_interstitial,
                if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                GetFirebase.transition_AddClockBack,
                GetFirebase.counter_interval,
                Utils.isPremium,
                GetFirebase.enable_interstitial_ads,
                {
                    startActivity(Intent(this@AddClockActivity, ClockActivity::class.java))
                    finish()
                },
                {
                    startActivity(Intent(this@AddClockActivity, ClockActivity::class.java))
                    finish()
                })

        }

    }

    override fun onDestroy() {
        AppEventLogger.trackScreenDestroy(this, "AddClockScreen")
        super.onDestroy()
    }

    private fun loadClocks() {
        CoroutineScope(Dispatchers.IO).launch {
            val clocks =
                WorldClockDatabase.getDatabase(this@AddClockActivity).worldClockDao().getAllClocks()
            val updated = updateTimes(clocks)
            withContext(Dispatchers.Main) {
                val currentClock = getCurrentWorldClock()
                val clocksU = updated.filter { it.country.lowercase() != currentClock.countryName.lowercase() }



                adapter.updateList(clocksU)
                binding.recycler.adapter = adapter
                binding.searchBar.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {}

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        val query = s.toString().trim()
                        val filtered = if (query.isEmpty()) {
                            clocksU
                        } else {
                            clocksU.filter {
                                it.city.contains(query, ignoreCase = true) ||
                                        it.country.contains(query, ignoreCase = true)
                            }
                        }
                        adapter.updateList(filtered)
                    }
                })

            }
        }
    }
}
