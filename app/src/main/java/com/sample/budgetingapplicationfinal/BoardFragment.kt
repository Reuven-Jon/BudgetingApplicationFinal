package com.sample.budgetingapplicationfinal

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import com.sample.budgetingapplicationfinal.databinding.FragmentBoardBinding

class BoardFragment : Fragment(R.layout.fragment_board) {
    private var _binding: FragmentBoardBinding? = null
    private val binding get() = _binding!!
    private lateinit var goal: BudgetGoal

    companion object {
        private const val ARG_GOAL = "arg_goal"
        fun newInstance(goal: BudgetGoal) = BoardFragment().apply {
            arguments = Bundle().apply { putParcelable(ARG_GOAL, goal) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBoardBinding.bind(view)

        @Suppress("DEPRECATION")
        goal = arguments?.getParcelable(ARG_GOAL) ?: return

        // navigation buttons
        binding.btnGoToIncome.setOnClickListener {
            startActivity(Intent(requireContext(), IncomeActivity::class.java))
        }
        binding.btnGoToExpense.setOnClickListener {
            startActivity(Intent(requireContext(), ExpenseActivity::class.java))
        }

        // initialize progress display
        binding.tvProgress.text = getString(R.string.progress_format, 0)

        binding.btnUpdateProgress.setOnClickListener {
            val entered = binding.etManualProgress.text.toString().toDoubleOrNull()
            if (entered == null) {
                Toast.makeText(requireContext(),
                    getString(R.string.error_invalid_number),
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1) clamp & compute percent
            val saved = entered.coerceIn(0.0, goal.target)
            val percent = ((saved / goal.target) * 100).toInt().coerceIn(0, 100)
            binding.tvProgress.text = getString(R.string.progress_format, percent)

            // 2) locate edge containers
            val board = binding.boardContainer
            val topRow    = board.getChildAt(0) as ViewGroup
            val rightCol  = board.getChildAt(1) as ViewGroup
            val bottomRow = board.getChildAt(2) as ViewGroup
            val leftCol   = board.getChildAt(3) as ViewGroup

            // 3) gather TextViews in order
            val cells = mutableListOf<TextView>().apply {
                topRow.children.filterIsInstance<TextView>().forEach    { add(it) }
                rightCol.children.filterIsInstance<TextView>().forEach  { add(it) }
                bottomRow.children.filterIsInstance<TextView>()
                    .toList().asReversed().forEach { add(it) }
                leftCol.children.filterIsInstance<TextView>()
                    .toList().asReversed().forEach { add(it) }
            }

            // 4) reset & highlight one cell
            val defaultColor   = ContextCompat.getColor(requireContext(), R.color.boardCellDefault)
            val highlightColor = ContextCompat.getColor(requireContext(), R.color.boardCellFilled)
            cells.forEach { it.setBackgroundColor(defaultColor) }
            val bucket = (percent / 10).coerceIn(0, cells.lastIndex)
            cells.getOrNull(bucket)?.setBackgroundColor(highlightColor)

            // 5) show random motivation
            val msgs = listOf(
                "Awesome—you're $percent% there!",
                "Keep it up! $percent% done 🎉",
                "$percent% closer to ${goal.name}! 💪",
                "Only ${100 - percent}% left—go you!",
                "You're rocking this: $percent% complete!"
            )
            binding.tvMotivation.text = msgs.random()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
