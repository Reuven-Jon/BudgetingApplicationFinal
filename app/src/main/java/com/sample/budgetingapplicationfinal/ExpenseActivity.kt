package com.sample.budgetingapplicationfinal

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sample.budgetingapplicationfinal.databinding.ActivityExpenseBinding
import java.time.LocalDate

class ExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpenseBinding
    private val expenseList = mutableListOf<Expense>()
    private var totalExpenses: Double = 0.0
    private lateinit var notificationHelper: NotificationHelper

    companion object {
        private const val NOTIF_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // init notification helper & channels
        notificationHelper = NotificationHelper(this)

        // ask for POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIF_PERMISSION_REQUEST_CODE
                )
            }
        }

        updateTotalExpenseDisplay()
        binding.optionsButtonExpense.setOnClickListener { showExpensePopup() }
        binding.backArrow.setOnClickListener {
            startActivity(Intent(this, IncomeActivity::class.java))
            finish()
        }
    }

    private fun showExpensePopup() {
        val popupView = LayoutInflater.from(this)
            .inflate(R.layout.expense_input_popup, null)
        val dialog = AlertDialog.Builder(this)
            .setView(popupView)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        val categoryInput = popupView.findViewById<EditText>(R.id.expenseCategoryInput)
        val amountInput   = popupView.findViewById<EditText>(R.id.expenseAmountInput)
        val submitButton  = popupView.findViewById<Button>(R.id.submitExpenseButton)

        submitButton.setOnClickListener {
            val categoryText = categoryInput.text.toString().trim()
            val amountText   = amountInput.text.toString().trim()
            val parsedAmount = amountText.toDoubleOrNull()

            // validate inputs
            if (categoryText.isBlank()) {
                categoryInput.error = "Required"
                return@setOnClickListener
            }
            if (parsedAmount == null) {
                amountInput.error = "Enter valid number"
                return@setOnClickListener
            }

            // record expense
            val newExpense = Expense(source = categoryText, amount = parsedAmount)
            expenseList.add(newExpense)
            addExpenseCard(newExpense)

            totalExpenses += parsedAmount
            updateTotalExpenseDisplay()

            // send notification if permission granted
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    notificationHelper.showExpenseNotification(
                        newExpense.source,
                        newExpense.amount
                    )
                } catch (e: Exception) {
                    Log.e("ExpenseActivity", "Notification error", e)
                    Toast.makeText(this, "Couldn’t send notification", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show()
            }

            Toast.makeText(
                this,
                "Expense Added: ${newExpense.source} – R${"%,.2f".format(newExpense.amount)}",
                Toast.LENGTH_SHORT
            ).show()
            dialog.dismiss()
        }
    }

    // inform user if they deny notification permission
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIF_PERMISSION_REQUEST_CODE &&
            grantResults.firstOrNull() != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addExpenseCard(expense: Expense) {
        val cardView = layoutInflater.inflate(
            R.layout.item_expense_card,
            binding.expenseHistoryContainer,
            false
        )
        val sourceTv = cardView.findViewById<TextView>(R.id.expenseSource)
        val dateTv   = cardView.findViewById<TextView>(R.id.expenseDate)
        val amountTv = cardView.findViewById<TextView>(R.id.expenseAmount)

        sourceTv.text = expense.source
        dateTv.text = LocalDate.now().month.name.lowercase()
            .replaceFirstChar { it.uppercase() } + " ${LocalDate.now().year}"
        amountTv.text = "R${"%,.2f".format(expense.amount)}"

        binding.expenseHistoryContainer.addView(cardView)
    }

    private fun updateTotalExpenseDisplay() {
        binding.totalExpenseAmount.text = String.format("R%,.2f", totalExpenses)
    }
}
