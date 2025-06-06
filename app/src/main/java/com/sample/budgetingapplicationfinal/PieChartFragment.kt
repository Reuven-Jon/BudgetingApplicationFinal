package com.sample.budgetingapplicationfinal

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Fetches last 30 days of “expenses” and “incomes” from Firebase,
 * then updates CustomPieChartView and animates the slices.
 */
class PieChartFragment : Fragment() {

    private lateinit var customPieChart: CustomPieChartView
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout containing CustomPieChartView
        val view = inflater.inflate(R.layout.fragment_pie_chart, container, false)
        customPieChart = view.findViewById(R.id.customPieChartView)

        // Initially show zero data
        customPieChart.setData(0.0, 0.0)
        return view
    }

    /**
     * Called by PieChartActivity when “REFRESH CHART” is tapped.
     * Fetches sums for last 30 days, updates view, and animates.
     */
    fun loadChartData() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid

        val expensesRef = FirebaseDatabase
            .getInstance()
            .getReference("users").child(uid).child("expenses")

        val incomesRef = FirebaseDatabase
            .getInstance()
            .getReference("users").child(uid).child("incomes")

        val today = LocalDate.now()
        val cutoffDate = today.minusDays(30)

        // 1) Sum expenses
        var totalExpenses = 0.0
        expensesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { child ->
                    val expEntry = child.getValue(ExpenseEntry::class.java)
                    if (expEntry != null) {
                        val expDate = LocalDate.parse(expEntry.date, dateFormatter)
                        if (!expDate.isBefore(cutoffDate)) {
                            totalExpenses += expEntry.amount
                        }
                    }
                }
                // 2) Now sum incomes
                fetchIncomes(cutoffDate, incomesRef, totalExpenses)
            }
            override fun onCancelled(error: DatabaseError) {
                customPieChart.setData(0.0, 0.0)
                animatePie(0f, 0f)
            }
        })
    }

    private fun fetchIncomes(
        cutoffDate: LocalDate,
        incomesRef: DatabaseReference,
        totalExpenses: Double
    ) {
        var totalIncomes = 0.0
        incomesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { child ->
                    val incEntry = child.getValue(Income::class.java)
                    if (incEntry != null) {
                        val incDate = LocalDate.parse(incEntry.date, dateFormatter)
                        if (!incDate.isBefore(cutoffDate)) {
                            totalIncomes += incEntry.amount
                        }
                    }
                }
                // 3) Update view & start animation
                customPieChart.setData(totalIncomes, totalExpenses)
                animatePie(totalIncomes.toFloat(), totalExpenses.toFloat())
            }
            override fun onCancelled(error: DatabaseError) {
                customPieChart.setData(0.0, 0.0)
                animatePie(0f, 0f)
            }
        })
    }

    /** Animate animationProgress from 0→1 with a bounce. */
    private fun animatePie(income: Float, expense: Float) {
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000L
            interpolator = OvershootInterpolator(1.5f)
            addUpdateListener { anim ->
                val prog = anim.animatedValue as Float
                customPieChart.animationProgress = prog
            }
        }
        animator.start()
    }
}
