package com.sample.budgetingapplicationfinal

import java.time.LocalDate

data class Income(
    var source: String = "",           // where the income came from
    var amount: Double = 0.0,          // how much you earned
    var date: String = LocalDate.now().toString()  // store date as text
)