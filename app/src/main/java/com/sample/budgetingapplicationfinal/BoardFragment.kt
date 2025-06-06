package com.sample.budgetingapplicationfinal

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
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
import kotlin.math.roundToInt

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
        goal = arguments?.getParcelable(ARG_GOAL) ?: BudgetGoal(
            name = "Temporary Goal",
            target = 200_000.0,
            periodMonths = 12,
            incomePerMonth = 15_000.0
        )

        // 2) Init FirebaseAuth & guard
        auth = Firebase.auth
        if (auth.currentUser == null) {
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
                    // percentage already between 0–100
                    updateBoard(saved.score)
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
            val percent = ((entered.coerceIn(0.0, goal.target) / goal.target) * 100)
                .roundToInt().coerceIn(0, 100)

            updateBoard(percent)

            // 8) Save to Firebase
            progressRef.setValue(GameProgress(level = 0, score = percent))
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


    private fun updateBoard(percent: Int) {
        // 1) Update progress label
        binding.tvProgress.text =
            getString(R.string.progress_format, percent)

        // 2) Collect TextViews in ring order
        val board = binding.boardContainer
        val topRow    = board.getChildAt(0) as ViewGroup
        val rightCol  = board.getChildAt(1) as ViewGroup
        val bottomRow = board.getChildAt(2) as ViewGroup
        val leftCol   = board.getChildAt(3) as ViewGroup

        val cells = mutableListOf<TextView>().apply {
            addAll(topRow.children.filterIsInstance<TextView>())
            addAll(rightCol.children.filterIsInstance<TextView>())
            bottomRow.children
                .filterIsInstance<TextView>()
                .toList().asReversed()
                .forEach { add(it) }
            leftCol.children
                .filterIsInstance<TextView>()
                .toList().asReversed()
                .forEach { add(it) }
        }

        // 3) Pick the bucket index (0–10% → index 0, etc.)
        val idx = (percent / 10).coerceIn(0, cells.lastIndex)
        val target = cells[idx]

        // 4) Prep marker
        val marker = binding.ivMarker.apply {
            visibility = View.VISIBLE
            bringToFront()
        }

        // 5) Compute its new center inside the board frame
        val boardPos  = IntArray(2).also { board.getLocationOnScreen(it) }
        val targetPos = IntArray(2).also { target.getLocationOnScreen(it) }
        val centerX = targetPos[0] - boardPos[0] + target.width/2f - marker.width/2f
        val centerY = targetPos[1] - boardPos[1] + target.height/2f - marker.height/2f

        // 6) Bounce it there
        marker.animate()
            .translationX(centerX)
            .translationY(centerY)
            .setInterpolator(BounceInterpolator())
            .setDuration(600)
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
