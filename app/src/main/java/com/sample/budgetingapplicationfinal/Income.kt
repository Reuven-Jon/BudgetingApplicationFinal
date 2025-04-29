package com.sample.budgetingapplicationfinal

import java.time.LocalDate

/**
 * Data class for income entries.
 */
data class Income(
    val amount: Double,
    val source: String = "",
    val date: LocalDate = LocalDate.now()
)