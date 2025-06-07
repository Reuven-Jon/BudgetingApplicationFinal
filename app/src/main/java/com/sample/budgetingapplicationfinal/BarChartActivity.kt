package com.sample.budgetingapplicationfinal

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity

class BarChartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use the bar‐chart layout
        setContentView(R.layout.activity_bar_chart)

        // 1) Host our BarChartFragment in the container
        supportFragmentManager.beginTransaction()
            .replace(R.id.barChartFragmentContainer, BarChartFragment())
            .commit()

        // 2) Hamburger menu (top‐left or right, per your XML)
        findViewById<ImageButton>(R.id.menuButton).setOnClickListener { anchor ->
            PopupMenu(this, anchor).apply {
                menuInflater.inflate(R.menu.menu_pie_chart, menu)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.nav_expense -> {
                            startActivity(Intent(this@BarChartActivity, ExpenseActivity::class.java))
                            true
                        }
                        R.id.nav_income -> {
                            startActivity(Intent(this@BarChartActivity, IncomeActivity::class.java))
                            true
                        }
                        R.id.nav_board_game -> {
                            startActivity(
                                Intent(this@BarChartActivity, MainActivity::class.java)
                                    .putExtra("startFragment", "board")
                            )
                            true
                        }
                        R.id.nav_login -> {
                            startActivity(
                                Intent(this@BarChartActivity, MainActivity::class.java)
                                    .putExtra("startFragment", "login")
                            )
                            true
                        }
                        R.id.nav_bar_chart -> {
                            // If you also want to allow switching back, start PieChartActivity
                            startActivity(Intent(this@BarChartActivity, BarChartActivity::class.java))
                            true
                        }
                        else -> false
                    }
                }
                show()
            }
        }
    }
}
