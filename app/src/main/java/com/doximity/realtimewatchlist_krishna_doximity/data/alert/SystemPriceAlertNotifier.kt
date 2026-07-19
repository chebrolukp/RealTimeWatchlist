package com.doximity.realtimewatchlist_krishna_doximity.data.alert

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.doximity.realtimewatchlist_krishna_doximity.MainActivity
import com.doximity.realtimewatchlist_krishna_doximity.R
import com.doximity.realtimewatchlist_krishna_doximity.core.ui.util.formatPrice
import com.doximity.realtimewatchlist_krishna_doximity.domain.alert.PriceAlertNotifier
import com.doximity.realtimewatchlist_krishna_doximity.domain.alert.label
import com.doximity.realtimewatchlist_krishna_doximity.domain.model.PriceAlert
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemPriceAlertNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) : PriceAlertNotifier {

    init {
        ensureChannel()
    }

    override fun notifyAlert(
        symbol: String,
        displaySymbol: String,
        alert: PriceAlert,
        currentPrice: Double,
    ) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val title = context.getString(R.string.price_alert_notification_title, displaySymbol)
        val body = context.getString(
            R.string.price_alert_notification_body,
            displaySymbol,
            formatPrice(currentPrice),
            alert.direction.label(),
            formatPrice(alert.threshold),
        )

        val contentIntent = PendingIntent.getActivity(
            context,
            symbol.hashCode(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_price_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        try {
            manager.notify(NOTIFICATION_TAG, symbol.hashCode(), notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS may be denied on API 33+.
        }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.price_alert_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.price_alert_channel_description)
        }
        manager.createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL_ID = "price_alerts"
        const val NOTIFICATION_TAG = "price_alert"
    }
}
