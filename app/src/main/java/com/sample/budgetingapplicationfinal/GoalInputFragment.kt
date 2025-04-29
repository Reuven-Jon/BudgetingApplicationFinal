package com.sample.budgetingapplicationfinal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.sample.budgetingapplicationfinal.databinding.FragmentGoalInputBinding
import com.sample.budgetingapplicationfinal.BudgetGoal


/**
 * Lets users enter goal name, period and income.
 */
class GoalInputFragment : Fragment(R.layout.fragment_goal_input) {
    private var binding: FragmentGoalInputBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentGoalInputBinding.bind(view)
        binding?.btnSubmit?.setOnClickListener { submitGoal() }
    }

    /**
     * Reads inputs and navigates to board.
     */
    private fun submitGoal() {
        val name = binding?.etGoalName?.text.toString().ifBlank { "My Goal" }
        val period = binding?.etPeriodMonths?.text.toString().toIntOrNull() ?: return
        val income = binding?.etIncomePerMonth?.text.toString().toDoubleOrNull() ?: return
        // Total target = period × income

        val target = period.toDouble() * income

        val goal = BudgetGoal(name, target, period, income)
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, BoardFragment.newInstance(goal))
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}