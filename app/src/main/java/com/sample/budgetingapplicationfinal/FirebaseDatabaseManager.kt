package com.sample.budgetingapplicationfinal

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate

// 1) Avoid name clash by renaming
data class ExpenseEntry(
    var category: String = "",
    var amount: Double = 0.0,
    var date: String = LocalDate.now().toString()
)

data class UserProfile(
    var uid: String = "",
    var email: String = "",
    var displayName: String = "",
    var createdAt: Long = System.currentTimeMillis()
)

data class GameProgress(
    var level: Int = 1,
    var score: Int = 0,
    var lastUpdate: Long = System.currentTimeMillis()
)

object FirebaseDatabaseManager {
    private val rootRef: DatabaseReference =
        FirebaseDatabase.getInstance().reference

    fun saveUserProfile(profile: UserProfile) {
        rootRef.child("users")
            .child(profile.uid)
            .child("profile")
            .setValue(profile)
    }

    fun saveIncome(uid: String, income: Income) {
        rootRef.child("users")
            .child(uid)
            .child("incomes")
            .push()
            .setValue(income)
    }

    fun saveExpense(uid: String, expense: Expense) {
        rootRef.child("users")
            .child(uid)
            .child("expenses")
            .push()
            .setValue(expense)
    }

    /** Save or update BudgetGoal under /users/{uid}/budgetGoal */
    fun saveBudgetGoal(uid: String, goal: BudgetGoal) {
        rootRef.child("users")
            .child(uid)
            .child("budgetGoal")
            .setValue(goal)
    }

    /** Retrieve BudgetGoal reference */
    fun getBudgetGoalRef(uid: String): DatabaseReference =
        rootRef.child("users")
            .child(uid)
            .child("budgetGoal")

    /** Save or update GameProgress under /users/{uid}/gameProgress */
    fun saveGameProgress(uid: String, progress: GameProgress) {
        rootRef.child("users")
            .child(uid)
            .child("gameProgress")
            .setValue(progress)
    }
}
