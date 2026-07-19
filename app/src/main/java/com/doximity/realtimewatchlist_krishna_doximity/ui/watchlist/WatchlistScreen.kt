package com.doximity.realtimewatchlist_krishna_doximity.ui.watchlist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.doximity.realtimewatchlist_krishna_doximity.R
import com.doximity.realtimewatchlist_krishna_doximity.core.domain.model.ConnectionState
import com.doximity.realtimewatchlist_krishna_doximity.core.domain.model.PriceStatus
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.adaptive.AdaptiveContentContainer
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.adaptive.adaptiveContentPadding
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.components.ConnectionBanner
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.components.EmptyState
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.components.ErrorBanner
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.components.LoadingIndicator
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.model.asString
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.util.buildWatchlistStatusText
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.util.connectionStateLabel
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.util.formatChange
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.util.formatPercentChange
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.util.formatPrice
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.util.formatWatchlistEntryContentDescription
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.PriceAlertDirection
import com.doximity.realtimewatchlist_krishna_doximity.ui.theme.CardBackground
import com.doximity.realtimewatchlist_krishna_doximity.ui.theme.Error
import com.doximity.realtimewatchlist_krishna_doximity.ui.theme.ListItemBackground
import com.doximity.realtimewatchlist_krishna_doximity.ui.theme.PageBackground
import com.doximity.realtimewatchlist_krishna_doximity.ui.theme.Tertiary
import kotlinx.coroutines.launch

