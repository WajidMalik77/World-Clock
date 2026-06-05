package com.worldclock.app_themes.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.SkuDetailsResponseListener
import com.worldclock.app_themes.activities.MainActivity
import com.worldclock.app_themes.R
import com.worldclock.app_themes.databinding.ActivityPurchaseBinding
import com.worldclock.app_themes.utils.AdsConstants.LifeTimePref
import com.worldclock.app_themes.utils.AdsConstants.PrefsName
import com.worldclock.app_themes.utils.AdsConstants.isFirstTime
import com.worldclock.app_themes.utils.AdsConstants.monthly_ad
import com.worldclock.app_themes.utils.AdsConstants.weekly_ad
import com.worldclock.app_themes.utils.AdsConstants.yearly_ad
import com.worldclock.app_themes.utils.BillingUtilsIAP
import com.worldclock.app_themes.utils.GooglePlayBuySubscription
import com.worldclock.app_themes.utils.PrefUtil
import com.worldclock.app_themes.utils.SubscriptionBilling
import com.worldclock.app_themes.utils.SubscriptionPurchaseInterface
import com.worldclock.app_themes.utils.openLink
import com.worldclock.app_themes.utils.openPrivacyPolicy
import java.util.Currency
import kotlin.collections.forEach
import kotlin.or

class ActivityPurchase : BaseActivity() {

    private val binding by lazy {
        ActivityPurchaseBinding.inflate(layoutInflater)
    }
    private var isSplash = false
    private var billingClient: BillingClient? = null

    //    private var mSharePrefHelper: SharedPreferencesClass? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        applyEdgeToEdgePadding(R.id.main)
        isSplash = intent.getBooleanExtra("isSplash", false)
        Handler(mainLooper).postDelayed({
            binding.back.visibility = View.VISIBLE
        }, 1400)
        binding.cancelButton.setOnClickListener {
            goNext()
        }
        binding.privacy.paintFlags = binding.privacy.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.termsAmpConditions.paintFlags =
            binding.termsAmpConditions.paintFlags or Paint.UNDERLINE_TEXT_FLAG

//        binding.privacyTv.setOnClickListener {
//            openPrivacyPolicy(this)
//        }
//        binding.termsTv.setOnClickListener {
//            openGooglePrivacy()
//        }
//        binding.cancelButton.setOnClickListener {
//            if (isN) {
//                val uri = Uri.parse("https://play.google.com/store/account/subscriptions")
//                val intent = Intent(Intent.ACTION_VIEW, uri)
//                startActivity(intent)
//            } else {
//                Toast.makeText(this, "no internet", Toast.LENGTH_SHORT).show()
//            }
//        }
        binding.termsAmpConditions.setOnClickListener {
            openPrivacyPolicy("https://styleappsworld.wordpress.com/world-clock-_terms-conditions/")
        }
        binding.privacy.setOnClickListener {
            openPrivacyPolicy("https://styleappsworld.wordpress.com/world-clock-privacy-policy/")
        }
        binding.back.setOnClickListener {
            goNext()
        }
//        mSharePrefHelper = SharedPreferencesClass(applicationContext)
//
//
//        mSharePrefHelper!!.setBooleanPreferences(
//            mSharePrefHelper!!.REMOVE_AD_ACTIVITY_OPEN, true
//        )

        binding.purchase.setOnClickListener {
            MyApplication.isResume = false
            BillingUtilsIAP(this)
                .purchase(
                    this,
                    BillingUtilsIAP.LIFETIME
                )

        }
        billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    querySkuDetails()
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
                Log.d("TAG", "skuDetails: list = ${purchase.orderId}")
                // Handle the purchase, acknowledge, consume, or process it as needed
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle user cancellation
            Log.d("TAG", "skuDetails: cancel")
        } else {
            // Handle other errors
            Log.d("TAG", "skuDetails: error")
        }
    }

    @SuppressLint("SetTextI18n")
    fun querySkuDetails() {
        val skuList: MutableList<String> = ArrayList()
        skuList.add(BillingUtilsIAP.LIFETIME)
        Log.e("TESTTAG", "querySkuDetails ")
        val params = SkuDetailsParams.newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.INAPP)
            .build()

        billingClient?.querySkuDetailsAsync(
            params,
            SkuDetailsResponseListener { billingResult, skuDetailsList ->
                Log.e("TESTTAG", "billingResult ")
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                    Log.e("TESTTAG", "skuDetailsList123 = ${skuDetailsList.size} ")
                    for (skuDetails in skuDetailsList) {
                        Log.e("TESTTAG", "skuDetails ")
                        val originalPrice =
                            skuDetails.originalPrice // The original price before any discount
                        val discountedPrice = skuDetails.price // The discounted price
                        val currencyCode = skuDetails.priceCurrencyCode
                        val currencySymbol =
                            Currency.getInstance(currencyCode).symbol // Gets the currency symbol based on the currency code

                        runOnUiThread {
                            binding.price.text =
                                "$discountedPrice ${getString(R.string.for_lifetime)}" // Assumes a TextView for discounted price
                        }

                    }
                } else {
                    Log.d("TAG", "Failed to query SKU details: ${billingResult.debugMessage}")
                }
            })
    }

    private fun goNext() {
        if (isSplash) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            finish()
        }
    }

    /* private fun goNext() {
         if (isSplash) {
             if (!isFinishing && !isDestroyed)
                 if (!getSharedPreferences(
                         PrefsName,
                         Context.MODE_PRIVATE
                     ).getBoolean(
                         isFirstTime,
                         false
                     )
                 ) {
 //                    preLoadShowInterstitial(Ispurchase_inter_ad_key, purchase_inter_ad_key) {
                     startActivity(
                         Intent(
                             this,
                             MainActivity::class.java
                         )
                     )
                     finish()

 //                }
                 } else {
 //                    showInterstitialWithoutCounter(Ispurchase_inter_ad_key, purchase_inter_ad_key) {
                     startActivity(
                         Intent(this, MainActivity::class.java)
                     )
                     finish()

                 }

 //                }

         } else
             finish()
     }*/
}