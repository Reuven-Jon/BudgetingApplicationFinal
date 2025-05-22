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

        // Decide what to start
        when (intent.getStringExtra("startFragment")) {
            "income" -> {
                // Launch your IncomeActivity directly
                startActivity(Intent(this, IncomeActivity::class.java))
                finish()
                return
            }
            "board" -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, BoardFragment())
                    .commit()
            }
            "login", null -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, LoginFragment())
                    .commit()
            }
        }

        // Request Android 13+ notification permission once
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_POST_NOTIFICATIONS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_POST_NOTIFICATIONS &&
            grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("MainActivity", "POST_NOTIFICATIONS permission granted")
        }
    }
}
