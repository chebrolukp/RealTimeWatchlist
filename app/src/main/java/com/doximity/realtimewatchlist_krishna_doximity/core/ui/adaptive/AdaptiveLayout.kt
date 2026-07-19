package com.doximity.realtimewatchlist_krishna_doximity.core.ui.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass

@Composable
fun adaptiveWindowInfo(): WindowAdaptiveInfo = currentWindowAdaptiveInfo()

@Composable
fun adaptiveContentPadding(
    adaptiveInfo: WindowAdaptiveInfo = adaptiveWindowInfo(),
): Dp {
    val sizeClass = adaptiveInfo.windowSizeClass
    return when {
        !sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> 16.dp
        !sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> 24.dp
        else -> 32.dp
    }
}

@Composable
fun adaptiveListColumnCount(
    adaptiveInfo: WindowAdaptiveInfo = adaptiveWindowInfo(),
): Int {
    val sizeClass = adaptiveInfo.windowSizeClass
    return if (sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)) {
        2
    } else {
        1
    }
}

@Composable
fun adaptiveMaxContentWidth(
    adaptiveInfo: WindowAdaptiveInfo = adaptiveWindowInfo(),
): Dp? {
    val sizeClass = adaptiveInfo.windowSizeClass
    return when {
        sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> 960.dp
        sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> 720.dp
        else -> null
    }
}

@Composable
fun AdaptiveContentContainer(
    modifier: Modifier = Modifier,
    applyHorizontalPadding: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val horizontalPadding = if (applyHorizontalPadding) adaptiveContentPadding() else 0.dp
    val maxWidth = adaptiveMaxContentWidth()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.TopCenter,
    ) {
        Box(
            modifier = Modifier
                .then(if (maxWidth != null) Modifier.widthIn(max = maxWidth) else Modifier)
                .fillMaxWidth(),
            content = content,
        )
    }
}
