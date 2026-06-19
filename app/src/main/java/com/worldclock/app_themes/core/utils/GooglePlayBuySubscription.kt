package com.worldclock.app_themes.core.utils


import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode.BILLING_UNAVAILABLE
import com.android.billingclient.api.BillingClient.BillingResponseCode.DEVELOPER_ERROR
import com.android.billingclient.api.BillingClient.BillingResponseCode.ERROR
import com.android.billingclient.api.BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED
import com.android.billingclient.api.BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED
import com.android.billingclient.api.BillingClient.BillingResponseCode.ITEM_NOT_OWNED
import com.android.billingclient.api.BillingClient.BillingResponseCode.ITEM_UNAVAILABLE
import com.android.billingclient.api.BillingClient.BillingResponseCode.OK
import com.android.billingclient.api.BillingClient.BillingResponseCode.SERVICE_DISCONNECTED
import com.android.billingclient.api.BillingClient.BillingResponseCode.SERVICE_TIMEOUT
import com.android.billingclient.api.BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE
import com.android.billingclient.api.BillingClient.BillingResponseCode.USER_CANCELED
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.QueryProductDetailsResult
import com.worldclock.app_themes.core.utils.AdsConstants.monthly_ad
import com.worldclock.app_themes.core.utils.AdsConstants.weekly_ad
import com.worldclock.app_themes.core.utils.AdsConstants.yearly_ad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty


