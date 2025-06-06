package com.sample.budgetingapplicationfinal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity

class PieChartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pie_chart)

        // 1) Host the fragment inside the FrameLayout
        supportFragmentManager.beginTransaction()
            .replace(R.id.pieChartFragmentContainer, PieChartFragment())
            .commit()

        // 2) Wire “REFRESH CHART” button to reload + animate
        findViewById<Button>(R.id.refreshButton).setOnClickListener {
            val frag = supportFragmentManager
                .findFragmentById(R.id.pieChartFragmentContainer)
            if (frag is PieChartFragment) {
                frag.loadChartData()
            }
        }

        // 3) Wire hamburger (menuButton) to show menu_pie_chart.xml
        findViewById<ImageButton>(R.id.menuButton).setOnClickListener { anchor ->
            PopupMenu(this, anchor).apply {
                menuInflater.inflate(R.menu.menu_pie_chart, menu)

                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.nav_expense -> {
                            startActivity(Intent(this@PieChartActivity, ExpenseActivity::class.java))
                            true
                        }
                        R.id.nav_income -> {
                            startActivity(Intent(this@PieChartActivity, IncomeActivity::class.java))
                            true
                        }
                        R.id.nav_board_game -> {
                            startActivity(
                                Intent(this@PieChartActivity, MainActivity::class.java)
                                    .putExtra("startFragment", "board")
                            )
                            true
                        }
                        R.id.nav_login -> {
                            startActivity(
                                Intent(this@PieChartActivity, MainActivity::class.java)
                                    .putExtra("startFragment", "login")
                            )
                            true
                        }
                        R.id.nav_pie_chart -> {
                            // Already on PieChartActivity, no-op
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
