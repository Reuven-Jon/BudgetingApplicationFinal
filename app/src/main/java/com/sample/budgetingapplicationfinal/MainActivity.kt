package com.sample.budgetingapplicationfinal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sample.budgetingapplicationfinal.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If you need to override the manifest theme here, use:
        // setTheme(R.style.Theme_BudgetingApplicationFinal)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Ask for POST_NOTIFICATIONS on Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_POST_NOTIFICATIONS
                )
            }
        }

        // 2) As soon as the channel exists & (optionally) permission is granted, fire a tip
        val notifier = NotificationHelper(this)
        notifier.showTip("Track one expense today!")

        // Start with your input fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, GoalInputFragment())
            .commit()
    }

    companion object {
        private const val REQUEST_POST_NOTIFICATIONS = 1001
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_POST_NOTIFICATIONS
            && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            // Now you can call notifier.showTip(...) again if you want
        }
    }
}
