package com.play.urlscheduler

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import com.play.urlscheduler.core.privilege.PayloadHolder
import timber.log.Timber

/**
 * A transparent trampoline activity that immediately fires a Custom Tab.
 * It does NOT bind to the CustomTabsService to prevent background hanging.
 */
class CustomTabActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("CustomTabActivity onCreate")
        
        // Force Screen Wake (Chrome won't show over a secure lock screen, but the physical screen will turn on!)
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
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Timber.d("CustomTabActivity onNewIntent")
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        // Read URL from Intent or shared memory
        val url = intent?.data?.toString() ?: intent?.getStringExtra("url") ?: PayloadHolder.currentUrl
        
        if (!url.isNullOrBlank()) {
            launchCustomTab(url)
        } else {
            Timber.e("No URL found in Intent or PayloadHolder!")
            finish()
        }
    }

    private fun launchCustomTab(url: String) {
        Timber.i("Launching embedded Custom Tab: \$url")
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        
        // Fire-and-Forget: Launch Chrome in an entirely separate, isolated task hierarchy
        customTabsIntent.intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        customTabsIntent.launchUrl(this, Uri.parse(url))
        
        // Immediately annihilate this trampoline activity so it never blocks the screen
        finish()
    }
}
