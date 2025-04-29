package com.sample.budgetingapplicationfinal

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.sample.budgetingapplicationfinal.databinding.FragmentBoardBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BoardFragment : Fragment(R.layout.fragment_board) {
    private var binding: FragmentBoardBinding? = null
    private lateinit var cells: MutableList<BoardCell>
    private lateinit var adapter: BoardAdapter

    companion object {
        private const val ARG_GOAL = "arg_goal"
        fun newInstance(goal: BudgetGoal) = BoardFragment().apply {
            arguments = Bundle().apply { putParcelable(ARG_GOAL, goal) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBoardBinding.bind(view)

        @Suppress("DEPRECATION")
        val goal = arguments?.getParcelable<BudgetGoal>(ARG_GOAL) ?: return
        val totalCells = 100
        cells = MutableList(totalCells) { BoardCell(it) }

        adapter = BoardAdapter(cells)
        binding?.rvBoard?.layoutManager = GridLayoutManager(context, 10)
        binding?.rvBoard?.adapter = adapter

        val stepSize = Gamification.calculateStepSize(goal, totalCells)
        var saved = 0.0

        lifecycleScope.launch {
            while (saved <= goal.target) {
                Gamification.fillCells(cells, saved, stepSize)
                adapter.notifyDataSetChanged()
                val percent =
                    (saved / goal.target * 100).toInt().coerceAtMost(100)
                binding?.tvProgress?.text =
                    getString(R.string.progress_format, percent)
                delay(500)
                saved += goal.incomePerMonth
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
