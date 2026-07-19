package com.doximity.realtimewatchlist_krishna_doximity.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.doximity.realtimewatchlist_krishna_doximity.BuildConfig
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.components.DemoModeBanner
import com.doximity.realtimewatchlist_krishna_doximity.ui.search.SearchScreen
import com.doximity.realtimewatchlist_krishna_doximity.ui.search.SearchViewModel
import com.doximity.realtimewatchlist_krishna_doximity.ui.theme.PageBackground
import com.doximity.realtimewatchlist_krishna_doximity.ui.watchlist.WatchlistScreen
import com.doximity.realtimewatchlist_krishna_doximity.ui.watchlist.WatchlistViewModel

@Composable
fun HomeScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val onDestinationSelected: (AppDestination) -> Unit = { destination ->
        navController.navigate(destination.route) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            homeNavigationItems(
                selectedRoute = currentDestination?.route,
                onDestinationSelected = onDestinationSelected,
            )
        },
        containerColor = PageBackground,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (BuildConfig.DEMO_MODE) {
                DemoModeBanner()
            }
            HomeNavHost(
                navController = navController,
                modifier = Modifier
                    .weight(1f)
                    .background(PageBackground),
            )
        }
    }
}

@Composable
private fun HomeNavHost(
    navController: androidx.navigation.NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Watchlist.route,
        modifier = modifier,
    ) {
        composable(AppDestination.Watchlist.route) {
            val viewModel: WatchlistViewModel = hiltViewModel()
            WatchlistScreen(viewModel = viewModel)
        }
        composable(AppDestination.Search.route) {
            val viewModel: SearchViewModel = hiltViewModel()
            SearchScreen(viewModel = viewModel)
        }
    }
}
