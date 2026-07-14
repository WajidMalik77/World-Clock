package com.worldclock.app_themes.presentation.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.worldclock.app_themes.R
import com.worldclock.app_themes.presentation.adapter.SleepSoundAdapter
import com.worldclock.app_themes.databinding.ActivitySleepSoundBinding
import com.worldclock.app_themes.presentation.viewmodel.SleepSoundViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.worldclock.app_themes.ads.helpers.ui.NativeAdOrchestrator
import com.worldclock.app_themes.ads.helpers.models.NativeAdConfig
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
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
class SleepSoundActivity : BaseActivity() {
    @Inject
    lateinit var bannerAdOrchestrator: com.worldclock.app_themes.ads.helpers.ui.BannerAdOrchestrator


    private val binding by lazy { ActivitySleepSoundBinding.inflate(layoutInflater) }

    @Inject
    lateinit var nativeAdOrchestrator: NativeAdOrchestrator
    private val viewModel: SleepSoundViewModel by viewModels()
    private lateinit var adapter: SleepSoundAdapter

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(this, "Storage permission needed for download", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppEventLogger.trackScreenCreate(this, savedInstanceState, "SleepSoundScreen", "activity_lifecycle")
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)


        binding.toolbar.back.setOnClickListener {

            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_top,"top",this@SleepSoundActivity,
                GetFirebase.adIdMainActivity_bannerTop, GetFirebase.adIdMainActivity_nativeTop)

            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_bottom,"bottom",this@SleepSoundActivity,
                GetFirebase.adIdMainActivity_bannerBottom, GetFirebase.adIdMainActivity_nativeBottom)




            AppEventLogger.trackButtonClick("SleepSoundScreen", "back", "navigate_back", "sleep_sound_flow")
            InterstitialAdManager.showIfReady(
                this@SleepSoundActivity,
                InterstitialScreen.OTHER,
                GetFirebase.adIdOther_interstitial,
                if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                GetFirebase.transition_SleepSoundBack,
                GetFirebase.counter_interval,
                Utils.isPremium,
                GetFirebase.enable_interstitial_ads,
                {
                    startActivity(Intent(this@SleepSoundActivity, MainActivity::class.java))
                    finish()
                },
                {
                    startActivity(Intent(this@SleepSoundActivity, MainActivity::class.java))
                    finish()
                })
        }
        binding.toolbar.title.text = getString(R.string.sleep_sound)

        onBackPressedDispatcher.addCallback(this@SleepSoundActivity){

            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_top,"top",this@SleepSoundActivity,
                GetFirebase.adIdMainActivity_bannerTop, GetFirebase.adIdMainActivity_nativeTop)

            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_bottom,"bottom",this@SleepSoundActivity,
                GetFirebase.adIdMainActivity_bannerBottom, GetFirebase.adIdMainActivity_nativeBottom)


            InterstitialAdManager.showIfReady(
                this@SleepSoundActivity,
                InterstitialScreen.OTHER,
                GetFirebase.adIdOther_interstitial,
                if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                GetFirebase.transition_SleepSoundBack,
                GetFirebase.counter_interval,
                Utils.isPremium,
                GetFirebase.enable_interstitial_ads,
                {
                    startActivity(Intent(this@SleepSoundActivity, MainActivity::class.java))
                    finish()
                },
                {
                    startActivity(Intent(this@SleepSoundActivity, MainActivity::class.java))
                    finish()
                })
        }

        PreloadController.observeBanner(this,
            BannerPreload.adBannerTopLiveData,
            binding.bannerContainer.bannerTopContainer,binding.bannerContainer.adVeiwTop,binding.bannerContainer.adTextAdvertisementTop,
            GetFirebase.banner_ad_sleepsoundactivity_top,"top",
            window,
            NativePreload.adNativeTopLiveData){

        }

        PreloadController.observeBanner(this,
            BannerPreload.adBannerBottomLiveData,
            binding.adsContainer.bannerBottomContainer,binding.adsContainer.adVeiwBottom,binding.adsContainer.adTextAdvertisementBottom,
            GetFirebase.banner_ad_sleepsoundactivity_bottom,"bottom",
            window,
            NativePreload.adNativeBottomLiveData){

        }

        checkStoragePermission()
        setupRecycler()
        observeViewModel()
        setupSearch()
    }

    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private fun setupRecycler() {
        adapter = SleepSoundAdapter(
            onPlay = { sound ->
                AppEventLogger.trackButtonClick("SleepSoundScreen", "play_sound", "navigate", "sleep_sound_list")
                val navigate = {
                    startActivity(
                        Intent(this, PlaySoundActivity::class.java).apply {
                            putExtra("sound_name", sound.name)
                            putExtra("preview_url", sound.previewUrl)
                            putExtra("thumbnail_res", R.drawable.ic_sleep_placeholder)
                        }
                    )
                }

                val isPremium = PrefUtil(this).getBool("is_premium", false)
                    || getSharedPreferences(LifeTimePref, 0).getBoolean("premium", false)

                if (isPremium) {
                    navigate()
                } else {

                    PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_playsoundactivity_top,"top",this@SleepSoundActivity,
                        GetFirebase.adIdPlaySound_bannerTop, GetFirebase.adIdPlaySound_nativeTop)

                    PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_playsoundactivity_bottom,"bottom",this@SleepSoundActivity,
                        GetFirebase.adIdPlaySound_bannerBottom, GetFirebase.adIdPlaySound_nativeBottom)




                    InterstitialAdManager.showIfReady(
                        this,
                        InterstitialScreen.OTHER,
                        GetFirebase.adIdOther_interstitial,
                        if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                        GetFirebase.transition_SleepSoundForward,
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
        )
        binding.rvSleepSounds.adapter = adapter

        binding.rvSleepSounds.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) return

                val layoutManager = recyclerView.layoutManager as? GridLayoutManager ?: return
                val totalItems = layoutManager.itemCount
                val lastVisible = layoutManager.findLastVisibleItemPosition()

                if (lastVisible >= totalItems - 4) {
                    viewModel.loadMore()
                }
            }
        })
    }

    private fun observeViewModel() {
        viewModel.sounds.observe(this) { sounds ->
            adapter.submitList(sounds)
        }

        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.isLoadingMore.observe(this) { loadingMore ->
            binding.progressBar.visibility = if (loadingMore) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun setupSearch() {
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                AppEventLogger.trackButtonClick("SleepSoundScreen", "search", "submit", "sleep_sound_search")
                viewModel.search(binding.etSearch.text.toString())
                true
            } else false
        }
    }

    override fun onDestroy() {
        AppEventLogger.trackScreenDestroy(this, "SleepSoundScreen")
        super.onDestroy()
    }
}
