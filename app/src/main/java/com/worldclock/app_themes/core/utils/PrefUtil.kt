package com.worldclock.app_themes.core.utils

import android.content.Context
import androidx.core.content.edit

class PrefUtil(private val context: Context)
{
    fun setInt(key: String?, value: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0)
        prefs.edit {
            putInt(key, value)
        }
    }

    fun getInt(key: String?, defValue: Int): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0)
        return prefs.getInt(key, defValue)
    }

    fun setString(key: String?, value: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0)
        prefs.edit {
            putString(key, value)
        }
    }

    fun getString(key: String?): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0)
        return prefs.getString(key, "null")
    }

    fun setBool(key: String?, value: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0)
        prefs.edit {
            putBoolean(key, value)
        }
    }

    fun getBool(key: String?): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0)
        return prefs.getBoolean(key, false)
    }

    fun getBool(key: String?, defaultValue: Boolean): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, 0)
        return prefs.getBoolean(key, defaultValue)
    } //    public boolean getIsLinearLayout(String key) {

    companion object {
        val PREFS_NAME = "my_prefs"
        const val premiumKey = "PREMIUM"
        const val premiumCheck = "CHECK"
        fun setPremiumString(value: String, context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            prefs.edit {
                putString(premiumKey, value)
            }
        }

        fun getPremium(context: Context): String? {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            return prefs.getString(premiumKey, "")
        }

        fun setPremium(context: Context, value: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            prefs.edit {
                putBoolean(premiumCheck, value)
            }
        }

        fun isPremium(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            return prefs.getBoolean(premiumCheck, false)
        }
    }
}