package com.sample.budgetingapplicationfinal

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.sample.budgetingapplicationfinal.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val REQUEST_POST_NOTIFICATIONS = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // See if we got a specific screen request from IncomeActivity
        val startScreen = intent.getStringExtra("startFragment")
        when (startScreen) {
            "income" -> {
                // You tapped "Income" in the menu, so let's go there
                val intent = Intent(this, IncomeActivity::class.java)
                startActivity(intent)
                finish()  // close this activity so back won't return here
                return     // no more setup needed
            }
            "board" -> {
                // You chose "Board Game", so load that fragment
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, BoardFragment())
                    .commit()
            }
            "login" -> {
                // Back to login if that's what you asked for
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, LoginFragment())
                    .commit()
            }
            else -> {
                // No flag or unknown value: default to login screen
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, LoginFragment())
                    .commit()
            }
        }

        // On Android 13+ we need to ask for notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Friendly prompt asking the user to allow notifications
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_POST_NOTIFICATIONS
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_POST_NOTIFICATIONS) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                Log.d("MainActivity", "Notifications allowed by user 👍")
            } else {
                Log.d("MainActivity", "Notifications permission denied 👎")
            }
        }
    }
}
