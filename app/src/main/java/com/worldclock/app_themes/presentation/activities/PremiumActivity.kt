package com.worldclock.app_themes.presentation.activities

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
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.QueryProductDetailsResult
import com.worldclock.app_themes.R
import com.worldclock.app_themes.databinding.ActivityPremiumBinding
import com.worldclock.app_themes.core.utils.AdsConstants.LifeTimePref
import com.worldclock.app_themes.core.utils.AdsConstants.monthly_ad
import com.worldclock.app_themes.core.utils.AdsConstants.weekly_ad
import com.worldclock.app_themes.core.utils.AdsConstants.yearly_ad
import com.worldclock.app_themes.core.utils.GooglePlayBuySubscription
import com.worldclock.app_themes.core.utils.PrefUtil
import com.worldclock.app_themes.core.utils.SubscriptionBilling
import com.worldclock.app_themes.core.utils.SubscriptionPurchaseInterface
import com.worldclock.app_themes.core.utils.openLink
import com.worldclock.app_themes.ads.helpers.safeShowInterstitialAction
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
class PremiumActivity : BaseActivity(), SubscriptionPurchaseInterface {

    private val binding by lazy { ActivityPremiumBinding.inflate(layoutInflater) }
    private var purchasCount = 1
    private var isSplash = false
    private var billingClient: BillingClient? = null
    var productDetailsList: ArrayList<ProductDetails>? = ArrayList()
    var retryCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)
        AppEventLogger.trackScreenCreate(this, savedInstanceState, "PremiumScreen", "activity_lifecycle")

        isSplash = intent.getBooleanExtra("isSplash", false)



        if (GetFirebase.enable_on_demand_interstitial == 1){
            InterstitialAdManager.loadPremium(this, GetFirebase.adIdPremium_interstitial)
        }

        Handler(mainLooper).postDelayed({
            binding.back.visibility = View.VISIBLE
        }, 1400)

        // ── Default selection — Monthly (yearly button) selected on open ──────
        purchasCount = 1
        binding.yearly.setBackgroundResource(R.drawable.bg_black_line)
        binding.sixMonth.setBackgroundResource(R.drawable.bg_prem)
        binding.lifetime.setBackgroundResource(R.drawable.bg_prem)

        binding.termsAmpConditions.setOnClickListener {
            openLink("https://styleappsworld.wordpress.com/world-clock-_terms-conditions/")
        }
        binding.privacy.paintFlags = binding.privacy.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.termsAmpConditions.paintFlags =
            binding.termsAmpConditions.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.privacy.setOnClickListener {
            openLink("https://pps66.wordpress.com/2022/11/22/privacy-policy/")
        }
        binding.back.setOnClickListener {
            AppEventLogger.trackButtonClick("PremiumScreen", "back", "navigate_back", "premium_flow")

            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_top,"top",this,
                GetFirebase.adIdMainActivity_bannerTop, GetFirebase.adIdMainActivity_nativeTop)

            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_bottom,"bottom",this,
                GetFirebase.adIdMainActivity_bannerBottom, GetFirebase.adIdMainActivity_nativeBottom)



            InterstitialAdManager.showIfReady(
                this@PremiumActivity,
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

        onBackPressedDispatcher.addCallback(this@PremiumActivity){


            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_top,"top",this@PremiumActivity,
                GetFirebase.adIdMainActivity_bannerTop, GetFirebase.adIdMainActivity_nativeTop)

            PreloadController.loadAdInBannerPosition(GetFirebase.banner_ad_mainactivity_bottom,"bottom",this@PremiumActivity,
                GetFirebase.adIdMainActivity_bannerBottom, GetFirebase.adIdMainActivity_nativeBottom)



            InterstitialAdManager.showIfReady(
                this@PremiumActivity,
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

        binding.purchase.setOnClickListener {
            performPurchase()
        }

        binding.yearly.setOnClickListener {
            purchasCount = 1
            AppEventLogger.trackButtonClick("PremiumScreen", "select_yearly", "click", "premium_options")
            binding.yearly.setBackgroundResource(R.drawable.bg_black_line)
            binding.sixMonth.setBackgroundResource(R.drawable.bg_prem)
            binding.lifetime.setBackgroundResource(R.drawable.bg_prem)
            performPurchase()
        }
        binding.sixMonth.setOnClickListener {
            purchasCount = 0
            AppEventLogger.trackButtonClick("PremiumScreen", "select_monthly", "click", "premium_options")
            binding.yearly.setBackgroundResource(R.drawable.bg_prem)
            binding.sixMonth.setBackgroundResource(R.drawable.bg_black_line)
            binding.lifetime.setBackgroundResource(R.drawable.bg_prem)
            performPurchase()
        }
        binding.lifetime.setOnClickListener {
            purchasCount = 2
            AppEventLogger.trackButtonClick("PremiumScreen", "select_weekly", "click", "premium_options")
            binding.yearly.setBackgroundResource(R.drawable.bg_prem)
            binding.sixMonth.setBackgroundResource(R.drawable.bg_prem)
            binding.lifetime.setBackgroundResource(R.drawable.bg_black_line)
            performPurchase()
        }

        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .setListener { billingResult, list ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
                    for (purchase in list) verifySubPurchase(purchase)
                }
            }.build()
        establishConnection()

        GooglePlayBuySubscription.purchasesInterface = this
        Log.e("TAG", "onCreate0: ${SubscriptionBilling.productsDetailsList.isNullOrEmpty()}")

        if (!SubscriptionBilling.productsDetailsList.isNullOrEmpty()) {
            SubscriptionBilling.productsDetailsList?.let { list ->
                val map = list.associateBy { it.productId }
                map[yearly_ad]?.let { binding.lifetimePrice.text = getPrice(it) }
                map[monthly_ad]?.let { binding.yearlyPrice.text = getPrice(it) }
                map[weekly_ad]?.let { binding.sixMonthPrice.text = getPrice(it) }
            }
        } else {
            Log.e("TAG", "onCreate0: ELSE")
        }
    }

    private fun goNext() {
        val isPremium = PrefUtil(this).getBool("is_premium", false) ||
                getSharedPreferences(LifeTimePref, Context.MODE_PRIVATE).getBoolean("premium", false)

        val proceed = {
            if (isSplash) {
                startActivity(getNextScreenIntent(this, "premium"))
                finish()
            } else {
                finish()
            }
        }

        if (isPremium) {
            proceed()
        } else {
            proceed()

        }
    }

    fun getNextScreenIntent(context: Context, currentScreen: String): Intent {
        val isFirstLaunch = !context.getSharedPreferences(AdsConstants.PrefsName, Context.MODE_PRIVATE)
            .getBoolean(AdsConstants.isFirstTime, false)

        val isPremium = PrefUtil(context).getBool("is_premium", false)
                || context.getSharedPreferences(AdsConstants.LifeTimePref, 0).getBoolean("premium", false)



        return Intent(context, MainActivity::class.java)

    }


    private fun getPrice(prod: ProductDetails): String {
        val offer = prod.subscriptionOfferDetails?.firstOrNull()
        val phase = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()
        return "${phase?.formattedPrice} ${phase?.priceCurrencyCode}"
    }

    private fun performPurchase() {
        val selectedId = when (purchasCount) {
            0 -> monthly_ad
            1 -> yearly_ad
            2 -> weekly_ad
            else -> monthly_ad
        }
        AppEventLogger.trackButtonClick("PremiumScreen", "purchase", "start_purchase", "premium_flow")
        val product = productDetailsList?.find { it.productId == selectedId }
        product?.let { launchPurchaseFlow(it) }
    }

    override fun productPurchasedSuccessful() {
        PrefUtil(this).setBool("is_premium", true)
        PrefUtil.setPremium(this, true)
        Toast.makeText(this, getString(R.string.subscribed_successfully), Toast.LENGTH_SHORT).show()
        goNext()
    }

    override fun productPurchaseFailed() {
        Toast.makeText(this, getString(R.string.subscription_failed), Toast.LENGTH_LONG).show()
    }

    private fun establishConnection() {
        val client = billingClient ?: return
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) getProducts()
            }

            override fun onBillingServiceDisconnected() {
                if (retryCount <= 3) establishConnection()
                retryCount++
            }
        })
    }

    private fun verifySubPurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken).build()
        billingClient?.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                PrefUtil(this).setBool("is_premium", true)
                PrefUtil.setPremium(this, true)
                runOnUiThread {
                    Toast.makeText(this, getString(R.string.subscribed_successfully), Toast.LENGTH_SHORT).show()
                    goNext()
                }
            }
        }
    }

    private fun getProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(yearly_ad).setProductType(BillingClient.ProductType.SUBS).build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(monthly_ad).setProductType(BillingClient.ProductType.SUBS).build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(weekly_ad).setProductType(BillingClient.ProductType.SUBS).build()
        )
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()
        billingClient?.queryProductDetailsAsync(params, ProductDetailsResponseListener { billingResult: BillingResult, result: QueryProductDetailsResult ->
            val prodDetailsList = result.productDetailsList
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                prodDetailsList.forEach { prod ->
                    Log.d("Hassan", "getProducts: ${prod.productId}")
                    productDetailsList?.add(prod)
                }
                runOnUiThread {
                    val map = prodDetailsList.associateBy { prod -> prod.productId }
                    map[yearly_ad]?.let { prod -> binding.lifetimePrice.text = getPrice(prod) }
                    map[monthly_ad]?.let { prod -> binding.yearlyPrice.text = getPrice(prod) }
                    map[weekly_ad]?.let { prod -> binding.sixMonthPrice.text = getPrice(prod) }
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    "Error ${billingResult.debugMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun launchPurchaseFlow(productDetails: ProductDetails) {
        val offerToken = productDetails.subscriptionOfferDetails
            ?.firstOrNull()
            ?.offerToken
        if (offerToken.isNullOrBlank()) {
            Toast.makeText(this, getString(R.string.subscription_failed), Toast.LENGTH_SHORT).show()
            return
        }
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList).build()
        billingClient?.launchBillingFlow(this, billingFlowParams)
    }

    override fun onResume() {
        super.onResume()
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { billingResult, list ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in list) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                        verifySubPurchase(purchase)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        AppEventLogger.trackScreenDestroy(this, "PremiumScreen")
        super.onDestroy()
    }
}
