// app/src/main/java/com/sample/budgetingapplicationfinal/ExpenseEntry.kt

package com.sample.budgetingapplicationfinal

import java.time.LocalDate

data class Expense(
    var category: String = "",               // e.g. “Groceries”
    var amount: Double = 0.0,                // e.g. 250.0
    var date: String = LocalDate.now().toString(),  // ISO date
    var period: String = "",                 // e.g. “Jun”
    var source: String = ""                  // free-text description
)
