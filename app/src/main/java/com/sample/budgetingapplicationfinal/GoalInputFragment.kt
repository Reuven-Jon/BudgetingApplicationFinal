package com.sample.budgetingapplicationfinal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sample.budgetingapplicationfinal.databinding.FragmentGoalInputBinding

class GoalInputFragment : Fragment(R.layout.fragment_goal_input) {
    private var binding: FragmentGoalInputBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentGoalInputBinding.bind(view)
        binding?.btnSubmit?.setOnClickListener { submitGoal() }
    }

    private fun submitGoal() {
        // 1) Read user input
        val name   = binding?.etGoalName?.text.toString().ifBlank { return }
        val cost   = binding?.etGoalCost?.text.toString().toDoubleOrNull() ?: return
        val period = binding?.etPeriodMonths?.text.toString().toIntOrNull()  ?: return

        // 2) Save the BudgetGoal under /users/{uid}/budgetGoal
        val uid = Firebase.auth.currentUser!!.uid
        val incomePerMonth = cost / period
        val userGoal = BudgetGoal(
            name = name,
            target = cost,
            periodMonths = period,
            incomePerMonth = incomePerMonth
        )
        FirebaseDatabaseManager.saveBudgetGoal(uid, userGoal)

        // 3) Navigate to BoardFragment directly (it will load the goal from Firebase)
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, BoardFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
