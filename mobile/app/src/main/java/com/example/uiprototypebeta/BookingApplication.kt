package com.brazwebdes.hairstylistbooking

import android.app.Application
import androidx.core.content.pm.PackageInfoCompat

class BookingApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        clearSessionOnAppUpdate()
        AppSessionStore.initialize(this)
    }

    private fun clearSessionOnAppUpdate() {
        val prefs = getSharedPreferences(APP_BOOT_PREFS, MODE_PRIVATE)
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val currentVersionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
        val previousVersionCode = prefs.getLong(KEY_LAST_VERSION_CODE, -1L)

        if (previousVersionCode != -1L && previousVersionCode != currentVersionCode) {
            AppSessionStore.clear(this)
        }

        prefs.edit().putLong(KEY_LAST_VERSION_CODE, currentVersionCode).apply()
    }

    companion object {
        private const val APP_BOOT_PREFS = "app_boot"
        private const val KEY_LAST_VERSION_CODE = "last_version_code"
    }
}
