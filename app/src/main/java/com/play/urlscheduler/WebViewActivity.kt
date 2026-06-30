package com.play.urlscheduler

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.play.urlscheduler.core.privilege.PayloadHolder
import com.play.urlscheduler.core.ui.theme.URLRotatorTheme
import timber.log.Timber

class WebViewActivity : ComponentActivity() {

    // Compose state for the current URL
    private var currentUrl by mutableStateOf<String?>(null)
    private var loadTrigger by mutableStateOf(0)

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("WebViewActivity onCreate")

        // Force Screen Wake and allow drawing over secure lock screens
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        handleIntent(intent)

        setContent {
            URLRotatorTheme {
                // Scaffold automatically handles Edge-to-Edge System Window Insets!
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    currentUrl?.let { url ->
                        key(loadTrigger) {
                            AndroidView(
                                factory = { context ->
                                    WebView(context).apply {
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        settings.cacheMode = WebSettings.LOAD_DEFAULT
                                        webViewClient = WebViewClient()
                                        Timber.i("WebView factory loading URL: \$url")
                                        loadUrl(url)
                                    }
                                },
                                update = { webView ->
                                    Timber.i("WebView updating URL to: \$url")
                                    webView.loadUrl(url)
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }
                    } ?: run {
                        // Fallback UI if URL is null
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.fillMaxSize().padding(innerPadding),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Invalid or missing URL payload.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Timber.d("WebViewActivity onNewIntent")
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val url = intent?.data?.toString() ?: intent?.getStringExtra("url") ?: PayloadHolder.currentUrl
        url?.let {
            currentUrl = it
            loadTrigger++ // Force a fresh WebView render
        }
    }
}
