package com.sample.budgetingapplicationfinal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

/**
 * Data class for expense entries.
 *
 * @param source  what the money was spent on (e.g. "Car Service")
 * @param amount  how much was spent
 * @param date    date of the expense (defaults to today)
 */
@Parcelize
data class Expense(
    val source: String,
    val amount: Double,
    val date: LocalDate = LocalDate.now()
) : Parcelable {
    init {
        require(source.isNotBlank()) { "Expense source cannot be blank" }
        require(amount >= 0.0)      { "Expense amount must be non-negative" }
    }
}
