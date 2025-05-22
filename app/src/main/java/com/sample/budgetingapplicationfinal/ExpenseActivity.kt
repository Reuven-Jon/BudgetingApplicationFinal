package com.sample.budgetingapplicationfinal

import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.sample.budgetingapplicationfinal.databinding.ActivityExpenseBinding
import java.time.LocalDate

class ExpenseActivity : AppCompatActivity() {
    private lateinit var binding: ActivityExpenseBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: DatabaseReference
    private lateinit var adapter: FirebaseRecyclerAdapter<ExpenseEntry, ExpenseViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Auth check + nav back to Income
        auth = Firebase.auth
        binding.backArrow.setOnClickListener {
            startActivity(Intent(this, IncomeActivity::class.java))
            finish()
        }

        // 2. Point at /users/{uid}/expenses
        val uid = auth.currentUser?.uid ?: return finish()
        db = FirebaseDatabase.getInstance()
            .getReference("users").child(uid).child("expenses")

        // 3. Recycler + FirebaseUI
        val opts = FirebaseRecyclerOptions.Builder<ExpenseEntry>()
            .setQuery(db, ExpenseEntry::class.java)
            .build()
        adapter = object : FirebaseRecyclerAdapter<ExpenseEntry, ExpenseViewHolder>(opts) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                ExpenseViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_expense_card, parent, false)
                )
            override fun onBindViewHolder(holder: ExpenseViewHolder, pos: Int, model: ExpenseEntry) {
                holder.tvCat.text    = model.category
                holder.tvAmt.text    = String.format("R%,.2f", model.amount)
                holder.tvDate.text   = model.date

                // Show info dialog on tap
                holder.btnInfo.setOnClickListener {
                    AlertDialog.Builder(this@ExpenseActivity)
                        .setTitle("Expense Details")
                        .setMessage("""
              Category: ${model.category}
              Amount: R${model.amount}
              Date: ${model.date}
            """.trimIndent())
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }
        binding.expenseRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ExpenseActivity)
            adapter = this@ExpenseActivity.adapter
        }

        // 4. Swipe-to-refresh
        binding.swipeContainerExpense.setOnRefreshListener {
            adapter.notifyDataSetChanged()
            binding.swipeContainerExpense.isRefreshing = false
        }

        // 5. Empty-state & total summary
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                var total = 0.0
                snap.children.forEach {
                    it.getValue(ExpenseEntry::class.java)?.let { exp ->
                        total += exp.amount
                    }
                }
                binding.totalExpenseAmount.text = String.format("R%,.2f", total)
                binding.emptyExpenseView.visibility =
                    if (total == 0.0) View.VISIBLE else View.GONE
            }
            override fun onCancelled(err: DatabaseError) {}
        })

        // 6. FAB to add one
        binding.addExpenseFab.setOnClickListener { showExpensePopup() }
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }
    override fun onStop() {
        adapter.stopListening()
        super.onStop()
    }

    private fun showExpensePopup() {
        // exactly the same pattern as IncomeActivity
        val popup = LayoutInflater.from(this)
            .inflate(R.layout.dialog_add_expense, null)
        val dlg = AlertDialog.Builder(this)
            .setView(popup)
            .create()

        val etCat   = popup.findViewById<EditText>(R.id.expenseCategoryInput)
        val etAmt   = popup.findViewById<EditText>(R.id.expenseAmountInput)
        val btnSave = popup.findViewById<Button>(R.id.submitExpenseButton)

        btnSave.setOnClickListener {
            val cat = etCat.text.toString().trim()
            val amt = etAmt.text.toString().toDoubleOrNull()
            when {
                cat.isEmpty() -> etCat.error = "Required"
                amt == null   -> etAmt.error = "Enter number"
                else -> {
                    val e = ExpenseEntry(cat, amt, LocalDate.now().toString())
                    db.push().setValue(e)
                    Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show()
                    dlg.dismiss()
                }
            }
        }

        dlg.window?.setBackgroundDrawable(ColorDrawable(0))
        dlg.show()
    }

    // ViewHolder for item_expense_card.xml
    class ExpenseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCat  = view.findViewById<TextView>(R.id.expenseCategory)
        val tvAmt  = view.findViewById<TextView>(R.id.expenseAmount)
        val tvDate = view.findViewById<TextView>(R.id.expenseDate)
        val btnInfo= view.findViewById<ImageButton>(R.id.expenseInfoButton)
    }
}
