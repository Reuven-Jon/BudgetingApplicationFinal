package com.sample.budgetingapplicationfinal

import android.app.AlertDialog                       // for popup
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater                    // to inflate XML
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity        // base Activity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth          // auth calls
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*                 // Realtime DB
import com.google.firebase.ktx.Firebase
import com.sample.budgetingapplicationfinal.databinding.ActivityIncomeBinding
import java.time.LocalDate
import android.view.ViewGroup


class IncomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIncomeBinding   // view binding
    private lateinit var auth: FirebaseAuth              // FirebaseAuth
    private lateinit var database: DatabaseReference     // DB ref for /users/{uid}/incomes
    private lateinit var adapter: FirebaseRecyclerAdapter<Income, IncomeViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomeBinding.inflate(layoutInflater) // inflate layout
        setContentView(binding.root)                            // set content view

        auth = Firebase.auth                                    // get auth instance
        val uid = auth.currentUser?.uid                         // current user id
            ?: run {
                finish()                                        // exit if no user
                return
            }

// inside onCreate(...)
        binding.backArrow.setOnClickListener {
            // 1. Launch ExpenseActivity
            startActivity(Intent(this, ExpenseActivity::class.java))
            // 2. Finish IncomeActivity so the back stack is clean
            finish()
        }

        // Point database at /users/{uid}/incomes
        database = FirebaseDatabase
            .getInstance()
            .getReference("users")
            .child(uid)
            .child("incomes")

        // RecyclerView setup using FirebaseUI
        val options = FirebaseRecyclerOptions.Builder<Income>()
            .setQuery(database, Income::class.java)             // map DB → Income
            .build()
        adapter = object : FirebaseRecyclerAdapter<Income, IncomeViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                IncomeViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_income_card, parent, false)
                )
            override fun onBindViewHolder(holder: IncomeViewHolder, position: Int, model: Income) {
                holder.tvCategory.text = model.category         // show category
                holder.tvAmount.text   = String.format("R%,.2f", model.amount) // show amount
                holder.tvDate.text     = model.date             // show date
                holder.tvPeriod.text   = model.period           // show period
                holder.tvSource.text   = model.source           // show source

                // Info-button tap shows full details
                holder.btnInfo.setOnClickListener {
                    AlertDialog.Builder(this@IncomeActivity)
                        .setTitle("Income Details")
                        .setMessage("""
                            Category: ${model.category}
                            Amount: R${model.amount}
                            Date: ${model.date}
                            Period: ${model.period}
                            Source: ${model.source}
                        """.trimIndent())
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }
        binding.incomeRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@IncomeActivity) // vertical list
            adapter = this@IncomeActivity.adapter
        }

        // FAB opens the popup
        binding.addIncomeFab.setOnClickListener { showIncomePopup() }
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()                                // begin DB listening
    }

    override fun onStop() {
        adapter.stopListening()                                 // stop DB listening
        super.onStop()
    }

    private fun showIncomePopup() {
        // Inflate your new dialog layout with category, amount, period, source
        val popup = LayoutInflater.from(this)
            .inflate(R.layout.dialog_add_income, null)
        val dialog = AlertDialog.Builder(this)
            .setView(popup)
            .create()

        // Hook fields
        val etCategory = popup.findViewById<EditText>(R.id.etCategory)
        val etAmount   = popup.findViewById<EditText>(R.id.etAmount)
        val spinner    = popup.findViewById<Spinner>(R.id.spinnerPeriod)
        val etSource   = popup.findViewById<EditText>(R.id.etSource)
        val btnSubmit  = popup.findViewById<Button>(R.id.btnSubmit)

        // Populate period choices (example with months)
        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        )

        // Handle form submission
        btnSubmit.setOnClickListener {
            val cat    = etCategory.text.toString().trim()                 // read category
            val amt    = etAmount.text.toString().toDoubleOrNull()         // read amount
            val period = spinner.selectedItem as String                    // read period
            val src    = etSource.text.toString().trim()                   // read source

            // Simple validation
            when {
                cat.isEmpty()  -> etCategory.error = "Required"
                amt == null    -> etAmount.error   = "Enter number"
                src.isEmpty()  -> etSource.error   = "Required"
                else -> {
                    // Build Income object
                    val entry = Income(
                        category = cat,
                        amount   = amt,
                        date     = LocalDate.now().toString(),
                        period   = period,
                        source   = src
                    )
                    // Save to Realtime DB
                    database.push().setValue(entry)
                    Toast.makeText(this, "Income added", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()                                      // close popup
                }
            }
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(0))  // transparent bg
        dialog.show()                                           // display it
    }

    // ViewHolder binds your item layout views
    class IncomeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory = view.findViewById<TextView>(R.id.incomeCategory)
        val tvAmount   = view.findViewById<TextView>(R.id.incomeAmount)
        val tvDate     = view.findViewById<TextView>(R.id.incomeDate)
        val tvPeriod   = view.findViewById<TextView>(R.id.incomePeriod)
        val tvSource   = view.findViewById<TextView>(R.id.incomeSource)
        val btnInfo    = view.findViewById<ImageButton>(R.id.incomeInfoButton)
    }
}
