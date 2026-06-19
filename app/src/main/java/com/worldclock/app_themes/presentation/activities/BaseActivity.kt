package com.worldclock.app_themes.presentation.activities

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.worldclock.app_themes.R
import com.zeugmasolutions.localehelper.LocaleHelper
import com.zeugmasolutions.localehelper.LocaleHelperActivityDelegate
import com.zeugmasolutions.localehelper.LocaleHelperActivityDelegateImpl

import java.util.Locale

open class BaseActivity : AppCompatActivity() {
    private val localeDelegate: LocaleHelperActivityDelegate = LocaleHelperActivityDelegateImpl()

    override fun getDelegate() = localeDelegate.getAppCompatDelegate(super.getDelegate())

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(localeDelegate.attachBaseContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            // Enable drawing behind status/navigation bars
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // Set your background color for the status bar
            window.statusBarColor = ContextCompat.getColor(this, R.color.white)

            // Make status bar icons WHITE
            WindowInsetsControllerCompat(window, window.decorView).apply {
                isAppearanceLightStatusBars = false
            }

            localeDelegate.onCreate(this)

        } catch (_: ClassCastException) {

        }


    }
    protected fun applyEdgeToEdgePadding(rootViewId: Int) {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(rootViewId)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    override fun onResume() {
        try {
            super.onResume()


            localeDelegate.onResumed(this)
        } catch (_: ClassCastException) {
        }

    }

    override fun onPause() {
        try {
            super.onPause()
            localeDelegate.onPaused()
        } catch (_: ClassCastException) {
        }

    }

    override fun createConfigurationContext(overrideConfiguration: Configuration): Context {
        val context = super.createConfigurationContext(overrideConfiguration)
        return LocaleHelper.onAttach(context)
    }

    override fun getApplicationContext(): Context =
        localeDelegate.getApplicationContext(super.getApplicationContext())

    open fun updateLocale(locale: Locale) {
        localeDelegate.setLocale(this, locale)
    }
}