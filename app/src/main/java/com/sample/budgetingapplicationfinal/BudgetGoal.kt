package com.sample.budgetingapplicationfinal



import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BudgetGoal(
    val name: String,
    val target: Double,
    val periodMonths: Int,
    val incomePerMonth: Double,

) : Parcelable