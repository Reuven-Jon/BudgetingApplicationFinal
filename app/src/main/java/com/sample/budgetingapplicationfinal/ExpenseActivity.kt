package com.sample.budgetingapplicationfinal

import android.Manifest                                        // for runtime permissions
import android.app.AlertDialog                                 // to build pop-up dialogs
import android.content.Intent                                  // to launch other Activities
import android.content.pm.PackageManager                       // to check permission status
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build                                        // to branch on Android version
import android.os.Bundle
import android.util.Log                                        // for logging errors
import android.view.LayoutInflater                             // to inflate custom pop-up layout
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth                    // Firebase authentication
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference          // to reference your Firebase DB
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.sample.budgetingapplicationfinal.databinding.ActivityExpenseBinding
import java.time.LocalDate                                     // to stamp expenses with dates

class ExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExpenseBinding   // view-binding for activity_expense.xml
    private lateinit var auth: FirebaseAuth                // FirebaseAuth instance
    private lateinit var dbRef: DatabaseReference          // Reference to /users/{uid}/expenses
    private lateinit var notificationHelper: NotificationHelper
    private var totalExpenses: Double = 0.0                // running total in-memory
    private val expenseList = mutableListOf<Expense>()  // holds expenses for the UI

    companion object {
        private const val NOTIF_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout and set it as the content view
        binding = ActivityExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth
        auth = Firebase.auth

        // If user isn’t signed in, bounce them to login flow
        if (auth.currentUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Build a reference to /users/{uid}/expenses in Realtime DB
        val uid = auth.currentUser!!.uid
        dbRef = FirebaseDatabase
            .getInstance()
            .getReference("users")
            .child(uid)
            .child("expenses")

        // Initialize notifications helper
        notificationHelper = NotificationHelper(this)

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                NOTIF_PERMISSION_REQUEST_CODE
            )
        }

        // Show current total
        updateTotalExpenseDisplay()

        // Show pop-up when user taps the options button
        binding.optionsButtonExpense.setOnClickListener {
            showExpensePopup()
        }

        // Back arrow returns to IncomeActivity
        binding.backArrow.setOnClickListener {
            startActivity(Intent(this, IncomeActivity::class.java))
            finish()
        }
    }

    private fun showExpensePopup() {
        // Inflate the custom layout for expense input
        val popupView = LayoutInflater.from(this)
            .inflate(R.layout.expense_input_popup, null)

        // Build and show a transparent-background AlertDialog
        val dialog = AlertDialog.Builder(this)
            .setView(popupView)
            .create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        // Grab references to the input fields and button
        val categoryInput = popupView.findViewById<EditText>(R.id.expenseCategoryInput)
        val amountInput   = popupView.findViewById<EditText>(R.id.expenseAmountInput)
        val submitButton  = popupView.findViewById<Button>(R.id.submitExpenseButton)

        submitButton.setOnClickListener {
            // Read and trim user inputs
            val categoryText = categoryInput.text.toString().trim()
            val amountText   = amountInput.text.toString().trim()
            val parsedAmount = amountText.toDoubleOrNull()

            // Validate category isn’t blank
            if (categoryText.isBlank()) {
                categoryInput.error = "Required"
                return@setOnClickListener
            }
            // Validate amount is a number
            if (parsedAmount == null) {
                amountInput.error = "Enter valid number"
                return@setOnClickListener
            }

            // Create the Expense object
            val newExpense = Expense(source = categoryText, amount = parsedAmount)

            // Add to in-memory list and UI
            expenseList.add(newExpense)
            addExpenseCard(newExpense)

            // Persist to Firebase under /users/{uid}/expenses
            val expenseEntry = ExpenseEntry(
                category = categoryText,
                amount = parsedAmount,
                date = LocalDate.now().toString()
            )
            dbRef.push().setValue(expenseEntry)                // write to new path
            FirebaseDatabaseManager.saveExpense(auth.currentUser!!.uid, expenseEntry) // ensure saved

            // Update totals and display
            totalExpenses += parsedAmount
            updateTotalExpenseDisplay()

            // Send notification if permitted
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

            // Show success toast
            Toast.makeText(
                this,
                "Expense added: ${newExpense.source} – R%,.2f".format(newExpense.amount),
                Toast.LENGTH_SHORT
            ).show()

            // Dismiss the dialog
            dialog.dismiss()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Inform user if they denied notification permission
        if (requestCode == NOTIF_PERMISSION_REQUEST_CODE &&
            grantResults.firstOrNull() != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addExpenseCard(expense: Expense) {
        // Inflate an expense card and bind its fields
        val cardView = layoutInflater.inflate(
            R.layout.item_expense_card,
            binding.expenseHistoryContainer,
            false
        )
        val sourceTv = cardView.findViewById<TextView>(R.id.expenseSource)
        val dateTv   = cardView.findViewById<TextView>(R.id.expenseDate)
        val amountTv = cardView.findViewById<TextView>(R.id.expenseAmount)

        // Display source, formatted date, and amount
        sourceTv.text = expense.source
        dateTv.text   = "${LocalDate.now().month.name.lowercase().replaceFirstChar { it.uppercase() }} ${LocalDate.now().year}"
        amountTv.text = "R%,.2f".format(expense.amount)

        // Add the card to the scrollable container
        binding.expenseHistoryContainer.addView(cardView)
    }

    private fun updateTotalExpenseDisplay() {
        // Update the total expenses TextView
        binding.totalExpenseAmount.text = "R%,.2f".format(totalExpenses)
    }
}
