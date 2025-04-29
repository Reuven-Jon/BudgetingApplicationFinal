package com.sample.budgetingapplicationfinal

import android.content.Context
import android.content.SharedPreferences

/**
 * Saves preferences (currency, reminders).
 */
class UserSettings(context: Context) {
    companion object {
        private const val PREFS = "budget_app_prefs"
        private const val KEY_CURRENCY = "key_currency"
        private const val KEY_REMINDERS = "key_reminders"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    var currency: String
        get() = prefs.getString(KEY_CURRENCY, "ZAR") ?: "ZAR"
        set(value) = prefs.edit().putString(KEY_CURRENCY, value).apply()

    var remindersEnabled: Boolean
        get() = prefs.getBoolean(KEY_REMINDERS, true)
        set(value) = prefs.edit().putBoolean(KEY_REMINDERS, value).apply()
}