// MyApp.kt
package com.sample.budgetingapplicationfinal

import android.app.Application
import com.google.firebase.FirebaseApp

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Force Firebase to load your google-services.json
        FirebaseApp.initializeApp(this)
    }
}
