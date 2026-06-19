package com.worldclock.app_themes.ads.app

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.worldclock.app_themes.ads.utils.AdsPref
import com.worldclock.app_themes.ads.utils.Constants
import com.worldclock.app_themes.ads.utils.Constants.PRODUCT_LIFETIME
import com.worldclock.app_themes.core.utils.AdsConstants
import com.worldclock.app_themes.core.utils.PrefUtil
import timber.log.Timber
import java.util.concurrent.CountDownLatch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    private val context: Application,
    private val adsPref: AdsPref,
    private val premiumRepository: PremiumRepository
) {
    companion object {
        private const val TAG_BILLING_DIAG = "BillingAckDiag"
        private val PREMIUM_PRODUCTS = setOf(
            Constants.PRODUCT_WEEKLY,
            PRODUCT_LIFETIME,
            Constants.PRODUCT_MONTHLY,
            Constants.PRODUCT_YEARLY,
            // Alternate subscription IDs used in PremiumActivity flow.
            AdsConstants.weekly_ad,
            AdsConstants.monthly_ad,
            AdsConstants.yearly_ad
        )
    }

    private lateinit var billingClient: BillingClient
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _firstSyncCompleted = MutableStateFlow(false)
    /** Becomes true after the first successful queryAllPurchases reconciliation,
     *  so Splash / callers can wait for authoritative premium state before navigating. */
    val firstSyncCompleted: StateFlow<Boolean> = _firstSyncCompleted.asStateFlow()

    fun initialize() {
        // Set initial state from local storage
        val localPremiumStatus = adsPref.getIsPremiumStatus()
        premiumRepository.updatePremiumState(localPremiumStatus)

        // Setup billing client and sync with server
        setupBillingClient()
    }

    private fun setupBillingClient() {
        Timber.d("Billing: Initializing BillingClient")

        billingClient = BillingClient.newBuilder(context)
            .setListener { result, purchases -> handlePurchaseUpdate(result, purchases) }
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .build()

        connectBillingClient()
    }

    private fun connectBillingClient() {
        try {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    Timber.d("Billing: Setup finished - ${result.responseCode}")

                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        queryAllPurchases()
                        retryUnacknowledgedPurchases()
                    }
                }

                override fun onBillingServiceDisconnected() {
                    Timber.w("Billing: Service disconnected, retrying in 1s")
                    mainHandler.postDelayed({ connectBillingClient() }, 1000)
                }
            })
        } catch (e: SecurityException) {
            Timber.e(e, "Billing: SecurityException during connection (likely cross-user/work profile restriction)")
        } catch (e: Exception) {
            Timber.e(e, "Billing: Unexpected error during startConnection")
        }
    }

    fun queryAllPurchases() {
        val productTypes = listOf(
            BillingClient.ProductType.SUBS,
            BillingClient.ProductType.INAPP
        )

        val allPurchases = mutableListOf<Purchase>()
        val allOk = java.util.concurrent.atomic.AtomicBoolean(true)
        val countdown = CountDownLatch(productTypes.size)

        productTypes.forEach { type ->
            queryPurchasesByType(type) { purchases, ok ->
                synchronized(allPurchases) {
                    if (ok) allPurchases.addAll(purchases) else allOk.set(false)
                }
                countdown.countDown()
            }
        }

        // Reconcile premium only when BOTH queries succeed — a transient Play Billing
        // failure must not downgrade a paying user to free.
        CoroutineScope(Dispatchers.IO).launch {
            countdown.await()
            if (allOk.get()) {
                processPurchases(allPurchases)
                _firstSyncCompleted.value = true
            } else {
                Timber.w("$TAG_BILLING_DIAG Skipping reconciliation — at least one purchase query failed")
                // Unblock Splash even on failure — local cached state stays as-is.
                _firstSyncCompleted.value = true
            }
        }
    }

    private fun queryPurchasesByType(
        type: String,
        onComplete: (List<Purchase>, Boolean) -> Unit
    ) {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(type).build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                onComplete(purchases, true)
            } else {
                Timber.w("Query failed for $type: ${result.debugMessage}")
                onComplete(emptyList(), false)
            }
        }
    }

    private fun processPurchases(purchases: List<Purchase>) {
        val validPurchases = purchases.filter {
            it.purchaseState == Purchase.PurchaseState.PURCHASED
        }
        Timber.d("$TAG_BILLING_DIAG processPurchases total=${purchases.size} validPurchased=${validPurchases.size}")

        val purchasedProducts = mutableSetOf<String>()
        var hasLifetime = false

        validPurchases.forEach { purchase ->
            val productId = purchase.products.firstOrNull() ?: return@forEach
            Timber.d(
                "$TAG_BILLING_DIAG purchase productId=$productId acknowledged=${purchase.isAcknowledged} token=${purchase.purchaseToken.take(12)}..."
            )

            // Acknowledge if needed
            if (!purchase.isAcknowledged) {
                Timber.d("$TAG_BILLING_DIAG ack_required productId=$productId token=${purchase.purchaseToken.take(12)}...")
                acknowledgePurchase(purchase)
            } else {
                Timber.d("$TAG_BILLING_DIAG ack_already_done productId=$productId token=${purchase.purchaseToken.take(12)}...")
            }

            when (productId) {
                PRODUCT_LIFETIME -> {
                    hasLifetime = true
                    purchasedProducts.clear()
                    purchasedProducts.add(PRODUCT_LIFETIME)
                }
                in PREMIUM_PRODUCTS -> {
                    if (!hasLifetime) {
                        purchasedProducts.add(productId)
                    }
                }
            }
        }

        val isPremium = purchasedProducts.isNotEmpty()
        adsPref.setPurchasedProductsSet(purchasedProducts)
        adsPref.setIsPremiumStatus(isPremium)
        premiumRepository.updatePremiumState(isPremium)
        // Keep all premium flags in sync and hide ads immediately on current UI.
        PrefUtil.setPremium(context, isPremium)

        Timber.d("Purchases processed: $purchasedProducts, isPremium=$isPremium")
    }

    private fun handlePurchaseUpdate(result: BillingResult, purchases: List<Purchase>?) {
        Timber.d("Purchase update: ${result.responseCode}")

        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            processPurchases(purchases)
        }
    }

    private fun acknowledgePurchase(purchase: Purchase, attemptsLeft: Int = 3) {
        if (attemptsLeft <= 0) {
            Timber.e("Failed to acknowledge: ${purchase.products}")
            return
        }
        val productId = purchase.products.firstOrNull().orEmpty()
        Timber.d(
            "$TAG_BILLING_DIAG ack_attempt productId=$productId attemptsLeft=$attemptsLeft token=${purchase.purchaseToken.take(12)}..."
        )

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) { result ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                Timber.d(
                    "$TAG_BILLING_DIAG ack_success productId=$productId token=${purchase.purchaseToken.take(12)}... response=${result.responseCode}"
                )
            } else {
                Timber.w(
                    "$TAG_BILLING_DIAG ack_failed productId=$productId token=${purchase.purchaseToken.take(12)}... response=${result.responseCode} debug=${result.debugMessage}"
                )
                mainHandler.postDelayed({
                    acknowledgePurchase(purchase, attemptsLeft - 1)
                }, 2000)
            }
        }
    }

    private fun retryUnacknowledgedPurchases() {
        queryAllPurchases() // Reuses existing acknowledgment logic
    }

}
