package com.worldclock.app_themes.presentation.activities

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
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
import com.worldclock.app_themes.core.analytics.AppEventLogger

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
        AppEventLogger.trackScreenCreate(this, savedInstanceState, "AddClockScreen", "activity_lifecycle")
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)

        lifecycleScope.launch {
            bannerAdOrchestrator.loadBannerAd(
                context = this@AddClockActivity,
                screen = "AddClockScreen",
                position = "top",
                container = binding.bannerContainer.admobBanner,
                shimmer = binding.bannerContainer.bannerAdShimmer
            )
        }



        lifecycleScope.launch {
            nativeAdOrchestrator.loadNativeAds(
                context = this@AddClockActivity,
                screen = "AddClockScreen",
                nativeConfigs = listOf(
                    NativeAdConfig(
                        position = "bottom",
                        container = binding.adsContainer.admobNative,
                        shimmer = binding.adsContainer.nativeAdShimmer
                    )
                )
            )
        }
               dao = WorldClockDatabase.getDatabase(this).worldClockDao()

        CoroutineScope(Dispatchers.IO).launch {
            val existingCount = dao.getCount()  // 👈 check existing data

            if (existingCount == 0) {
                // generate and insert only if database is empty
                val clocks = getAllWorldClocksUsingZoneTabWithRelation()
                dao.insertAll(clocks)
                Log.d("DB", "Inserted ${clocks.size} clocks")
            } else {
                Log.d("DB", "Database already has data, skipping insert.")
            }

            withContext(Dispatchers.Main) {
                adapter = AddClockAdapter{ clock ->
                    lifecycleScope.launch {
                        dao.updateClock(clock)
                    }
                }
                binding.toolbar.title.text = getString(R.string.add_city)
                binding.toolbar.back.setOnClickListener {
                    AppEventLogger.trackButtonClick("AddClockScreen", "back", "navigate_back", "clock_flow")
                    finish()
                }
                loadClocks()

                binding.addNewClock.setOnClickListener {
                    AppEventLogger.trackButtonClick("AddClockScreen", "save", "save_city", "clock_flow")
                    val selectedItems = adapter.getSelectedItems()
                    if (selectedItems.isEmpty()) return@setOnClickListener

                    val navigate = {
                        lifecycleScope.launch {
                            selectedItems.forEach { dao.updateClock(it) }
                            finish()
                        }
                        Unit
                    }

                    val isPremium = PrefUtil(this@AddClockActivity).getBool("is_premium", false)
                        || getSharedPreferences(LifeTimePref, 0).getBoolean("premium", false)

                    if (isPremium) {
                        navigate()
                    } else {
                        safeShowInterstitialAction(
                            screenName = "AddClockScreen",
                            trigger = "save",
                            noCounterNeeded = false,
                            afterAd = navigate
                        )
                    }
                }


                binding.searchIcon.setOnClickListener {
                    AppEventLogger.trackButtonClick("AddClockScreen", "search", "toggle", "clock_search")
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
                                val imm =
                                    getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                                imm?.showSoftInput(binding.searchBar, InputMethodManager.SHOW_IMPLICIT)
                            }
                            .start()
                    } else {
                        binding.searchIcon.setImageResource(R.drawable.search)

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
                adapter.updateList(updated)
                binding.recycler.adapter = adapter
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
                            updated
                        } else {
                            updated.filter {
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
