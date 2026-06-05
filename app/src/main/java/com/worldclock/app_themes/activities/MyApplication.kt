package com.worldclock.app_themes.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.FirebaseApp
import com.worldclock.app_themes.utils.AdsConstants.LifeTimePref
import com.worldclock.app_themes.utils.BillingUtilsIAP
import com.worldclock.app_themes.utils.PrefUtil
import com.worldclock.app_themes.utils.RemoteConfig

import com.worldclock.app_themes.utils.SubscriptionBilling
import com.zeugmasolutions.localehelper.LocaleAwareApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.let

class MyApplication : LocaleAwareApplication(), LifecycleObserver,
    Application.ActivityLifecycleCallbacks  {
    private var isAppopenRemote = false

    //    private lateinit var appOpenAdManager: AppOpenAdManager
    private var currentActivity: Activity? = null
    private var AD_UNIT_ID = ""


    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var mContext: Context
        var isResume = false
        fun getAppContext(): Context {
            return mContext.applicationContext
        }
    }

    override fun onCreate() {
        super<LocaleAwareApplication>.onCreate()
        FirebaseApp.initializeApp(this)
        SubscriptionBilling(this)
        mContext = this

        BillingUtilsIAP(this)
//        ApplicationClass.Companion.mContext = this


//        SubscriptionBilling(this)

//        val backgroundScope = CoroutineScope(Dispatchers.IO)
//        backgroundScope.launch {
//            // Initialize the Google Mobile Ads SDK on a background thread.
//            MobileAds.initialize(this@MyApplication) {}
//        }
        registerActivityLifecycleCallbacks(this)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)


    }
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onMoveToForeground() {
        Log.d("TAG", "onMoveToForeground: ${isResume} , ${mContext}")

        if (isResume)
            isResume = false
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        mContext = activity
        Log.d("TAG", "onActivityCreated: ")

    }

    override fun onActivityStarted(activity: Activity) {
        mContext = activity
        Log.d("TAG", "onActivityStarted:1 ")

    }

    override fun onActivityResumed(activity: Activity) {
        Log.d("TAG", "onActivityResumed:1 ")

        mContext = activity
    }

    override fun onActivityPaused(activity: Activity) {
        Log.d("TAG", "onActivityPaused:1 ")

        mContext = activity
    }

    override fun onActivityStopped(activity: Activity) {
        Log.d("TAG", "onActivityStopped:1 ")

        mContext = activity
        isResume = true
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        mContext = activity
        Log.d("TAG", "onActivitySaveInstanceState:1 ")

    }

    override fun onActivityDestroyed(activity: Activity) {
        Log.d("TAG", "onActivityDestroyed:1 ")

        mContext = activity
    }



}