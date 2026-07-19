package com.doximity.realtimewatchlist_krishna_doximity.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.doximity.realtimewatchlist_krishna_doximity.R

sealed class AppDestination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    data object Watchlist : AppDestination(
        route = "watchlist",
        labelRes = R.string.nav_watchlist,
        icon = Icons.AutoMirrored.Filled.List,
    )

    data object Search : AppDestination(
        route = "search",
        labelRes = R.string.nav_search,
        icon = Icons.Default.Search,
    )

    companion object {
        val entries: List<AppDestination> = listOf(Watchlist, Search)
    }
}

fun NavigationSuiteScope.homeNavigationItems(
    selectedRoute: String?,
    onDestinationSelected: (AppDestination) -> Unit,
) {
    AppDestination.entries.forEach { destination ->
        val selected = selectedRoute == destination.route
        item(
            selected = selected,
            onClick = { onDestinationSelected(destination) },
            icon = {
                Icon(
                    imageVector = destination.icon,
                    contentDescription = null,
                    tint = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                    },
                )
            },
            label = { Text(stringResource(destination.labelRes)) },
        )
    }
}
