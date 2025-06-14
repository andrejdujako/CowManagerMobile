package com.example.new_cow_manager.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.new_cow_manager.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(NotificationService.EXTRA_NOTIFICATION_ID, 0)
        val title = intent.getStringExtra(NotificationService.EXTRA_NOTIFICATION_TITLE) ?: ""
        val message = intent.getStringExtra(NotificationService.EXTRA_NOTIFICATION_MESSAGE) ?: ""

        val notification = NotificationCompat.Builder(context, NotificationService.GGPG_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, notification)
        }
    }
}
