package com.doximity.realtimewatchlist_krishna_doximity.core.ui.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalWindowWidthSizeClass = staticCompositionLocalOf { WindowWidthSizeClass.Compact }

@Composable
fun adaptiveContentPadding(): Dp = when (LocalWindowWidthSizeClass.current) {
    WindowWidthSizeClass.Compact -> 16.dp
    WindowWidthSizeClass.Medium -> 24.dp
    WindowWidthSizeClass.Expanded -> 32.dp
}

@Composable
fun adaptiveListColumnCount(): Int = when (LocalWindowWidthSizeClass.current) {
    WindowWidthSizeClass.Expanded -> 2
    else -> 1
}

@Composable
fun adaptiveMaxContentWidth(): Dp? = when (LocalWindowWidthSizeClass.current) {
    WindowWidthSizeClass.Expanded -> 960.dp
    WindowWidthSizeClass.Medium -> 720.dp
    WindowWidthSizeClass.Compact -> null
}

@Composable
fun useNavigationRail(): Boolean =
    LocalWindowWidthSizeClass.current != WindowWidthSizeClass.Compact

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
