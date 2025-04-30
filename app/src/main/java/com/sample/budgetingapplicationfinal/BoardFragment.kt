package com.sample.budgetingapplicationfinal

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment                                     // Fragments guide → https://developer.android.com/guide/fragments
import androidx.recyclerview.widget.GridLayoutManager                    // GridLayoutManager docs → https://developer.android.com/reference/androidx/recyclerview/widget/GridLayoutManager
import com.sample.budgetingapplicationfinal.databinding.FragmentBoardBinding // View Binding → https://developer.android.com/topic/libraries/view-binding

class BoardFragment : Fragment(R.layout.fragment_board) {               // Fragment ctor API → https://developer.android.com/reference/androidx/fragment/app/Fragment#Fragment(int)
    private var binding: FragmentBoardBinding? = null
    private lateinit var cells: MutableList<BoardCell>
    private lateinit var adapter: BoardAdapter
    private lateinit var goal: BudgetGoal
    private var stepSize: Double = 0.0

    companion object {
        private const val ARG_GOAL = "arg_goal"
        fun newInstance(goal: BudgetGoal) = BoardFragment().apply {
            arguments = Bundle().apply { putParcelable(ARG_GOAL, goal) }   // getParcelable → https://developer.android.com/reference/android/os/Bundle#getParcelable(java.lang.String)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBoardBinding.bind(view)

        @Suppress("DEPRECATION")
        goal = arguments?.getParcelable(ARG_GOAL) ?: return               // retrieve passed Goal

        // 1) Prepare a 10×10 board
        val totalCells = 100
        cells = MutableList(totalCells) { BoardCell(it) }               // MutableList init → https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/mutable-list-of.html
        stepSize = goal.target / totalCells                             // compute value per cell

        // 2) RecyclerView + GridLayoutManager setup
        adapter = BoardAdapter(cells)
        binding!!.rvBoard.layoutManager = GridLayoutManager(context, 10)
        binding!!.rvBoard.adapter = adapter

        // 3) Initialize UI
        binding!!.tvProgress.text   = getString(R.string.progress_format, 0)  // Context.getString(format) → https://developer.android.com/reference/android/content/Context#getString(int,java.lang.Object...)
        binding!!.tvMotivation.text = getString(R.string.motivation_initial)

        // 4) Handle “Update Progress” button
        binding!!.btnUpdateProgress.setOnClickListener {
            // parse user input safely
            val entered = binding!!.etManualProgress.text.toString()
                .toDoubleOrNull()                                           // toDoubleOrNull → https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/to-double-or-null.html
            if (entered == null) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_invalid_number),
                    Toast.LENGTH_SHORT                                     // Toast guide → https://developer.android.com/guide/topics/ui/notifiers/toasts
                ).show()
                return@setOnClickListener
            }

            // cap at total target
            val savedSoFar = entered.coerceAtMost(goal.target)             // coerceAtMost → https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.ranges/coerce-at-most.html

            // fill cells & refresh
            Gamification.fillCells(cells, savedSoFar, stepSize)
            adapter.notifyDataSetChanged()                                 // notifyDataSetChanged → https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.Adapter#notifyDataSetChanged()

            // compute and display %
            val percent = ((savedSoFar / goal.target) * 100)
                .toInt()
                .coerceAtMost(100)
            binding!!.tvProgress.text = getString(R.string.progress_format, percent)

            // show a random motivational message
            val messages = listOf(
                "Awesome—you're $percent% there!",
                "Keep it up! $percent% done 🎉",
                "$percent% closer to ${goal.name}! 💪",
                "Only ${100 - percent}% left—go you!",
                "You're rocking this: $percent% complete!"
            )
            binding!!.tvMotivation.text = messages.random()                // random() → https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/random.html
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null                                                   // avoid memory leaks → https://developer.android.com/topic/libraries/view-binding#fragments
    }
}
