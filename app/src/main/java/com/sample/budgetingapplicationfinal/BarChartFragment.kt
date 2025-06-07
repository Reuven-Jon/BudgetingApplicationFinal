package com.sample.budgetingapplicationfinal

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BarChartFragment : Fragment(R.layout.fragment_bar_chart) {
    private val dateFmt = DateTimeFormatter.ISO_LOCAL_DATE
    private val auth get() = FirebaseAuth.getInstance()
    private val uid get() = auth.currentUser!!.uid

    private lateinit var spinnerMonth: Spinner
    private lateinit var cbIncome: CheckBox
    private lateinit var cbExpense: CheckBox
    private lateinit var spinnerIncCat: Spinner
    private lateinit var spinnerExpCat: Spinner
    private lateinit var btnApply: Button
    private lateinit var barChart: CustomBarChartView

    // Adapters and data stores
    private val months = listOf("All","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    private lateinit var incCatAdapter: ArrayAdapter<String>
    private lateinit var expCatAdapter: ArrayAdapter<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        spinnerMonth = view.findViewById(R.id.spinnerMonth)
        cbIncome     = view.findViewById(R.id.cbIncome)
        cbExpense    = view.findViewById(R.id.cbExpense)
        spinnerIncCat= view.findViewById(R.id.spinnerIncomeCategory)
        spinnerExpCat= view.findViewById(R.id.spinnerExpenseCategory)
        btnApply     = view.findViewById(R.id.btnApplyFilters)
        barChart     = view.findViewById(R.id.barChartView)

        // 1) Month spinner
        spinnerMonth.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            months
        )

        // 2) Category adapters start with only "All"
        incCatAdapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_dropdown_item, mutableListOf("All"))
        spinnerIncCat.adapter = incCatAdapter

        expCatAdapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_dropdown_item, mutableListOf("All"))
        spinnerExpCat.adapter = expCatAdapter

        // 3) Load distinct categories from Firebase once
        loadCategories()

        // 4) Apply filters on click
        btnApply.setOnClickListener { loadBarData() }

        // Initial draw
        loadBarData()
    }

    private fun loadCategories() {
        val db = FirebaseDatabase.getInstance()
        val incRef = db.getReference("users").child(uid).child("incomes")
        incRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val list = mutableSetOf<String>()
                snap.children.forEach { child ->
                    child.getValue(Income::class.java)?.category?.let { list += it }
                }
                // Update adapter: keep "All" at index 0
                incCatAdapter.clear()
                incCatAdapter.add("All")
                incCatAdapter.addAll(list.sorted())
                incCatAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(err: DatabaseError) {}
        })

        val expRef = db.getReference("users").child(uid).child("expenses")
        expRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val list = mutableSetOf<String>()
                snap.children.forEach { child ->
                    child.getValue(Expense::class.java)?.category?.let { list += it }
                }
                expCatAdapter.clear()
                expCatAdapter.add("All")
                expCatAdapter.addAll(list.sorted())
                expCatAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(err: DatabaseError) {}
        })
    }

    private fun loadBarData() {
        // Read filters
        val monthIdx = spinnerMonth.selectedItemPosition
        val incomeOn  = cbIncome.isChecked
        val expenseOn = cbExpense.isChecked
        val incCatSel = spinnerIncCat.selectedItem as String
        val expCatSel = spinnerExpCat.selectedItem as String

        // Match month helper
        fun matchesMonth(periodStr: String): Boolean {
            // spinner position 0 = “All”
            if (monthIdx == 0) return true
            // spinner “Jan”…”Dec”
            val selected = spinnerMonth.selectedItem as String
            return periodStr == selected
        }

        var sumInc = 0f
        var sumExp = 0f

        val db = FirebaseDatabase.getInstance()
        val incRef = db.getReference("users").child(uid).child("incomes")
        val expRef = db.getReference("users").child(uid).child("expenses")

        // Sum incomes
        incRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                snap.children.forEach { c ->
                    val inc = c.getValue(Income::class.java) ?: return@forEach
                    if (incomeOn
                                          && matchesMonth(inc.period)               // ← use period
                                          && (incCatSel=="All" || inc.category==incCatSel)
                                    ) sumInc += inc.amount.toFloat()
                }
                // Then sum expenses
                expRef.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(esnap: DataSnapshot) {
                        esnap.children.forEach { c2 ->
                            val ex = c2.getValue(Expense::class.java) ?: return@forEach
                            if (expenseOn
                                                           && matchesMonth(ex.period)             // ← use period
                                                           && (expCatSel=="All" || ex.category==expCatSel)
                                                      ) sumExp += ex.amount.toFloat()
                        }
                        // Finally update chart
                        val map = linkedMapOf<String,Float>()
                        if (incomeOn)  map["Income"]  = sumInc
                        if (expenseOn) map["Expense"] = sumExp
                        barChart.setData(map)
                    }
                    override fun onCancelled(err: DatabaseError) {}
                })
            }
            override fun onCancelled(err: DatabaseError) {}
        })
    }
}
