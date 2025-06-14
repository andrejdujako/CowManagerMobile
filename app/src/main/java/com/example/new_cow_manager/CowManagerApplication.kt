package com.example.new_cow_manager

import android.app.Application
import com.google.firebase.FirebaseApp

class CowManagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
