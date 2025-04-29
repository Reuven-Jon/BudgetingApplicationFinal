package com.sample.budgetingapplicationfinal

import java.time.LocalDate

/**
 * Data class for expense entries.
 */
data class Expenses(
    val amount: Double,
    val category: String,
    val date: LocalDate = LocalDate.now()
)