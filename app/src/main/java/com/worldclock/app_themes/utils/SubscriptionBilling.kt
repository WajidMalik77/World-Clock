package com.worldclock.app_themes.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsParams.Product
import com.android.billingclient.api.QueryPurchasesParams
import com.worldclock.app_themes.utils.AdsConstants.monthly_ad
import com.worldclock.app_themes.utils.AdsConstants.weekly_ad
import com.worldclock.app_themes.utils.AdsConstants.yearly_ad

import kotlin.collections.firstOrNull
import kotlin.collections.isNotEmpty
import kotlin.let


class SubscriptionBilling(private val context: Context) {

    init {
        initBilling()
    }

    private fun initBilling() {

        billingClient = BillingClient.newBuilder(context).enablePendingPurchases()
            .setListener { billingResult, list ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
                    for (purchase in list) {
                        verifySubPurchase(context, purchase)
                    }
                }
            }.build()


        makeConnection()
        Log.e("TESTAG", "Init Billing")
    }

    fun makeConnection() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // verifySubPurchase(context)
                    showProducts()
                }
            }

            override fun onBillingServiceDisconnected() {
//                establishConnection()
            }
        })
    }
    private fun showProducts() {
        val productList = listOf(
            Product.newBuilder()
                .setProductId(yearly_ad)
                .setProductType(ProductType.SUBS)
                .build(),
            Product.newBuilder()
                .setProductId(monthly_ad)
                .setProductType(ProductType.SUBS)
                .build(),
            Product.newBuilder()
                .setProductId(weekly_ad)
                .setProductType(ProductType.SUBS)
                .build(),

            )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productsDetailsList = productDetailsList ?: mutableListOf()
                productsDetailsList?.firstOrNull()?.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.let { pricingPhase ->
                    Log.e("TESTAG", "Price: ${pricingPhase.formattedPrice}")
                } ?: Log.e("TESTAG", "No pricing phase available")
            } else {
                Log.e("TESTAG", "Failed to retrieve product details: ${billingResult.debugMessage}")
            }
        }
    }
 /*   private fun showProducts() {
        val productList = listOf(
            Product.newBuilder()
                .setProductId("mothly_sub")
                .setProductType(ProductType.SUBS)
                .build(),
       *//*     Product.newBuilder()
                .setProductId("sixmonth_sub")
                .setProductType(ProductType.SUBS)
                .build(),*//*
            Product.newBuilder()
                .setProductId("yearly_sub")
                .setProductType(ProductType.SUBS)
                .build()
        )
        val skuList = ArrayList<String>()
        skuList.add("mothly_sub")
       // skuList.add("sixmonth_sub")
        skuList.add("yearly_sub")

        val params = QueryProductDetailsParams.newBuilder().setProductList(productList)
        val params1 = SkuDetailsParams.newBuilder()
        params1.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)
        billingClient?.queryProductDetailsAsync(params.build()) { billingResult, productDetailsList ->
            // Process the result
            productsDetailsList = productDetailsList

            Log.e(
                "TESTAG",
                "called${productDetailsList?.firstOrNull()?.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice}"
            )
        }
        billingClient?.querySkuDetailsAsync(params1.build()) { billingResult: BillingResult, skuDetailsList: MutableList<SkuDetails>? ->
            Log.e("TESTAG", "called sku ${skuDetailsList!!.size}")

        }
    }*/


    companion object {
        private var billingClient: BillingClient? = null
        var productsDetailsList: MutableList<ProductDetails>? = null

        fun launchPurchaseFlow(plan: Int, activity: Activity) {
            productsDetailsList?.size?.let {
                if (it > 0) {
                    val offerToken =
                        productsDetailsList?.get(plan)?.subscriptionOfferDetails?.get(0)?.offerToken
                    val productDetailsParamsList =
                        listOf(
                            BillingFlowParams.ProductDetailsParams.newBuilder()
                                .setProductDetails(productsDetailsList!![plan])
                                .setOfferToken(offerToken!!)
                                .build()
                        )
                    val billingFlowParams =
                        BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(productDetailsParamsList)
                            .build()

                    billingClient?.launchBillingFlow(activity, billingFlowParams)
                }
            }


        }

        fun verifySubPurchase(context: Context) {
            billingClient!!.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(ProductType.SUBS)
                    .build()
            ) { billingResult: BillingResult, list: List<Purchase> ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    for (purchase in list) {
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                            verifySubPurchase(context, purchase)
                        }
                    }
                    if (list.isNotEmpty()) {
                        PrefUtil.setPremiumString(list[0].products[0], context)
                    }
                }

            }

        }

        private fun verifySubPurchase(context: Context, purchase: Purchase) {
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient?.acknowledgePurchase(
                acknowledgePurchaseParams
            ) { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    PrefUtil.setPremium(context, true)
                  //  InAppPrefs(context).premium = true
                }
            }
        }

    }
}