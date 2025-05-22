package com.sample.budgetingapplicationfinal

import android.app.AlertDialog                      // for dialogs
import android.content.Intent                       // to start activities
import android.graphics.drawable.ColorDrawable      // to style dialog bg
import android.os.Bundle
import android.view.LayoutInflater                  // to inflate XML
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu                     // for the hamburger menu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*                // Realtime DB
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

        // 1. Guard: if user isn’t signed in, send to MainActivity
        auth = Firebase.auth
        if (auth.currentUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // 2. Wire hamburger button to show our popup menu
        binding.menuButton.setOnClickListener { view ->
            PopupMenu(this, view).apply {
                menuInflater.inflate(R.menu.menu_expense, menu)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.nav_income -> {
                            startActivity(Intent(this@ExpenseActivity, IncomeActivity::class.java))
                            true
                        }
                        R.id.nav_board_game -> {
                            startActivity(Intent(this@ExpenseActivity, MainActivity::class.java)
                                .putExtra("startFragment", "board"))
                            true
                        }
                        R.id.nav_login -> {
                            startActivity(Intent(this@ExpenseActivity, MainActivity::class.java)
                                .putExtra("startFragment", "login"))
                            true
                        }
                        else -> false
                    }
                }
                show()  // display the menu
            }
        }

        // 3. Point db to /users/{uid}/expenses
        db = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(auth.currentUser!!.uid)
            .child("expenses")

        // 4. Setup RecyclerView with FirebaseUI
        val options = FirebaseRecyclerOptions.Builder<ExpenseEntry>()
            .setQuery(db, ExpenseEntry::class.java)
            .build()
        adapter = object : FirebaseRecyclerAdapter<ExpenseEntry, ExpenseViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                ExpenseViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_expense_card, parent, false)
                )

            override fun onBindViewHolder(
                holder: ExpenseViewHolder,
                position: Int,
                model: ExpenseEntry
            ) {
                holder.tvCat.text  = model.category
                holder.tvAmt.text  = String.format("R%,.2f", model.amount)
                holder.tvDate.text = model.date

                // Info button shows details dialog
                holder.btnInfo.setOnClickListener {
                    AlertDialog.Builder(this@ExpenseActivity)
                        .setTitle("Expense Details")
                        .setMessage(
                            "Category: ${model.category}\n" +
                                    "Amount:   R${model.amount}\n" +
                                    "Date:     ${model.date}"
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

        // 5. Pull-to-refresh simply reloads data
        binding.swipeContainerExpense.setOnRefreshListener {
            adapter.notifyDataSetChanged()
            binding.swipeContainerExpense.isRefreshing = false
        }

        // 6. Sum all expenses and show total or empty view
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var total = 0.0
                snapshot.children.forEach {
                    it.getValue(ExpenseEntry::class.java)?.let { exp ->
                        total += exp.amount
                    }
                }
                binding.totalExpenseAmount.text = String.format("R%,.2f", total)
                binding.emptyView.visibility =
                    if (total == 0.0) View.VISIBLE else View.GONE
            }
            override fun onCancelled(error: DatabaseError) { /* no-op */ }
        })

        // 7. FAB opens the add-expense dialog
        binding.addExpenseFab.setOnClickListener { showExpensePopup() }
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening() // begin listening for DB updates
    }

    override fun onStop() {
        adapter.stopListening()  // stop listening
        super.onStop()
    }

    private fun showExpensePopup() {
        val popup = LayoutInflater.from(this)
            .inflate(R.layout.dialog_add_expense, null, false)
        val dialog = AlertDialog.Builder(this)
            .setView(popup)
            .create()

        val etCat  = popup.findViewById<EditText>(R.id.expenseCategoryInput)
        val etAmt  = popup.findViewById<EditText>(R.id.expenseAmountInput)
        val btnSave = popup.findViewById<Button>(R.id.submitExpenseButton)

        btnSave.setOnClickListener {
            val cat = etCat.text.toString().trim()
            val amt = etAmt.text.toString().toDoubleOrNull()
            when {
                cat.isEmpty() -> etCat.error = "Required"
                amt == null   -> etAmt.error = "Enter number"
                else -> {
                    val entry = ExpenseEntry(cat, amt, LocalDate.now().toString())
                    db.push().setValue(entry)
                    Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        dialog.show()
    }

    // ViewHolder for each expense card
    class ExpenseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCat   = view.findViewById<TextView>(R.id.expenseCategory)
        val tvAmt   = view.findViewById<TextView>(R.id.expenseAmount)
        val tvDate  = view.findViewById<TextView>(R.id.expenseDate)
        val btnInfo = view.findViewById<ImageButton>(R.id.expenseInfoButton)
    }
}
