package com.sample.budgetingapplicationfinal

import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var db:      DatabaseReference
    private lateinit var adapter: FirebaseRecyclerAdapter<Expense, ExpenseViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Auth guard
        auth = Firebase.auth
        if (auth.currentUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        val uid = auth.currentUser!!.uid

        // 2) Hamburger menu
        binding.menuButton.setOnClickListener { view ->
            PopupMenu(this, view).apply {
                menuInflater.inflate(R.menu.menu_expense, menu)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.nav_income -> {
                            startActivity(Intent(this@ExpenseActivity, IncomeActivity::class.java))
                            true
                        }
                        R.id.nav_bar_chart -> {
                            startActivity(Intent(this@ExpenseActivity, BarChartActivity::class.java))
                            true
                        }
                        R.id.nav_board_game -> {
                            startActivity(
                                Intent(this@ExpenseActivity, MainActivity::class.java)
                                    .putExtra("startFragment", "board")
                            )
                            true
                        }
                        R.id.nav_login -> {
                            startActivity(
                                Intent(this@ExpenseActivity, MainActivity::class.java)
                                    .putExtra("startFragment", "login")
                            )
                            true
                        }
                        else -> false
                    }
                }
                show()
            }
        }

        // 3) DB reference
        db = FirebaseDatabase
            .getInstance()
            .getReference("users")
            .child(uid)
            .child("expenses")

        // 4) RecyclerView + FirebaseUI
        val options = FirebaseRecyclerOptions.Builder<Expense>()
            .setQuery(db, Expense::class.java)
            .build()
        adapter = object : FirebaseRecyclerAdapter<Expense, ExpenseViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                ExpenseViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_expense_card, parent, false)
                )

            override fun onBindViewHolder(
                holder: ExpenseViewHolder,
                position: Int,
                model: Expense
            ) {
                holder.tvCat.text    = model.category
                holder.tvAmt.text    = String.format("R%,.2f", model.amount)
                holder.tvDate.text   = model.date
                holder.tvPeriod.text = model.period
                holder.tvSource.text = model.source

                holder.btnInfo.setOnClickListener {
                    AlertDialog.Builder(this@ExpenseActivity)
                        .setTitle("Expense Details")
                        .setMessage(
                            "Category: ${model.category}\n" +
                                    "Amount:   R${model.amount}\n" +
                                    "Date:     ${model.date}\n" +
                                    "Period:   ${model.period}\n" +
                                    "Source:   ${model.source}"
                        )
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }
        binding.expenseRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ExpenseActivity)
            adapter = this@ExpenseActivity.adapter
        }

        // 5) Swipe-to-refresh
        binding.swipeContainerExpense.setOnRefreshListener {
            adapter.notifyDataSetChanged()
            binding.swipeContainerExpense.isRefreshing = false
        }

        // 6) Sum total
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var total = 0.0
                snapshot.children.forEach {
                    it.getValue(Expense::class.java)?.let { exp ->
                        total += exp.amount
                    }
                }
                binding.totalExpenseAmount.text =
                    String.format("R%,.2f", total)
                binding.emptyView.visibility =
                    if (total == 0.0) View.VISIBLE else View.GONE
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // 7) FAB → add dialog
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
        val popup = LayoutInflater.from(this)
            .inflate(R.layout.dialog_add_expense, null, false)
        val dialog = AlertDialog.Builder(this)
            .setView(popup)
            .create()

        val etCat    = popup.findViewById<EditText>(R.id.expenseCategoryInput)
        val etAmt    = popup.findViewById<EditText>(R.id.expenseAmountInput)
        val spinner  = popup.findViewById<Spinner>(R.id.spinnerPeriod)
        val etSource = popup.findViewById<EditText>(R.id.expenseSourceInput)
        val btnSave  = popup.findViewById<Button>(R.id.submitExpenseButton)

        // Populate months spinner
        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Jan","Feb","Mar","Apr","May","Jun",
                "Jul","Aug","Sep","Oct","Nov","Dec")
        )

        btnSave.setOnClickListener {
            val cat    = etCat.text.toString().trim()
            val amt    = etAmt.text.toString().toDoubleOrNull()
            val period = spinner.selectedItem as String
            val src    = etSource.text.toString().trim()

            when {
                cat.isEmpty()  -> etCat.error = "Required"
                amt == null    -> etAmt.error = "Enter number"
                src.isEmpty()  -> etSource.error = "Required"
                else -> {
                    val entry = Expense(
                        category = cat,
                        amount   = amt,
                        date     = LocalDate.now().toString(),
                        period   = period,
                        source   = src
                    )
                    db.push().setValue(entry)
                    Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        dialog.show()
    }

    // ViewHolder
    class ExpenseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCat    = view.findViewById<TextView>(R.id.expenseCategory)
        val tvAmt    = view.findViewById<TextView>(R.id.expenseAmount)
        val tvDate   = view.findViewById<TextView>(R.id.expenseDate)
        val tvPeriod = view.findViewById<TextView>(R.id.expensePeriod)
        val tvSource = view.findViewById<TextView>(R.id.expenseSource)
        val btnInfo  = view.findViewById<ImageButton>(R.id.expenseInfoButton)
    }
}
