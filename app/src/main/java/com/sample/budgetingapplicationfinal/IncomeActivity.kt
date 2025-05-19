package com.sample.budgetingapplicationfinal

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.sample.budgetingapplicationfinal.databinding.ActivityIncomeBinding
import java.time.LocalDate
import android.content.Intent


class IncomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIncomeBinding
    private val incomeList = mutableListOf<Income>()

    private var totalIncome: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateTotalIncomeDisplay()

        binding.optionsButton.setOnClickListener {
            showIncomePopup()
        }

        binding.backArrow.setOnClickListener {
            val intent = Intent(this, ExpenseActivity::class.java)
            startActivity(intent)
            finish()
        }

    }


    private fun showIncomePopup() {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.income_input_popup, null)

        val dialog = AlertDialog.Builder(this)
            .setView(popupView)
            .create()

        val categoryInput = popupView.findViewById<EditText>(R.id.incomeCategoryInput)
        val amountInput = popupView.findViewById<EditText>(R.id.incomeAmountInput)
        val submitButton = popupView.findViewById<Button>(R.id.submitIncomeButton)

        submitButton.setOnClickListener {
            val categoryText = categoryInput.text.toString().trim()
            val amountText = amountInput.text.toString().trim()

            if (categoryText.isNotEmpty() && amountText.isNotEmpty()) {
                val parsedAmount = amountText.toDoubleOrNull()

                if (parsedAmount != null) {
                    // 🔸 Store in variable
                    val newIncome = Income(source = categoryText, amount = parsedAmount)

                    // 🔸 Add to in-memory list
                    incomeList.add(newIncome)

                    addIncomeCard(newIncome)

                    // 🔸 Update total income
                    totalIncome += newIncome.amount
                    updateTotalIncomeDisplay()

                    Toast.makeText(this, "Income Added: ${newIncome.source} - R${newIncome.amount}", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    amountInput.error = "Enter a valid number"
                }
            } else {
                if (categoryText.isEmpty()) categoryInput.error = "Required"
                if (amountText.isEmpty()) amountInput.error = "Required"
            }
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun addIncomeCard(income: Income) {
        val cardView = layoutInflater.inflate(R.layout.item_income_card, binding.incomeHistoryContainer, false)


        val source = cardView.findViewById<TextView>(R.id.incomeSource)
        val date = cardView.findViewById<TextView>(R.id.incomeDate)
        val amount = cardView.findViewById<TextView>(R.id.incomeAmount)




        source.text = income.source
        date.text = LocalDate.now().month.name.lowercase().replaceFirstChar { it.uppercase() } + " ${LocalDate.now().year}"
        amount.text = "R${"%,.2f".format(income.amount)}"

        binding.incomeHistoryContainer.addView(cardView)
    }


    private fun updateTotalIncomeDisplay() {
        binding.totalIncomeAmount.text = String.format("R%,.2f", totalIncome)
    }
}