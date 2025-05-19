package com.sample.budgetingapplicationfinal

import java.time.LocalDate

data class Income(
    val amount: Double,
    val source: String = "",
)