package com.doximity.realtimewatchlist_krishna_doximity

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.adaptive.LocalWindowWidthSizeClass
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.adaptive.WindowWidthSizeClass
import com.doximity.realtimewatchlist_krishna_doximity.ui.theme.RealtimeWatchListKrishnaDoximityTheme

@Composable
fun CompactPhoneTestContent(content: @Composable () -> Unit) {
    RealtimeWatchListKrishnaDoximityTheme {
        CompositionLocalProvider(
            LocalWindowWidthSizeClass provides WindowWidthSizeClass.Compact,
        ) {
            content()
        }
    }
}
