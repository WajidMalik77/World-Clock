package com.worldclock.app_themes.presentation.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsResult
import com.worldclock.app_themes.R
import com.worldclock.app_themes.databinding.ActivityPurchaseBinding
import com.worldclock.app_themes.core.utils.AdsConstants.LifeTimePref
import com.worldclock.app_themes.core.utils.BillingUtilsIAP
import com.worldclock.app_themes.core.utils.PrefUtil
import com.worldclock.app_themes.ads.helpers.safeShowInterstitialAction
import com.worldclock.app_themes.core.utils.openPrivacyPolicy
import com.worldclock.app_themes.core.analytics.AppEventLogger
import dagger.hilt.android.EntryPointAccessors
import com.worldclock.app_themes.ads.di.AdConfigEntryPoint
import com.worldclock.app_themes.ads.preload.AdLoadMode
import com.worldclock.app_themes.ads.preload.InterstitialAdManager
import com.worldclock.app_themes.ads.preload.InterstitialScreen
import com.worldclock.app_themes.ads.preload.PreloadController
import com.worldclock.app_themes.ads.utils.GetFirebase
import com.worldclock.app_themes.ads.utils.Utils
import com.worldclock.app_themes.core.utils.AdsConstants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActivityPurchase : BaseActivity() {
    private val binding by lazy {
        ActivityPurchaseBinding.inflate(layoutInflater)
    }
    private var isSplash = false
    private var billingClient: BillingClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)
        AppEventLogger.trackScreenCreate(this, savedInstanceState, "PurchaseScreen", "activity_lifecycle")
        isSplash = intent.getBooleanExtra("isSplash", false)
        Handler(mainLooper).postDelayed({
            binding.back.visibility = View.VISIBLE
        }, 1400)


        if (GetFirebase.enable_on_demand_interstitial == 1){
            InterstitialAdManager.loadPremium(this, GetFirebase.adIdPremium_interstitial)
        }


        binding.cancelButton.setOnClickListener {
            AppEventLogger.trackButtonClick("PurchaseScreen", "cancel", "continue_with_ads", "premium_flow")
            val isPremium = PrefUtil(this).getBool("is_premium", false)
                || getSharedPreferences(LifeTimePref, 0).getBoolean("premium", false)
            if (isPremium) {
                goNext()
            } else {

                PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_top,"top",this,
                    GetFirebase.adIdMainActivity_bannerTop, GetFirebase.adIdMainActivity_nativeTop)

                PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_bottom,"bottom",this,
                    GetFirebase.adIdMainActivity_bannerBottom, GetFirebase.adIdMainActivity_nativeBottom)


                InterstitialAdManager.showIfReady(
                    this@ActivityPurchase,
                    InterstitialScreen.PREMIUM,
                    GetFirebase.adIdOther_interstitial,
                    if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                    GetFirebase.transition_PremiumBack,
                    GetFirebase.counter_interval,
                    Utils.isPremium,
                    GetFirebase.enable_interstitial_ads,
                    {
                        goNext()
                    },
                    {
                        goNext()
                    })
            }
        }
        binding.privacy.paintFlags = binding.privacy.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.termsAmpConditions.paintFlags =
            binding.termsAmpConditions.paintFlags or Paint.UNDERLINE_TEXT_FLAG
 
        binding.termsAmpConditions.setOnClickListener {
            openPrivacyPolicy("https://styleappsworld.wordpress.com/world-clock-_terms-conditions/")
        }
        binding.privacy.setOnClickListener {
            openPrivacyPolicy("https://styleappsworld.wordpress.com/world-clock-privacy-policy/")
        }


        onBackPressedDispatcher.addCallback(this@ActivityPurchase){

            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_top,"top",this@ActivityPurchase,
                GetFirebase.adIdMainActivity_bannerTop, GetFirebase.adIdMainActivity_nativeTop)

            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_bottom,"bottom",this@ActivityPurchase,
                GetFirebase.adIdMainActivity_bannerBottom, GetFirebase.adIdMainActivity_nativeBottom)



            InterstitialAdManager.showIfReady(
                this@ActivityPurchase,
                InterstitialScreen.PREMIUM,
                GetFirebase.adIdOther_interstitial,
                if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                GetFirebase.transition_PremiumBack,
                GetFirebase.counter_interval,
                Utils.isPremium,
                GetFirebase.enable_interstitial_ads,
                {
                    goNext()
                },
                {
                    goNext()
                })
        }


        binding.back.setOnClickListener {
            AppEventLogger.trackButtonClick("PurchaseScreen", "back", "navigate_back", "premium_flow")
            val isPremium = PrefUtil(this).getBool("is_premium", false)
                || getSharedPreferences(LifeTimePref, 0).getBoolean("premium", false)
            if (isPremium) {
                goNext()
            } else {
                PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_top,"top",this,
                    GetFirebase.adIdMainActivity_bannerTop, GetFirebase.adIdMainActivity_nativeTop)

                PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_bottom,"bottom",this,
                    GetFirebase.adIdMainActivity_bannerBottom, GetFirebase.adIdMainActivity_nativeBottom)


                InterstitialAdManager.showIfReady(
                    this@ActivityPurchase,
                    InterstitialScreen.PREMIUM,
                    GetFirebase.adIdOther_interstitial,
                    if (GetFirebase.enable_on_demand_interstitial == 0) AdLoadMode.ON_DEMAND else AdLoadMode.PRELOADED,
                    GetFirebase.transition_PremiumBack,
                    GetFirebase.counter_interval,
                    Utils.isPremium,
                    GetFirebase.enable_interstitial_ads,
                    {
                        goNext()
                    },
                    {
                        goNext()
                    })
            }
        }

        binding.purchase.setOnClickListener {
            AppEventLogger.trackButtonClick("PurchaseScreen", "purchase", "start_purchase", "premium_flow")
            BillingUtilsIAP(this)
                .purchase(
                    this,
                    BillingUtilsIAP.LIFETIME
                )

        }
        billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    queryProductDetails()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to Google Play by calling the startConnection() method.
            }
        })
    }


    val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    handleLifetimePurchase(purchase)
                }
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d("TAG", "Purchase canceled")
        } else {
            Log.d("TAG", "Purchase error: ${billingResult.debugMessage}")
        }
    }

    private fun handleLifetimePurchase(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient?.acknowledgePurchase(params) { result ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    setPremiumAndNavigate()
                }
            }
        } else {
            setPremiumAndNavigate()
        }
    }

    private fun setPremiumAndNavigate() {
        PrefUtil(this).setBool("is_premium", true)
        PrefUtil.setPremium(this, true)
        runOnUiThread {
            Toast.makeText(this, getString(R.string.subscribed_successfully), Toast.LENGTH_SHORT).show()
            goNext()
        }
    }

    @SuppressLint("SetTextI18n")
    fun queryProductDetails() {
        Log.e("TESTTAG", "queryProductDetails")

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(BillingUtilsIAP.LIFETIME)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(
            params
        ) { billingResult: BillingResult, result: QueryProductDetailsResult ->
            Log.e("TESTTAG", "billingResult ")
            val productDetailsList = result.productDetailsList
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.e("TESTTAG", "productDetailsList size=${productDetailsList.size}")
                for (productDetails in productDetailsList) {
                    Log.e("TESTTAG", "productDetails: ${productDetails.name}")
                    val oneTimePurchaseOfferDetails = productDetails.oneTimePurchaseOfferDetails
                    runOnUiThread {
                        binding.price.text =
                            "${oneTimePurchaseOfferDetails?.formattedPrice ?: ""} ${getString(R.string.for_lifetime)}"
                    }
                }
            } else {
                Log.d("TAG", "Failed to query product details: ${billingResult.debugMessage}")
            }
        }
    }

    private fun goNext() {
        if (isSplash) {
            startActivity(getNextScreenIntent(this, "premium"))
            finish()
        } else {
            finish()
        }
    }

    fun getNextScreenIntent(context: Context, currentScreen: String): Intent {
        val isFirstLaunch = !context.getSharedPreferences(AdsConstants.PrefsName, Context.MODE_PRIVATE)
            .getBoolean(AdsConstants.isFirstTime, false)

        val isPremium = PrefUtil(context).getBool("is_premium", false)
                || context.getSharedPreferences(AdsConstants.LifeTimePref, 0).getBoolean("premium", false)



        return Intent(context, MainActivity::class.java)

    }


    override fun onDestroy() {
        AppEventLogger.trackScreenDestroy(this, "PurchaseScreen")
        super.onDestroy()
    }
}
