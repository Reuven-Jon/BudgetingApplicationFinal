package com.sample.budgetingapplicationfinal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Random

/**
 * Triggers financial tips & goal reminders.
 */
class NotificationHelper(private val context: Context) {
    companion object {
        private const val CHANNEL_ID = "budget_tips"
        private const val CHANNEL_NAME = "Budget Tips"
        private const val CHANNEL_DESC = "Daily financial encouragement"
    }

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = CHANNEL_DESC }
            val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mgr.createNotificationChannel(channel)
        }
    }

    /**
     * Show a notification with a tip message.
     */
    fun showTip(message: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tip)
            .setContentTitle("Budget Tip")
            .setContentText(message)
            .setAutoCancel(true)
        NotificationManagerCompat.from(context).notify(Random().nextInt(), builder.build())
    }
}