package com.worldclock.app_themes.core.utils;

import android.content.Context;
import android.content.SharedPreferences;

public final class InAppPrefs {

    private static InAppPrefs inAppPrefs;
    private final SharedPreferences sharedPreferences;

    public InAppPrefs(Context context) {

        SharedPreferences sharedPreferences2 = context.getSharedPreferences("PrefPurchase", 0);

        this.sharedPreferences = sharedPreferences2;
    }

    public static final InAppPrefs getInstance(Context context) {

        synchronized (InAppPrefs.class) {
            if (InAppPrefs.inAppPrefs == null) {
                InAppPrefs.inAppPrefs = new InAppPrefs(context);
            }

        }
        return InAppPrefs.inAppPrefs;
    }

    public final boolean getPremium() {
        return this.sharedPreferences.getBoolean("premium", false);
    }

    public final void setPremium(boolean z) {
        this.sharedPreferences.edit().putBoolean("premium", z).apply();
    }


//    public final int getLanguage() {
//        return this.sharedPreferences.getInt("SELECTED_LANG", -1);
//    }
//
//    public final void setLanguage(int i) {
//        this.sharedPreferences.edit().putInt("SELECTED_LANG", i).apply();
//    }

}
