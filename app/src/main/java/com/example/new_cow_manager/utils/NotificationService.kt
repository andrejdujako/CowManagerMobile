package com.example.new_cow_manager.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.new_cow_manager.R
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.ZoneId

class NotificationService(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            GGPG_CHANNEL_ID,
            "GGPG Protocol",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for GGPG protocol steps"
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun scheduleGgpgNotification(date: LocalDate, title: String, message: String, notificationId: Int) {
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(EXTRA_NOTIFICATION_TITLE, title)
            putExtra(EXTRA_NOTIFICATION_MESSAGE, message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = date.toJavaLocalDate()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    companion object {
        const val GGPG_CHANNEL_ID = "ggpg_protocol"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_NOTIFICATION_TITLE = "notification_title"
        const val EXTRA_NOTIFICATION_MESSAGE = "notification_message"

        const val NOTIFICATION_ID_FIRST_G = 1001
        const val NOTIFICATION_ID_SECOND_G = 1002
        const val NOTIFICATION_ID_P = 1003
        const val NOTIFICATION_ID_FINAL_G = 1004
    }
}
