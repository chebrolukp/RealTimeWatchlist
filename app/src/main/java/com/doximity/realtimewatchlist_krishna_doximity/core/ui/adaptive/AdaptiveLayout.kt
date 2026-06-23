package com.doximity.realtimewatchlist_krishna_doximity.core.ui.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class AppWidthSizeClass {
    Compact,
    Medium,
    Expanded,
}

val LocalWindowWidthSizeClass = staticCompositionLocalOf { AppWidthSizeClass.Compact }

fun widthToSizeClass(width: Dp): AppWidthSizeClass = when {
    width < 600.dp -> AppWidthSizeClass.Compact
    width < 840.dp -> AppWidthSizeClass.Medium
    else -> AppWidthSizeClass.Expanded
}

@Composable
fun ProvideWindowWidthSizeClass(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    CompositionLocalProvider(
        LocalWindowWidthSizeClass provides widthToSizeClass(screenWidth),
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            content()
        }
    }
}

@Composable
fun adaptiveContentPadding(): Dp = when (LocalWindowWidthSizeClass.current) {
    AppWidthSizeClass.Compact -> 16.dp
    AppWidthSizeClass.Medium -> 24.dp
    AppWidthSizeClass.Expanded -> 32.dp
}

@Composable
fun adaptiveListColumnCount(): Int = when (LocalWindowWidthSizeClass.current) {
    AppWidthSizeClass.Expanded -> 2
    else -> 1
}

@Composable
fun adaptiveMaxContentWidth(): Dp? = when (LocalWindowWidthSizeClass.current) {
    AppWidthSizeClass.Expanded -> 960.dp
    AppWidthSizeClass.Medium -> 720.dp
    AppWidthSizeClass.Compact -> null
}

@Composable
fun useNavigationRail(): Boolean =
    LocalWindowWidthSizeClass.current != AppWidthSizeClass.Compact

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
