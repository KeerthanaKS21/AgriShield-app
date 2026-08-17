package com.agrishield.app.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.agrishield.app.MainActivity
import com.agrishield.app.data.model.AlertItem
import com.agrishield.app.data.model.AlertSeverity

class AgriNotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.CHANNEL_ID_ALERTS,
                Constants.CHANNEL_NAME_ALERTS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical crop disease and weather alerts for farmers"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showAlertNotification(alert: AlertItem, isTamil: Boolean = false) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isTamil && alert.titleTa.isNotEmpty()) alert.titleTa else alert.title
        val message = if (isTamil && alert.messageTa.isNotEmpty()) alert.messageTa else alert.message

        val priority = when (alert.severity) {
            AlertSeverity.CRITICAL -> NotificationCompat.PRIORITY_MAX
            AlertSeverity.WARNING -> NotificationCompat.PRIORITY_HIGH
            AlertSeverity.INFO -> NotificationCompat.PRIORITY_DEFAULT
        }

        val builder = NotificationCompat.Builder(context, Constants.CHANNEL_ID_ALERTS)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationId = when (alert.type) {
            com.agrishield.app.data.model.AlertType.DISEASE_RISK -> Constants.NOTIFICATION_ID_RISK
            com.agrishield.app.data.model.AlertType.SEVERE_WEATHER -> Constants.NOTIFICATION_ID_WEATHER
            else -> Constants.NOTIFICATION_ID_CARE
        }

        notificationManager.notify(notificationId, builder.build())
    }
}