class GooglePlayBuySubscription {
    companion object {
        val TAG = "GPlayBuySubscription"
        var purchasesInterface: SubscriptionPurchaseInterface? = null
        private var billingClient: BillingClient? = null
        private var mBillingResult: BillingResult? = null
        // Migrated from SkuDetails (v3) to ProductDetails (v5+)
        var mProductDetailsList: MutableList<ProductDetails>? = null

        var prefUtil: PrefUtil? = null

        fun initBillingClient(activity: Activity) {
            prefUtil = PrefUtil(activity)
            if (billingClient == null) {
                billingClient = BillingClient.newBuilder(activity)
                    .setListener(purchasesUpdatedListener)
                    .enablePendingPurchases(
                        PendingPurchasesParams.newBuilder()
                            .enableOneTimeProducts()
                            .build()
                    )
                    .build()
            }
        }

        fun makeGooglePlayConnectionRequest() {
            billingClient?.startConnection(billingClientStateListener)
        }

        private val purchasesUpdatedListener =
            PurchasesUpdatedListener { billingResult, purchases: MutableList<Purchase>? ->

                if (billingResult.responseCode == OK && purchases != null) {

                    Log.d(TAG, "purchasing:$purchases")

                    for (purchase in purchases) {
                        acknowledgePurchase(purchase)
                    }

                } else if (billingResult.responseCode == USER_CANCELED) {

                    Log.d(TAG, "Cancel:" + billingResult.responseCode)
                    purchasesInterface?.productPurchaseFailed()

                } else if (billingResult.responseCode == ITEM_ALREADY_OWNED) {

                    Log.d(TAG, "ITEM_ALREADY_OWNED:" + billingResult.responseCode)
                    purchasesInterface?.productPurchasedSuccessful()

                } else if (billingResult.responseCode == ITEM_NOT_OWNED) {
                    Log.d(TAG, "Error:" + billingResult.responseCode)
                    prefUtil?.setBool("is_premium", false)

                } else {
                    Log.d(TAG, "Error:" + billingResult.responseCode)
                    purchasesInterface?.productPurchaseFailed()
                }
            }

        private val billingClientStateListener = object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "On BillingServiceDisconnected")
            }

            override fun onBillingSetupFinished(p0: BillingResult) {
                Log.d(TAG, "On BillingSetupFinished")
                runBlocking {
                    queryProductDetails()
                }
            }
        }


        private fun handlePurchase(purchaseToken: String) {
            Log.e(TAG, "HANDLE PURCHASE")
            prefUtil?.setBool("is_premium", true)
        }


        suspend fun queryProductDetails() {
            checkPurchaseState()

            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(yearly_ad)
                    .setProductType(ProductType.SUBS)
                    .build(),
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(monthly_ad)
                    .setProductType(ProductType.SUBS)
                    .build(),
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(weekly_ad)
                    .setProductType(ProductType.SUBS)
                    .build()
            )

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            val result = withContext(Dispatchers.IO) {
                billingClient?.queryProductDetailsAsync(
                    params,
                    ProductDetailsResponseListener { billingResult: BillingResult, result: QueryProductDetailsResult ->
                        val productDetailsList = result.productDetailsList
                        Log.e(TAG, "queryProductDetails size=${productDetailsList.size}")
                        Log.e(TAG, "responseCode=${billingResult.responseCode}")
                        mBillingResult = billingResult
                        mProductDetailsList = ArrayList(productDetailsList)
                        mProductDetailsList?.forEach { prod ->
                            Log.e("TESTTAGFF", "product price=${prod.name}")
                        }
                    }
                )
            }

            Log.d(TAG, "Product Details query invoked: $result")
        }

        fun startSubscriptionProcess(context: Context, productDetails: ProductDetails) {
            val TAG = "BILLING PROCESS"
            val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken

            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .apply { offerToken?.let { setOfferToken(it) } }
                    .build()
            )

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            val responseCode = billingClient?.launchBillingFlow(
                context as Activity,
                billingFlowParams
            )?.responseCode

            Log.e(TAG, "starts Purchase Process: $responseCode")

            when (responseCode) {
                BILLING_UNAVAILABLE -> Log.e(TAG, "BILLING_UNAVAILABLE")
                DEVELOPER_ERROR     -> Log.e(TAG, "DEVELOPER_ERROR")
                ERROR               -> Log.e(TAG, "ERROR OCCURE")
                FEATURE_NOT_SUPPORTED -> Log.e(TAG, "FEATURE_NOT_SUPPORTED")
                ITEM_ALREADY_OWNED  -> Log.e(TAG, "ITEM_ALREADY_OWNED")
                ITEM_NOT_OWNED      -> Log.e(TAG, "ITEM_NOT_OWNED")
                ITEM_UNAVAILABLE    -> Log.e(TAG, "ITEM_UNAVAILABLE")
                OK                  -> Log.e(TAG, "OK")
                SERVICE_DISCONNECTED -> Log.e(TAG, "SERVICE_DISCONNECTED")
                SERVICE_TIMEOUT     -> Log.e(TAG, "SERVICE_TIMEOUT")
                SERVICE_UNAVAILABLE -> Log.e(TAG, "SERVICE_UNAVAILABLE")
                USER_CANCELED       -> Log.e(TAG, "USER_CANCELED")
            }
            purchasesInterface = context as SubscriptionPurchaseInterface
        }


        private fun acknowledgePurchase(purchase: Purchase) {

            if (mProductDetailsList == null) {
                if (purchase != null) {
                    acknowledge(purchase.purchaseToken)
                    return
                }
            }

            // v5+: ProductDetails no longer has a .type field — all items in the list are SUBS
            // so we just check purchaseState and isAcknowledged.
            if ((mProductDetailsList?.isNotEmpty() == true)
                && purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                && !purchase.isAcknowledged
            ) {
                acknowledge(purchase.purchaseToken)
            } else {
                if (purchase.isAcknowledged) {
                    prefUtil?.setBool("is_premium", true)
                }
            }

        }

        private fun acknowledge(purchaseToken: String) {

            Log.e(TAG, "acknowledgment")
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build()
            purchasesInterface?.productPurchasedSuccessful()
            billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                when (billingResult.responseCode) {
                    OK -> subscriptionOkayAndRemoveAds(purchaseToken)
                    else -> {
                        Log.d(TAG, "Failed to acknowledge $billingResult")
                        purchasesInterface?.productPurchaseFailed()
                    }
                }
            }
        }

        private fun subscriptionOkayAndRemoveAds(purchaseToken: String) {
            Log.e(TAG, "subscription Done")
            handlePurchase(purchaseToken)
        }

        fun checkPurchaseState() {
            billingClient?.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(ProductType.SUBS)
                    .build()
            ) { _: BillingResult, mutableList: List<Purchase> ->
                Log.e(TAG, "checkPurchaseState: $mutableList")
                if (mutableList.isEmpty())
                    prefUtil?.setBool("is_premium", false)

                for (item in mutableList) {
                    if (item.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        if (!item.isAcknowledged) {
                            acknowledge(item.purchaseToken)
                        } else {
                            handlePurchase(item.purchaseToken)
                        }
                    }
                }
            }
        }
    }
}