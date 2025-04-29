package com.sample.budgetingapplicationfinal

/**
 * Computes steps, milestones and cell highlighting.
 */
object Gamification {
    /**
     * Calculate amount each cell represents.
     */
    fun calculateStepSize(goal: BudgetGoal, totalCells: Int): Double {
        return goal.target / totalCells
    }

    /**
     * Fill cells based on saved amount.
     */
    fun fillCells(cells: List<BoardCell>, savedAmount: Double, stepSize: Double) {
        val fillCount = (savedAmount / stepSize).toInt().coerceIn(0, cells.size)
        cells.forEachIndexed { index, cell ->
            cell.isFilled = index < fillCount
        }
    }
}