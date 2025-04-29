package com.sample.budgetingapplicationfinal


/**
 * Data class for each board cell’s state.
 */
data class BoardCell(
    val index: Int,
    var isFilled: Boolean = false
)