package com.doximity.realtimewatchlist_krishna_doximity.ui.watchlist

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.doximity.realtimewatchlist_krishna_doximity.R
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.util.formatPrice
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.PriceAlert
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.PriceAlertDirection

@Composable
fun PriceAlertSection(
    alert: PriceAlert?,
    onSetAlert: (threshold: Double, direction: PriceAlertDirection) -> Unit,
    onClearAlert: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    var pendingAlert by remember { mutableStateOf<Pair<Double, PriceAlertDirection>?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val pending = pendingAlert
        pendingAlert = null
        if (granted && pending != null) {
            onSetAlert(pending.first, pending.second)
        }
    }

    fun submitAlert(threshold: Double, direction: PriceAlertDirection) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                pendingAlert = threshold to direction
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        onSetAlert(threshold, direction)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = alertStatusLabel(alert),
            style = MaterialTheme.typography.bodyMedium,
            color = if (alert?.triggered == true) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = { showDialog = true }) {
                Text(
                    text = stringResource(
                        if (alert == null) R.string.price_alert_set else R.string.price_alert_edit,
                    ),
                )
            }
            if (alert != null) {
                OutlinedButton(onClick = onClearAlert) {
                    Text(text = stringResource(R.string.price_alert_clear))
                }
            }
        }
    }

    if (showDialog) {
        PriceAlertDialog(
            initial = alert,
            onDismiss = { showDialog = false },
            onConfirm = { threshold, direction ->
                showDialog = false
                submitAlert(threshold, direction)
            },
        )
    }
}

@Composable
private fun alertStatusLabel(alert: PriceAlert?): String {
    if (alert == null) {
        return stringResource(R.string.price_alert_one_shot_hint)
    }
    val threshold = formatPrice(alert.threshold)
    return when {
        alert.triggered && alert.direction == PriceAlertDirection.Above ->
            stringResource(R.string.price_alert_triggered_above, threshold)
        alert.triggered && alert.direction == PriceAlertDirection.Below ->
            stringResource(R.string.price_alert_triggered_below, threshold)
        alert.direction == PriceAlertDirection.Above ->
            stringResource(R.string.price_alert_active_above, threshold)
        else ->
            stringResource(R.string.price_alert_active_below, threshold)
    }
}

@Composable
private fun PriceAlertDialog(
    initial: PriceAlert?,
    onDismiss: () -> Unit,
    onConfirm: (threshold: Double, direction: PriceAlertDirection) -> Unit,
) {
    var thresholdText by rememberSaveable {
        mutableStateOf(initial?.threshold?.toString().orEmpty())
    }
    var directionAbove by rememberSaveable {
        mutableStateOf(initial?.direction == PriceAlertDirection.Above)
    }
    var error by remember { mutableStateOf<String?>(null) }
    val invalidMessage = stringResource(R.string.price_alert_invalid_threshold)
    val direction = if (directionAbove) {
        PriceAlertDirection.Above
    } else {
        PriceAlertDirection.Below
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.price_alert_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.price_alert_one_shot_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = thresholdText,
                    onValueChange = {
                        thresholdText = it
                        error = null
                    },
                    label = { Text(stringResource(R.string.price_alert_threshold_label)) },
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                DirectionOption(
                    label = stringResource(R.string.price_alert_direction_above),
                    selected = directionAbove,
                    onClick = { directionAbove = true },
                )
                DirectionOption(
                    label = stringResource(R.string.price_alert_direction_below),
                    selected = !directionAbove,
                    onClick = { directionAbove = false },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val value = thresholdText.toDoubleOrNull()
                    if (value == null || value <= 0.0) {
                        error = invalidMessage
                    } else {
                        onConfirm(value, direction)
                    }
                },
            ) {
                Text(stringResource(R.string.price_alert_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.price_alert_cancel))
            }
        },
    )
}

@Composable
private fun DirectionOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}