@Composable
fun WatchlistScreen(
    viewModel: WatchlistViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WatchlistContent(
        uiState = uiState,
        onRemove = viewModel::removeSymbol,
        onRefresh = viewModel::refresh,
        onPreviousPage = viewModel::previousPage,
        onNextPage = viewModel::nextPage,
        onSetPriceAlert = viewModel::setPriceAlert,
        onClearPriceAlert = viewModel::clearPriceAlert,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun WatchlistContent(
    uiState: WatchlistScreenState,
    onRemove: (String) -> Unit,
    onRefresh: () -> Unit,
    onPreviousPage: () -> Unit = {},
    onNextPage: () -> Unit = {},
    onSetPriceAlert: (String, Double, PriceAlertDirection) -> Unit = { _, _, _ -> },
    onClearPriceAlert: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val contentPadding = adaptiveContentPadding()
    val context = LocalContext.current
    val watchlistLoadingMessage = stringResource(R.string.watchlist_loading)
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val scope = rememberCoroutineScope()

    BackHandler(enabled = navigator.canNavigateBack()) {
        scope.launch { navigator.navigateBack() }
    }

    AdaptiveContentContainer(
        modifier = modifier
            .fillMaxSize()
            .background(PageBackground),
        applyHorizontalPadding = false,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (uiState.totalItems > 0) {
                ConnectionBanner(
                    label = connectionStateLabel(context, uiState.connectionState),
                    modifier = Modifier.padding(horizontal = contentPadding),
                )
            }

            uiState.errorMessage?.let { message ->
                ErrorBanner(
                    message = message.asString(),
                    modifier = Modifier.padding(horizontal = contentPadding),
                )
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingIndicator(message = watchlistLoadingMessage)
                    }
                }

                else -> {
                    val pullToRefreshState = rememberPullToRefreshState()
                    PullToRefreshBox(
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = onRefresh,
                        state = pullToRefreshState,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        when {
                            uiState.totalItems == 0 -> {
                                EmptyState(
                                    title = stringResource(R.string.watchlist_empty_title),
                                    message = stringResource(R.string.watchlist_empty_message),
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }

                            else -> {
                                ListDetailPaneScaffold(
                                    directive = navigator.scaffoldDirective,
                                    value = navigator.scaffoldValue,
                                    listPane = {
                                        AnimatedPane {
                                            WatchlistListPane(
                                                uiState = uiState,
                                                selectedSymbol = navigator.currentDestination?.contentKey,
                                                contentPadding = contentPadding,
                                                onSelect = { symbol ->
                                                    scope.launch {
                                                        navigator.navigateTo(
                                                            pane = ListDetailPaneScaffoldRole.Detail,
                                                            contentKey = symbol,
                                                        )
                                                    }
                                                },
                                                onRemove = onRemove,
                                                onPreviousPage = onPreviousPage,
                                                onNextPage = onNextPage,
                                            )
                                        }
                                    },
                                    detailPane = {
                                        AnimatedPane {
                                            val selectedSymbol = navigator.currentDestination?.contentKey
                                            val selectedEntry = uiState.entries.find {
                                                it.item.symbol == selectedSymbol
                                            }
                                            WatchlistDetailPane(
                                                entry = selectedEntry,
                                                connectionState = uiState.connectionState,
                                                canNavigateBack = navigator.canNavigateBack(),
                                                onBack = {
                                                    scope.launch { navigator.navigateBack() }
                                                },
                                                onRemove = onRemove,
                                                onSetPriceAlert = onSetPriceAlert,
                                                onClearPriceAlert = onClearPriceAlert,
                                                contentPadding = contentPadding,
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WatchlistListPane(
    uiState: WatchlistScreenState,
    selectedSymbol: String?,
    contentPadding: Dp,
    onSelect: (String) -> Unit,
    onRemove: (String) -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(contentPadding),
            verticalArrangement = Arrangement.spacedBy(contentPadding / 2),
        ) {
            items(uiState.entries, key = { it.item.symbol }) { entry ->
                WatchlistItemCard(
                    entry = entry,
                    connectionState = uiState.connectionState,
                    selected = entry.item.symbol == selectedSymbol,
                    onClick = { onSelect(entry.item.symbol) },
                    onRemove = onRemove,
                )
            }
        }

        if (uiState.showPagination) {
            WatchlistPaginationBar(
                currentPage = uiState.currentPage,
                totalPages = uiState.totalPages,
                totalItems = uiState.totalItems,
                canGoToPreviousPage = uiState.canGoToPreviousPage,
                canGoToNextPage = uiState.canGoToNextPage,
                onPreviousPage = onPreviousPage,
                onNextPage = onNextPage,
                modifier = Modifier.padding(contentPadding),
            )
        }
    }
}

@Composable
private fun WatchlistDetailPane(
    entry: WatchlistEntryUiModel?,
    connectionState: ConnectionState,
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    onRemove: (String) -> Unit,
    onSetPriceAlert: (String, Double, PriceAlertDirection) -> Unit,
    onClearPriceAlert: (String) -> Unit,
    contentPadding: Dp,
) {
    if (entry == null) {
        EmptyState(
            title = stringResource(R.string.watchlist_detail_placeholder_title),
            message = stringResource(R.string.watchlist_detail_placeholder_message),
            modifier = Modifier.fillMaxSize(),
        )
        return
    }

    val context = LocalContext.current
    val statusText = buildWatchlistStatusText(context, entry.status, connectionState)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(contentPadding / 2),
    ) {
        if (canNavigateBack) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.minimumInteractiveComponentSize(),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.watchlist_navigate_back),
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp)
                    .background(ListItemBackground)
                    .padding(contentPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = entry.item.displaySymbol,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.semantics { heading() },
                        )
                        Text(
                            text = entry.item.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelMedium,
                            color = statusColor(entry.status),
                        )
                    }

                    IconButton(
                        onClick = { onRemove(entry.item.symbol) },
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(
                                R.string.remove_from_watchlist,
                                entry.item.displaySymbol,
                            ),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                Text(
                    text = entry.price?.let(::formatPrice) ?: "—",
                    style = MaterialTheme.typography.displaySmall,
                )
                entry.change?.let { change ->
                    val changeColor = if (change >= 0) Tertiary else Error
                    Text(
                        text = "${formatChange(change)} (${formatPercentChange(entry.percentChange ?: 0.0)})",
                        style = MaterialTheme.typography.titleMedium,
                        color = changeColor,
                    )
                }

                PriceAlertSection(
                    alert = entry.item.priceAlert,
                    onSetAlert = { threshold, direction ->
                        onSetPriceAlert(entry.item.symbol, threshold, direction)
                    },
                    onClearAlert = { onClearPriceAlert(entry.item.symbol) },
                )

                Text(
                    text = stringResource(R.string.chart_range_30d),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                PriceSparkline(
                    chart = entry.chart,
                    displaySymbol = entry.item.displaySymbol,
                    modifier = Modifier.height(120.dp),
                )
            }
        }
    }
}

@Composable
private fun WatchlistPaginationBar(
    currentPage: Int,
    totalPages: Int,
    totalItems: Int,
    canGoToPreviousPage: Boolean,
    canGoToNextPage: Boolean,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pageLabel = stringResource(
        R.string.watchlist_page_indicator,
        currentPage + 1,
        totalPages,
    )
    val pageDescription = stringResource(
        R.string.a11y_watchlist_page,
        currentPage + 1,
        totalPages,
        totalItems,
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(
            onClick = onPreviousPage,
            enabled = canGoToPreviousPage,
            modifier = Modifier.minimumInteractiveComponentSize(),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.watchlist_previous_page),
            )
        }

        Text(
            text = pageLabel,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { contentDescription = pageDescription },
        )

        IconButton(
            onClick = onNextPage,
            enabled = canGoToNextPage,
            modifier = Modifier.minimumInteractiveComponentSize(),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.watchlist_next_page),
            )
        }
    }
}

@Composable
private fun WatchlistItemCard(
    entry: WatchlistEntryUiModel,
    connectionState: ConnectionState,
    selected: Boolean,
    onClick: () -> Unit,
    onRemove: (String) -> Unit,
) {
    val context = LocalContext.current
    val statusText = buildWatchlistStatusText(context, entry.status, connectionState)
    val entryDescription = formatWatchlistEntryContentDescription(
        context = context,
        displaySymbol = entry.item.displaySymbol,
        description = entry.item.description,
        price = entry.price,
        change = entry.change,
        percentChange = entry.percentChange,
        status = entry.status,
        connectionState = connectionState,
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                CardBackground
            },
        ),
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = false) {
                contentDescription = entryDescription
            }
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
                .background(
                    if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        ListItemBackground
                    },
                )
                .padding(adaptiveContentPadding()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(adaptiveContentPadding() / 2),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = entry.item.displaySymbol,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = entry.item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor(entry.status),
                )
                entry.item.priceAlert?.let { alert ->
                    Text(
                        text = if (alert.triggered) {
                            stringResource(
                                if (alert.direction == PriceAlertDirection.Above) {
                                    R.string.price_alert_triggered_above
                                } else {
                                    R.string.price_alert_triggered_below
                                },
                                formatPrice(alert.threshold),
                            )
                        } else {
                            stringResource(
                                if (alert.direction == PriceAlertDirection.Above) {
                                    R.string.price_alert_active_above
                                } else {
                                    R.string.price_alert_active_below
                                },
                                formatPrice(alert.threshold),
                            )
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = entry.price?.let(::formatPrice) ?: "—",
                    style = MaterialTheme.typography.titleMedium,
                )
                entry.change?.let { change ->
                    val changeColor = if (change >= 0) Tertiary else Error
                    Text(
                        text = formatPercentChange(entry.percentChange ?: 0.0),
                        style = MaterialTheme.typography.bodySmall,
                        color = changeColor,
                    )
                }
            }

            IconButton(
                onClick = { onRemove(entry.item.symbol) },
                modifier = Modifier.minimumInteractiveComponentSize(),
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(
                        R.string.remove_from_watchlist,
                        entry.item.displaySymbol,
                    ),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun statusColor(status: PriceStatus) = when (status) {
    PriceStatus.Live -> MaterialTheme.colorScheme.primary
    PriceStatus.Stale -> MaterialTheme.colorScheme.tertiary
    PriceStatus.Unavailable -> MaterialTheme.colorScheme.error
}
