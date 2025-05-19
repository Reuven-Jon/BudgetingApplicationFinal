package com.sample.budgetingapplicationfinal

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.util.Random

/**
 * Manages app notifications:
 *  - daily budget tips
 *  - expense-logged alerts
 */
class NotificationHelper(private val context: Context) {
    companion object {
        // channel for budget tips
        private const val TIP_CHANNEL_ID = "budget_tips"
        private const val TIP_CHANNEL_NAME = "Budget Tips"
        private const val TIP_CHANNEL_DESC = "Daily financial encouragement"

        // channel for expense alerts
        private const val EXPENSE_CHANNEL_ID = "expense_alerts"
        private const val EXPENSE_CHANNEL_NAME = "Expense Alerts"
        private const val EXPENSE_CHANNEL_DESC = "Notifies when you log an expense"
    }

    init {
        createTipChannel()         // set up tips channel on Android 8+
        createExpenseChannel()     // set up expense channel on Android 8+
    }

    private fun createTipChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TIP_CHANNEL_ID,
                TIP_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = TIP_CHANNEL_DESC }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createExpenseChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                EXPENSE_CHANNEL_ID,
                EXPENSE_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = EXPENSE_CHANNEL_DESC }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Show a budget tip notification.
     */
    fun showTip(message: String) {
        // check permission
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("NotificationHelper", "Missing POST_NOTIFICATIONS permission")
            return
        }

        try {
            val builder = NotificationCompat.Builder(context, TIP_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_tip)
                .setContentTitle("Budget Tip")
                .setContentText(message)
                .setAutoCancel(true)
            NotificationManagerCompat.from(context)
                .notify(Random().nextInt(), builder.build())
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Tip notification failed", e)
        }
    }

    /**
     * Show an “Expense logged” notification.
     */
    fun showExpenseNotification(source: String, amount: Double) {
        // check permission
        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("NotificationHelper", "Missing POST_NOTIFICATIONS permission")
            return
        }

        try {
            val text = "Expense for $source: R%,.2f".format(amount)
            val builder = NotificationCompat.Builder(context, EXPENSE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_expense)
                .setContentTitle("Expense logged")
                .setContentText(text)
                .setAutoCancel(true)
            NotificationManagerCompat.from(context)
                .notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Expense notification failed", e)
        }
    }
}
