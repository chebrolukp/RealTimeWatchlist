package com.doximity.realtimewatchlist_krishna_doximity.core.ui.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Had some trouble using actual material design WindowSizeClass
 * Mirrors [androidx.compose.material3.windowsizeclass.WindowWidthSizeClass] breakpoints:
 * Compact &lt; 600dp, Medium &lt; 840dp, else Expanded.
 */
enum class WindowWidthSizeClass {
    Compact,
    Medium,
    Expanded,
}

data class WindowSizeClass(
    val widthSizeClass: WindowWidthSizeClass,
    val heightSizeClass: WindowWidthSizeClass,
)

private const val CompactWidthBreakpoint = 600
private const val MediumWidthBreakpoint = 840

private fun Int.toWidthSizeClass(): WindowWidthSizeClass = when {
    this < CompactWidthBreakpoint -> WindowWidthSizeClass.Compact
    this < MediumWidthBreakpoint -> WindowWidthSizeClass.Medium
    else -> WindowWidthSizeClass.Expanded
}

@Composable
fun rememberWindowSizeClass(): WindowSizeClass {
    val configuration = LocalConfiguration.current
    return remember(configuration.screenWidthDp, configuration.screenHeightDp) {
        WindowSizeClass(
            widthSizeClass = configuration.screenWidthDp.toWidthSizeClass(),
            heightSizeClass = configuration.screenHeightDp.toWidthSizeClass(),
        )
    }
}

@Composable
fun rememberWindowWidthSizeClass(): WindowWidthSizeClass =
    rememberWindowSizeClass().widthSizeClass
