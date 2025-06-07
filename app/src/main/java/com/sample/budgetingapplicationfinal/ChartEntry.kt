package com.sample.budgetingapplicationfinal

// One bar on the chart
data class ChartEntry(
    val label: String,      // e.g. "May 5" or category
    val amount: Float,
    val isIncome: Boolean
)
