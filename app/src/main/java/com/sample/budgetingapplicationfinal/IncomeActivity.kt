package com.sample.budgetingapplicationfinal

import android.app.AlertDialog                      // for dialogs
import android.content.Intent                       // to start activities
import android.graphics.drawable.ColorDrawable      // to style dialog bg
import android.os.Bundle
import android.view.LayoutInflater                  // to inflate XML
import android.view.View
import android.view.ViewGroup
import android.widget.*                             // views & widgets
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*                // Realtime DB
import com.google.firebase.ktx.Firebase
import com.sample.budgetingapplicationfinal.databinding.ActivityIncomeBinding
import java.time.LocalDate
import android.widget.PopupMenu                      // for popup menu navigation

class IncomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIncomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var adapter: FirebaseRecyclerAdapter<Income, IncomeViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Auth guard: redirect if not signed in
        auth = Firebase.auth
        val uid = auth.currentUser?.uid ?: run {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // 2. Database reference at /users/{uid}/incomes
        database = FirebaseDatabase
            .getInstance()
            .getReference("users")
            .child(uid)
            .child("incomes")

        // 3. Total & empty state views via ViewBinding
        val tvTotalIncome = binding.totalIncomeAmount
        val emptyView     = binding.emptyIncomeView

        // 4. Listen for changes, sum all incomes
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var total = 0.0
                snapshot.children.forEach { child ->
                    child.getValue(Income::class.java)?.let { total += it.amount }
                }
                tvTotalIncome.text    = String.format("R%,.2f", total)
                emptyView.visibility = if (total == 0.0) View.VISIBLE else View.GONE
            }
            override fun onCancelled(error: DatabaseError) { /* no-op */ }
        })

        // 5. RecyclerView + FirebaseUI adapter
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
                holder.tvCategory.text = model.category
                holder.tvAmount.text   = String.format("R%,.2f", model.amount)
                holder.tvDate.text     = model.date
                holder.tvPeriod.text   = model.period
                holder.tvSource.text   = model.source

                // Info button shows details dialog
                holder.btnInfo.setOnClickListener {
                    AlertDialog.Builder(this@IncomeActivity)
                        .setTitle("Income Details")
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
        binding.incomeRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@IncomeActivity)
            adapter = this@IncomeActivity.adapter
        }

        // 6. Hamburger menu replaced with popup for navigation
        binding.menuButton.setOnClickListener { anchor ->
            PopupMenu(this, anchor).apply {
                // inflate your actual menu_income.xml
                menuInflater.inflate(R.menu.menu_income, menu)

                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.nav_expense -> {
                            // navigate to the Expense screen
                            startActivity(Intent(this@IncomeActivity, ExpenseActivity::class.java))
                            true
                        }
                        R.id.nav_pie_chart -> {
                            // Launch the PieChartActivity when user clicks “View Pie Chart”
                            startActivity(Intent(this@IncomeActivity, PieChartActivity::class.java))
                            true
                        }

                        R.id.nav_board_game -> {
                            // navigate to the Board fragment via MainActivity
                            val intent = Intent(this@IncomeActivity, MainActivity::class.java)
                                .putExtra("startFragment", "board")
                            startActivity(intent)
                            true
                        }
                        R.id.nav_login -> {
                            // navigate to the Login fragment via MainActivity
                            val intent = Intent(this@IncomeActivity, MainActivity::class.java)
                                .putExtra("startFragment", "login")
                            startActivity(intent)
                            true
                        }
                        else -> false
                    }
                }

                show()
            }
        }

        // 7. FAB opens add-income dialog
        binding.addIncomeFab.setOnClickListener { showIncomePopup() }
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
        val view = LayoutInflater.from(this)
            .inflate(R.layout.dialog_add_income, null)
        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        val etCat   = view.findViewById<EditText>(R.id.etCategory)
        val etAmt   = view.findViewById<EditText>(R.id.etAmount)
        val spinner = view.findViewById<Spinner>(R.id.spinnerPeriod)
        val etSrc   = view.findViewById<EditText>(R.id.etSource)
        val btnSub  = view.findViewById<Button>(R.id.btnSubmit)

        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        )

        btnSub.setOnClickListener {
            val cat    = etCat.text.toString().trim()
            val amt    = etAmt.text.toString().toDoubleOrNull()
            val period = spinner.selectedItem as String
            val src    = etSrc.text.toString().trim()

            when {
                cat.isEmpty() -> etCat.error = "Required"
                amt == null   -> etAmt.error = "Enter number"
                src.isEmpty() -> etSrc.error = "Required"
                else -> {
                    val entry = Income(
                        category = cat,
                        amount   = amt,
                        date     = LocalDate.now().toString(),
                        period   = period,
                        source   = src
                    )
                    database.push().setValue(entry)
                    Toast.makeText(this, "Income added", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(0))
        dialog.show()
    }

    // ViewHolder binds item_income_card views
    class IncomeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory = view.findViewById<TextView>(R.id.incomeCategory)
        val tvAmount   = view.findViewById<TextView>(R.id.incomeAmount)
        val tvDate     = view.findViewById<TextView>(R.id.incomeDate)
        val tvPeriod   = view.findViewById<TextView>(R.id.incomePeriod)
        val tvSource   = view.findViewById<TextView>(R.id.incomeSource)
        val btnInfo    = view.findViewById<ImageButton>(R.id.incomeInfoButton)
    }
}
