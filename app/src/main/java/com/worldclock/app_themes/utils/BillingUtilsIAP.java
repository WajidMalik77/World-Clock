package com.worldclock.app_themes.utils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class BillingUtilsIAP
{


    public static final String LIFETIME = "lifetime_subscription";
    //   public static final String LIFETIME = "android.test.purchased";
    private static BillingClient billingClient;
    private static boolean isBillingReady;
    private static boolean isPremium;
    private static PurchasesUpdatedListener purchaseUpdateListener;

    public BillingUtilsIAP(final Context context) {
        InAppPrefs instance = InAppPrefs.getInstance(context);
        isPremium = instance.getPremium();

        if (billingClient == null) {
            purchaseUpdateListener = new PurchasesUpdatedListener() {
                @Override
                public void onPurchasesUpdated(@NotNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
                    // Timber.i("getOldPurchases: in Listener");
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                        for (Purchase purchase : purchases) {
                            handlePurchase(context, purchase);
                        }
                    } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                        //    Timber.i("getOldPurchases: User Cancelled");
                    } else {
                        //Timber.i("getOldPurchases: Other Error");
                    }
                }


            };
            BillingClient.Builder newBuilder = BillingClient.newBuilder(context);
            billingClient = newBuilder.setListener(purchaseUpdateListener).enablePendingPurchases().build();
            setupConnection(context);
        }
    }

    private void setupConnection(Context context) {

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NotNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    setBillingReady(true);
                    //Timber.i("onBillingServiceDisconnected: Setup Connection");
                    getOldPurchases(context);
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                //    Timber.i("onBillingServiceDisconnected: Setup Connection Failed");
                setBillingReady(false);
            }
        });
    }

    public void purchase(Activity activity, String str) {

        if (isBillingReady) {
            ArrayList arrayList = new ArrayList();
            arrayList.add(str);
            SkuDetailsParams.Builder newBuilder = SkuDetailsParams.newBuilder();
            newBuilder.setSkusList(arrayList).setType(BillingClient.SkuType.INAPP);
            BillingClient billingClient2 = billingClient;
            billingClient2.querySkuDetailsAsync(newBuilder.build(), new SkuDetailsResponseListener() {
                @Override
                public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                    if (skuDetailsList != null && (!skuDetailsList.isEmpty())) {
                        BillingFlowParams.Builder newBuilder = BillingFlowParams.newBuilder();
                        Object obj = skuDetailsList.get(0);

                        BillingFlowParams build = newBuilder.setSkuDetails((SkuDetails) obj).build();

                        BillingResult launchBillingFlow = getBillingClient().launchBillingFlow(activity, build);
                        if (launchBillingFlow.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                            //    Timber.i("getOldPurchases: Please try Again Later1");
                        }
                    }
                }
            });
            return;
        }
        //  Timber.i("getOldPurchases: Please try Again Later2");
        setupConnection(activity);
    }

    private void handlePurchase(Context context, Purchase purchase) {
        AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(@NotNull BillingResult billingResult) {

            }
        };
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            for (String skus : purchase.getSkus()) {
                if (skus.equals(LIFETIME)) {
                    isPremium = true;
                    new InAppPrefs(context).setPremium(true);
                    InAppPrefs.getInstance(context).setPremium(true);
                    // Set ads purchased flag in InterstitialMicAds class

                    Log.i("TAG", "handlePurchase: premium");
                }
            }

            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams build = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
                billingClient.acknowledgePurchase(build, acknowledgePurchaseResponseListener);
            }
        }
    }

    public void getOldPurchases(Context context) {

        billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP, new PurchasesResponseListener() {
            @Override
            public void onQueryPurchasesResponse(@NonNull BillingResult billingResult, @NonNull List<Purchase> list) {

                for (Purchase purchase : list) {

                    for (String skus : purchase.getSkus()) {
                        if (skus.equals(LIFETIME)) {
                            isPremium = true;
                            InAppPrefs instance = InAppPrefs.getInstance(context);
                            instance.setPremium(true);
                            Log.i("TAG", "getOldPurchases: premium");
                        }
                    }
                    Log.i("TAG", "handlePurchase: premium");
                }
            }
        });

    }


    public BillingClient getBillingClient() {
        return billingClient;
    }

    public void setBillingClient(BillingClient client) {
        client = client;
    }

    public PurchasesUpdatedListener getPurchaseUpdateListener() {
        return purchaseUpdateListener;
    }

    public void setPurchaseUpdateListener(PurchasesUpdatedListener purchasesUpdatedListener) {
        purchaseUpdateListener = purchasesUpdatedListener;
    }

    public boolean isBillingReady() {
        return isBillingReady;
    }

    public static void setBillingReady(boolean z) {
        isBillingReady = z;
    }

    public static boolean isPremium() {
        return isPremium;
    }

    public final void setPremium(boolean z) {
        isPremium = z;
    }


}
