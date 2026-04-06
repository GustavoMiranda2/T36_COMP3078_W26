package com.brazwebdes.hairstylistbooking

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.webkit.WebView

/** Clears persisted WebView auth artifacts so admin sessions do not survive logout or app upgrades. */
object WebSessionStore {
    fun clear(context: Context) {
        val appContext = context.applicationContext
        val clearAction: () -> Unit = {
            runCatching {
                CookieManager.getInstance().apply {
                    removeSessionCookies(null)
                    removeAllCookies(null)
                    flush()
                }
            }
            runCatching { WebStorage.getInstance().deleteAllData() }
            runCatching {
                WebView(appContext).apply {
                    clearCache(true)
                    clearHistory()
                    clearFormData()
                    destroy()
                }
            }
            Unit
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            clearAction()
        } else {
            Handler(Looper.getMainLooper()).post(clearAction)
        }
    }
}
