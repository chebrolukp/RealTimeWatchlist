package com.doximity.realtimewatchlist_krishna_doximity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.lifecycleScope
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.adaptive.LocalWindowWidthSizeClass
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.adaptive.rememberWindowWidthSizeClass
import com.doximity.realtimewatchlist_krishna_doximity.ui.navigation.HomeScreen
import com.doximity.realtimewatchlist_krishna_doximity.ui.theme.RealtimeWatchListKrishnaDoximityTheme
import com.doximity.realtimewatchlist_krishna_doximity.ui.widget.WatchlistWidgetUpdater
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val widthSizeClass = rememberWindowWidthSizeClass()
            CompositionLocalProvider(
                LocalWindowWidthSizeClass provides widthSizeClass,
            ) {
                RealtimeWatchListKrishnaDoximityTheme {
                    HomeScreen()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            WatchlistWidgetUpdater.updateAll(applicationContext)
        }
    }
}
