package com.sample.budgetingapplicationfinal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.sample.budgetingapplicationfinal.databinding.FragmentGoalInputBinding


/**
 * Lets users enter what they're saving for, the total cost, period,
 * and then computes the monthly amount needed (used as incomePerMonth).
 */
class GoalInputFragment : Fragment(R.layout.fragment_goal_input) {
    private var binding: FragmentGoalInputBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentGoalInputBinding.bind(view)
        binding?.btnSubmit?.setOnClickListener { submitGoal() }
    }

    private fun submitGoal() {
        val name   = binding?.etGoalName?.text.toString().ifBlank { return }
        val cost   = binding?.etGoalCost?.text.toString().toDoubleOrNull() ?: return
        val period = binding?.etPeriodMonths?.text.toString().toIntOrNull()  ?: return

        // compute how much to set aside each month to hit the goal in 'period' months
        val incomePerMonth = cost / period

        // now call the 4-arg constructor: (name, totalCost, period, incomePerMonth)
        val goal = BudgetGoal(name, cost, period, incomePerMonth)

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

