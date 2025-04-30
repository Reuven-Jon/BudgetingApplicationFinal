package com.sample.budgetingapplicationfinal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sample.budgetingapplicationfinal.databinding.ActivityMainBinding
import android.util.Log

/**
 * MainActivity serves as the single-Activity host for all fragments.
 *
 * 📝 Architecture & Fragment-first navigation:
 *   – We launch LoginFragment first, then swap in other flows on success.
 *   – See “Guide to App Architecture” on Android Developers:
 *     https://developer.android.com/jetpack/guide
 *
 * 📝 Handling runtime permissions:
 *   – We request POST_NOTIFICATIONS on Android 13+ up front.
 *   – See “Request permissions at runtime”:
 *     https://developer.android.com/training/permissions/requesting
 *
 * 📝 Kotlin style & code conventions:
 *   – Follow Kotlin Coding Conventions (naming, formatting):
 *     https://kotlinlang.org/docs/coding-conventions.html
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate view binding (avoids findViewById calls)
        // Reference: View Binding docs
        // https://developer.android.com/topic/libraries/view-binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Request POST_NOTIFICATIONS on Android 13+ before any notifications are shown
        // Best Practice: Always check SDK version before requesting new permissions
        // https://developer.android.com/about/versions/13/changes/behavior#notifications-permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_POST_NOTIFICATIONS
                )
            }
        }

        // Start with the login flow
        // Single-Activity, Fragment-based navigation:
        // https://developer.android.com/guide/fragments/fragmentmanager
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, LoginFragment())
            .commit()
    }

    companion object {
        private const val REQUEST_POST_NOTIFICATIONS = 1001
    }

    /**
     * Handle the result of our notification-permission request.
     * We don’t immediately fire notifications here; instead, we
     * let the logged-in fragment or ViewModel decide when to call
     * NotificationHelper.showTip().
     *
     * See “Handling the permissions request response”:
     * https://developer.android.com/training/permissions/requesting#handle-result
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_POST_NOTIFICATIONS &&
            grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        ) {
            // now safe to send notifications
            Log.d("MainActivity", "POST_NOTIFICATIONS permission granted")
        }
    }
}

