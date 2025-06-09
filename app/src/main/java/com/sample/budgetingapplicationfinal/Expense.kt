// app/src/main/java/com/sample/budgetingapplicationfinal/ExpenseEntry.kt

package com.sample.budgetingapplicationfinal

import java.time.LocalDate



data class Expense(
    val category: String = "",
    val amount: Double = 0.0,
    val date: String = "",
    val period: String = "",
    val source: String = "",
    val photoUrl: String? = null        // ← add this
)

