package com.brazwebdes.hairstylistbooking

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import org.json.JSONObject

class AdminDashboardActivity : BaseDrawerActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var webView: WebView

    private var injectedSession = false
    private var visitedProtectedPage = false
    private var fileChooserCallback: ValueCallback<Array<Uri>>? = null
    private var returningToPrimarySignIn = false

    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val callback = fileChooserCallback ?: return@registerForActivityResult
            fileChooserCallback = null

            val uris =
                if (result.resultCode == Activity.RESULT_OK) {
                    extractSelectedUris(result.data)
                } else {
                    null
                }
            callback.onReceiveValue(uris)
        }

    private val pageTitle: String
        get() = intent.getStringExtra(extraTitle).orEmpty().ifBlank { "Admin Dashboard" }

    private val pagePath: String
        get() = intent.getStringExtra(extraPath).orEmpty().ifBlank { "/admin/dashboard" }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!ensureAdminSession()) {
            return
        }

        setContentLayout(R.layout.content_admin_web)
        setToolbarTitle(pageTitle)
        setCheckedDrawerItem(R.id.m_admin)
        showLogoutOption(true)
        syncAuthUi()

        progressBar = findViewById(R.id.progressBar)
        webView = findViewById(R.id.webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.loadsImagesAutomatically = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.addJavascriptInterface(AndroidSessionBridge(), "AndroidSessionBridge")
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                progressBar.visibility = if (newProgress >= 100) View.GONE else View.VISIBLE
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                fileChooserCallback?.onReceiveValue(null)
                fileChooserCallback = filePathCallback

                if (filePathCallback == null || fileChooserParams == null) {
                    return false
                }

                return try {
                    filePickerLauncher.launch(buildFileChooserIntent(fileChooserParams))
                    true
                } catch (_: ActivityNotFoundException) {
                    fileChooserCallback = null
                    filePathCallback.onReceiveValue(null)
                    false
                }
            }
        }
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                progressBar.visibility = View.GONE

                if (injectedSession && visitedProtectedPage && isLoginPage(url)) {
                    returnToPrimarySignIn()
                    return
                }

                if (!injectedSession && AdminSession.isLoggedIn && view != null && isLoginPage(url)) {
                    injectedSession = true
                    view.evaluateJavascript(
                        """
                        (() => {
                          localStorage.setItem('hb-access', ${jsString(ApiClient.accessToken.orEmpty())});
                          localStorage.setItem('hb-refresh', ${jsString(ApiClient.refreshToken.orEmpty())});
                          localStorage.setItem('hb-role', 'admin');
                          localStorage.setItem('hb-name', ${jsString(AdminSession.displayName.ifBlank { "Admin" })});
                          window.location.replace(${jsString("${ApiClient.webBaseUrl}$pagePath")});
                        })();
                        """.trimIndent(),
                        null
                    )
                    return
                }

                if (injectedSession && !isLoginPage(url)) {
                    visitedProtectedPage = true
                }

                if (view != null) {
                    injectMobileAdminWebGuards(view)
                }
            }
        }
        webView.loadUrl("${ApiClient.webBaseUrl}/login")

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        ensureAdminSession()
    }

    override fun onDestroy() {
        fileChooserCallback?.onReceiveValue(null)
        fileChooserCallback = null
        super.onDestroy()
    }

    private fun ensureAdminSession(): Boolean {
        if (AdminSession.isLoggedIn) {
            return true
        }

        returnToPrimarySignIn()
        return false
    }

    private fun isLoginPage(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val path = runCatching { Uri.parse(url).path.orEmpty() }.getOrDefault("")
        return path == "/login" || path == "/admin/login"
    }

    private fun jsString(value: String): String = JSONObject.quote(value)

    private fun buildFileChooserIntent(params: WebChromeClient.FileChooserParams): Intent {
        val mimeTypes = params.acceptTypes
            ?.flatMap { value -> value.split(',') }
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?.distinct()
            ?.toTypedArray()
            ?: emptyArray()

        val primaryType = when {
            mimeTypes.isEmpty() -> "image/*"
            mimeTypes.size == 1 -> mimeTypes.first()
            else -> "*/*"
        }

        return Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = primaryType
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, params.mode == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE)
            if (mimeTypes.size > 1) {
                putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            }
        }
    }

    private fun extractSelectedUris(data: Intent?): Array<Uri>? {
        if (data == null) return null

        val clipData: ClipData? = data.clipData
        if (clipData != null && clipData.itemCount > 0) {
            return Array(clipData.itemCount) { index -> clipData.getItemAt(index).uri }
        }

        return data.data?.let { arrayOf(it) }
    }

    private fun injectMobileAdminWebGuards(view: WebView) {
        view.evaluateJavascript(
            """
            (() => {
              const bridge = window.AndroidSessionBridge;
              if (!bridge) return;
              const targetUrl = ${jsString("${ApiClient.webBaseUrl}$pagePath")};

              const clearSession = () => {
                try {
                  localStorage.removeItem('hb-access');
                  localStorage.removeItem('hb-refresh');
                  localStorage.removeItem('hb-role');
                  localStorage.removeItem('hb-name');
                } catch (_) {}
              };

              const hideHeader = () => {
                const header = document.querySelector('header');
                if (header instanceof HTMLElement) {
                  header.style.display = 'none';
                }
              };

              const notifyLogout = (reason) => {
                if (reason === 'expired') {
                  if (typeof bridge.onSessionExpired === 'function') {
                    bridge.onSessionExpired();
                  }
                } else if (typeof bridge.onLogout === 'function') {
                  bridge.onLogout();
                }
              };

              const syncState = () => {
                hideHeader();
                const path = window.location.pathname || '';
                const access = localStorage.getItem('hb-access');
                const role = localStorage.getItem('hb-role');

                if (path === '/login' || path === '/admin/login') {
                  if (access && role === 'admin') {
                    if (window.location.href !== targetUrl) {
                      window.location.replace(targetUrl);
                    }
                  }
                  return;
                }

                if (!access || role !== 'admin') {
                  notifyLogout('expired');
                }
              };

              if (!window.__hbMobileAdminGuardsInstalled) {
                window.__hbMobileAdminGuardsInstalled = true;

                const originalFetch = window.fetch.bind(window);
                window.fetch = async (...args) => {
                  const response = await originalFetch(...args);
                  if (response.status === 401) {
                    clearSession();
                    setTimeout(() => notifyLogout('expired'), 0);
                  }
                  return response;
                };

                for (const method of ['pushState', 'replaceState']) {
                  const original = history[method].bind(history);
                  history[method] = (...args) => {
                    const result = original(...args);
                    setTimeout(syncState, 0);
                    return result;
                  };
                }

                window.addEventListener('popstate', () => setTimeout(syncState, 0));
                window.addEventListener('pageshow', () => setTimeout(syncState, 0));
                window.addEventListener('storage', () => setTimeout(syncState, 0));

                document.addEventListener(
                  'click',
                  (event) => {
                    const target = event.target instanceof Element ? event.target.closest('button') : null;
                    const label = target?.textContent?.trim().toLowerCase() || '';
                    if (target && label.includes('sign out')) {
                      clearSession();
                      setTimeout(() => notifyLogout('logout'), 0);
                    }
                  },
                  true,
                );

                new MutationObserver(() => hideHeader()).observe(document.documentElement, {
                  childList: true,
                  subtree: true,
                });
              }

              syncState();
            })();
            """.trimIndent(),
            null
        )
    }

    private fun returnToPrimarySignIn() {
        if (returningToPrimarySignIn) return
        returningToPrimarySignIn = true
        AppSessionStore.clear(this)
        startActivity(Intent(this, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
        finish()
    }

    companion object {
        const val extraTitle = "title"
        const val extraPath = "path"

        fun intent(
            context: Context,
            title: String = "Admin Dashboard",
            path: String = "/admin/dashboard"
        ): Intent {
            return Intent(context, AdminDashboardActivity::class.java).apply {
                putExtra(extraTitle, title)
                putExtra(extraPath, path)
            }
        }
    }

    private inner class AndroidSessionBridge {
        @JavascriptInterface
        fun onLogout() {
            runOnUiThread { returnToPrimarySignIn() }
        }

        @JavascriptInterface
        fun onSessionExpired() {
            runOnUiThread {
                if (returningToPrimarySignIn) return@runOnUiThread
                Toast.makeText(
                    this@AdminDashboardActivity,
                    "Session expired. Please sign in again.",
                    Toast.LENGTH_LONG
                ).show()
                returnToPrimarySignIn()
            }
        }
    }
}
