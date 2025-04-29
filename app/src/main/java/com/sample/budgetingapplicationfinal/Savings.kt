package com.sample.budgetingapplicationfinal

import java.time.LocalDate

/**
 * Data class for savings entries.
 */
data class Savings(
    val amount: Double,
    val date: LocalDate = LocalDate.now()
)