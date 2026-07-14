package com.worldclock.app_themes.presentation.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.worldclock.app_themes.BuildConfig
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.perf.FirebasePerformance
import com.worldclock.app_themes.ads.managers.facebook.FbAdInitializer
import com.worldclock.app_themes.ads.preload.AppOpenAdManager
import com.worldclock.app_themes.ads.preload.AppOpenScreen
import com.worldclock.app_themes.ads.preload.InterstitialAdManager
import com.worldclock.app_themes.ads.utils.GetFirebase
import com.worldclock.app_themes.ads.utils.Utils
import com.worldclock.app_themes.core.utils.AdsConstants.LifeTimePref
import com.worldclock.app_themes.core.utils.BillingUtilsIAP
import com.worldclock.app_themes.core.utils.PrefUtil
import com.worldclock.app_themes.core.utils.RemoteConfig

import com.worldclock.app_themes.core.utils.SubscriptionBilling
import com.zeugmasolutions.localehelper.LocaleAwareApplication
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import kotlin.let

//@HiltAndroidApp
//class MyApplication : LocaleAwareApplication(), LifecycleObserver,
//    Application.ActivityLifecycleCallbacks  {
//    private var isAppopenRemote = false
//
//    //    private lateinit var appOpenAdManager: AppOpenAdManager
//    private var currentActivity: Activity? = null
//    private var AD_UNIT_ID = ""
//
//
//    @javax.inject.Inject
//    lateinit var appInitializer: com.worldclock.app_themes.ads.app.AppInitializer
//
//    @javax.inject.Inject
//    lateinit var appOpenAdLifecycleManager: com.worldclock.app_themes.ads.app.AppOpenAdLifecycleManager
//
//    companion object {
//        @SuppressLint("StaticFieldLeak")
//        lateinit var mContext: Context
//        var isResume = false
//        fun getAppContext(): Context {
//            return mContext.applicationContext
//        }
//    }
//
//    override fun onCreate() {
//        super<LocaleAwareApplication>.onCreate()
//        plantTimber()
//        FirebaseApp.initializeApp(this)
//        SubscriptionBilling(this)
//        mContext = this
//
//        BillingUtilsIAP(this)
//
//        runCatching {
//            FirebasePerformance.getInstance().isPerformanceCollectionEnabled = true
//        }.onFailure {
//            Log.w("MyApplication", "Firebase Performance init failed", it)
//        }
//
//        runCatching {
//            FirebaseMessaging.getInstance().isAutoInitEnabled = true
//        }.onFailure {
//            Log.w("MyApplication", "Firebase Messaging auto-init failed", it)
//        }
//
//        runCatching {
//            appInitializer.initialize()
//            appOpenAdLifecycleManager.attachToAppLifecycle(this)
//        }
//
//        runCatching {
//            val isPremium = PrefUtil(this).getBool("is_premium", false)
//                || getSharedPreferences(LifeTimePref, 0).getBoolean("premium", false)
//            if (!isPremium) {
//                FbAdInitializer.initialize(this)
//            }
//        }.onFailure {
//            Log.w("MyApplication", "Facebook Audience Network warmup failed", it)
//        }
//
//        registerActivityLifecycleCallbacks(this)
//
//        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
//    }
//    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
//    fun onMoveToForeground() {
//        Log.d("TAG", "onMoveToForeground: ${isResume} , ${mContext}")
//
//        if (isResume)
//            isResume = false
//    }
//
//    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
//        mContext = activity
//        Log.d("TAG", "onActivityCreated: ")
//
//    }
//
//    override fun onActivityStarted(activity: Activity) {
//        mContext = activity
//        Log.d("TAG", "onActivityStarted:1 ")
//
//    }
//
//    override fun onActivityResumed(activity: Activity) {
//        Log.d("TAG", "onActivityResumed:1 ")
//
//        mContext = activity
//    }
//
//    override fun onActivityPaused(activity: Activity) {
//        Log.d("TAG", "onActivityPaused:1 ")
//
//        mContext = activity
//    }
//
//    override fun onActivityStopped(activity: Activity) {
//        Log.d("TAG", "onActivityStopped:1 ")
//
//        mContext = activity
//        isResume = true
//    }
//
//    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
//        mContext = activity
//        Log.d("TAG", "onActivitySaveInstanceState:1 ")
//
//    }
//
//    override fun onActivityDestroyed(activity: Activity) {
//        Log.d("TAG", "onActivityDestroyed:1 ")
//
//        mContext = activity
//    }
//
//
//
//}
//
//private fun plantTimber() {
//    if (Timber.treeCount > 0) return
//    if (BuildConfig.DEBUG) {
//        Timber.plant(Timber.DebugTree())
//    } else {
//        Timber.plant(ReleaseAdDebugTree())
//    }
//}
//
//private class ReleaseAdDebugTree : Timber.Tree() {
//    private val allowedTags = setOf(
//        "NativeConfigTrace",
//        "SplashAdSequence",
//        "ConfigTrace",
//        "InterstitialTrace",
//        "AppOpenTrace"
//    )
//
//    override fun isLoggable(tag: String?, priority: Int): Boolean {
//        return priority >= Log.WARN || tag in allowedTags
//    }
//
//    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
//        if (!isLoggable(tag, priority)) return
//        val safeTag = tag ?: "WorldClock"
//        if (t != null) {
//            Log.println(priority, safeTag, "$message\n${Log.getStackTraceString(t)}")
//        } else {
//            Log.println(priority, safeTag, message)
//        }
//    }
//}

@HiltAndroidApp
class MyApplication : LocaleAwareApplication(),Application.ActivityLifecycleCallbacks,
    LifecycleObserver {

    private var currentActivity: Activity? = null
    var appOpenAdManager: AppOpenAdManager = AppOpenAdManager()


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

        BillingUtilsIAP(this)
        mContext = this

        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

    }


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        Handler().postDelayed(object : Runnable {
            override fun run() {
                val activityName = currentActivity?.localClassName?.lowercase().orEmpty()
                if (InterstitialAdManager.isAdVisible) {
                    return
                }
                val isSplashActivity = activityName.contains("splash")



                when {
                    isSplashActivity && GetFirebase.open_ad_from_background_splash -> {
                        currentActivity?.let {
                            appOpenAdManager.showIfAvailable(
                                it,
                                AppOpenScreen.BACKGROUND,
                                true,
                                Utils.isPremium,
                                true,
                                {

                                },
                                {

                                })
                        }
                    }

                    isSplashActivity && !GetFirebase.open_ad_from_background_splash -> {
                        //
                    }

                    !isSplashActivity && GetFirebase.open_ad_from_background -> {


                        currentActivity?.let {
                            appOpenAdManager.showIfAvailable(
                                it,
                                AppOpenScreen.BACKGROUND,
                                true,
                                Utils.isPremium,
                                true,
                                {

                                },
                                {

                                })
                        }
                    }
                }

            }

        }, 250)

    }

    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?
    ) {

    }

    override fun onActivityDestroyed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle
    ) {

    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityStopped(activity: Activity) {

    }

}
