package com.example.dsm_desafio_03

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class ReadingTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            val database = FirebaseDatabase.getInstance(DATABASE_URL)
            database.setPersistenceEnabled(true)
        } catch (_: Exception) {
            // Persistence can only be enabled once per process
        }
    }

    companion object {
        const val DATABASE_URL = "https://dsm-desafio-03-default-rtdb.firebaseio.com"
    }
}
