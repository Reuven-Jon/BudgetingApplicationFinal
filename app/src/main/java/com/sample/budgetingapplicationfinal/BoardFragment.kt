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
import com.google.firebase.auth.FirebaseAuth                        // FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot                   // for DB reads
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference              // DB reference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.sample.budgetingapplicationfinal.databinding.FragmentBoardBinding
import java.time.LocalDate

class BoardFragment : Fragment(R.layout.fragment_board) {
    private var _binding: FragmentBoardBinding? = null
    private val binding get() = _binding!!

    // Firebase instances
    private lateinit var auth: FirebaseAuth
    private lateinit var progressRef: DatabaseReference

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

        // 1) Get the BudgetGoal from args
        @Suppress("DEPRECATION")
        goal = arguments?.getParcelable(ARG_GOAL) ?: return

        // 2) Init FirebaseAuth & guard
        auth = Firebase.auth
        if (auth.currentUser == null) {
            // not signed in → go back to login flow
            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish()
            return
        }

        // 3) Point at /users/{uid}/gameProgress
        val uid = auth.currentUser!!.uid
        progressRef = FirebaseDatabase
            .getInstance()
            .getReference("users")
            .child(uid)
            .child("gameProgress")

        // 4) Load existing progress if any
        progressRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.getValue(GameProgress::class.java)?.let { saved ->
                    // interpret `score` as percent
                    val percent = saved.score.coerceIn(0,100)
                    updateBoard(percent)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    "Failed to load progress: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        // 5) Navigation buttons
        binding.btnGoToIncome.setOnClickListener {
            startActivity(Intent(requireContext(), IncomeActivity::class.java))
        }
        binding.btnGoToExpense.setOnClickListener {
            startActivity(Intent(requireContext(), ExpenseActivity::class.java))
        }

        // 6) Init with 0% until load completes
        binding.tvProgress.text = getString(R.string.progress_format, 0)

        // 7) Handle manual progress updates
        binding.btnUpdateProgress.setOnClickListener {
            val entered = binding.etManualProgress.text.toString().toDoubleOrNull()
            if (entered == null) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_invalid_number),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // clamp & compute percent
            val saved = entered.coerceIn(0.0, goal.target)
            val percent = ((saved / goal.target) * 100).toInt().coerceIn(0, 100)

            // update UI and board cells
            updateBoard(percent)

            // 8) Save to Firebase
            val progress = GameProgress(level = 0, score = percent)
            progressRef.setValue(progress)
                .addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "Failed to save: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            // 9) Show motivation
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

    // Extracted to avoid duplication
    private fun updateBoard(percent: Int) {
        binding.tvProgress.text = getString(R.string.progress_format, percent)

        // locate cells
        val board = binding.boardContainer
        val topRow    = board.getChildAt(0) as ViewGroup
        val rightCol  = board.getChildAt(1) as ViewGroup
        val bottomRow = board.getChildAt(2) as ViewGroup
        val leftCol   = board.getChildAt(3) as ViewGroup

        // gather TextView cells in ring order
        val cells = mutableListOf<TextView>().apply {
            topRow.children.filterIsInstance<TextView>().forEach    { add(it) }
            rightCol.children.filterIsInstance<TextView>().forEach  { add(it) }
            bottomRow.children.filterIsInstance<TextView>()
                .toList().asReversed().forEach { add(it) }
            leftCol.children.filterIsInstance<TextView>()
                .toList().asReversed().forEach { add(it) }
        }

        // reset all, then highlight bucket
        val defaultColor   = ContextCompat.getColor(requireContext(), R.color.boardCellDefault)
        val highlightColor = ContextCompat.getColor(requireContext(), R.color.boardCellFilled)
        cells.forEach { it.setBackgroundColor(defaultColor) }

        val bucket = (percent / 10).coerceIn(0, cells.lastIndex)
        cells.getOrNull(bucket)?.setBackgroundColor(highlightColor)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
