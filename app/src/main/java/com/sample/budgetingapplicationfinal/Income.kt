package com.sample.budgetingapplicationfinal

import java.time.LocalDate

// Data model for one income entry, stored in Firebase
data class Income(
    var category: String = "",                // e.g. “Freelance”, “Salary”
    var amount: Double = 0.0,                 // numeric amount
    var date: String = LocalDate.now().toString(), // ISO-date stamp
    var period: String = "",                  // e.g. “Jan 2025”, “Feb 2025”
    var source: String = ""                   // free-text description
)
