package com.sample.budgetingapplicationfinal

import android.app.AlertDialog                      // for pop-up dialogs
import android.content.Intent                        // to launch another Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater                   // to inflate XML
import android.view.View
import android.view.ViewGroup
import android.widget.*                              // EditText, Button, Toast
import androidx.appcompat.app.AppCompatActivity       // base class
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth         // Firebase auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*               // Firebase Realtime DB
import com.google.firebase.ktx.Firebase
import com.sample.budgetingapplicationfinal.databinding.ActivityIncomeBinding
import java.time.LocalDate

// ← NEW: import your DB manager
import com.sample.budgetingapplicationfinal.FirebaseDatabaseManager

class IncomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIncomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var adapter: FirebaseRecyclerAdapter<Income, IncomeViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Auth guard: if no user, bounce back to login-fragment host
        auth = Firebase.auth
        if (auth.currentUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Point at legacy path "/incomes/{uid}"
        val uid = auth.currentUser!!.uid
        database = FirebaseDatabase
            .getInstance()
            .getReference("incomes")
            .child(uid)

        binding.backArrow.setOnClickListener {
            startActivity(Intent(this, ExpenseActivity::class.java))
            finish()
        }

        binding.addIncomeFab.setOnClickListener { showIncomePopup() }

        // RecyclerView + FirebaseUI
        val options = FirebaseRecyclerOptions.Builder<Income>()
            .setQuery(database, Income::class.java)
            .build()
        adapter = object : FirebaseRecyclerAdapter<Income, IncomeViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                IncomeViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_income_card, parent, false)
                )
            override fun onBindViewHolder(
                holder: IncomeViewHolder,
                position: Int,
                model: Income
            ) {
                holder.source.text = model.source
                holder.date.text   = model.date
                holder.amount.text = String.format("R%,.2f", model.amount)
            }
        }
        binding.incomeRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@IncomeActivity)
            adapter = this@IncomeActivity.adapter
        }

        binding.swipeContainer.setOnRefreshListener {
            adapter.notifyDataSetChanged()
            binding.swipeContainer.isRefreshing = false
        }

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var sum = 0.0
                snapshot.children.forEach {
                    it.getValue(Income::class.java)?.let { income ->
                        sum += income.amount
                    }
                }
                binding.totalIncomeAmount.text = String.format("R%,.2f", sum)
                binding.emptyView.visibility =
                    if (sum == 0.0) View.VISIBLE else View.GONE
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        adapter.stopListening()
        super.onStop()
    }

    private fun showIncomePopup() {
        val popup = LayoutInflater.from(this)
            .inflate(R.layout.income_input_popup, null)
        val dialog = AlertDialog.Builder(this)
            .setView(popup)
            .create()

        val categoryInput = popup.findViewById<EditText>(R.id.incomeCategoryInput)
        val amountInput   = popup.findViewById<EditText>(R.id.incomeAmountInput)
        val submitButton  = popup.findViewById<Button>(R.id.submitIncomeButton)

        submitButton.setOnClickListener {
            val source = categoryInput.text.toString().trim()
            val amtText = amountInput.text.toString().trim()
            val amt = amtText.toDoubleOrNull()

            when {
                source.isEmpty() -> categoryInput.error = "Required"
                amt == null      -> amountInput.error = "Enter number"
                else -> {
                    val newIncome = Income(
                        source,
                        amt,
                        LocalDate.now().toString()
                    )

                    // 1) Legacy path
                    database.push().setValue(newIncome)

                    // 2) ALSO save under /users/{uid}/incomes
                    val uid = auth.currentUser!!.uid
                    FirebaseDatabaseManager.saveIncome(uid, newIncome)

                    Toast.makeText(
                        this,
                        String.format("Added %s – R%,.2f", source, amt),
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                }
            }
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    class IncomeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val source: TextView = view.findViewById(R.id.incomeSource)
        val date:   TextView = view.findViewById(R.id.incomeDate)
        val amount: TextView = view.findViewById(R.id.incomeAmount)
    }
}
