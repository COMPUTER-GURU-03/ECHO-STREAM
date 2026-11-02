package com.example

import android.app.Application
import android.util.Log
import android.content.Context
import com.example.core.data.SupabaseManager

class MovieDBApp : Application() {
    companion object {
        var appContext: Context? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        Log.i("MovieDBApp", "Initializing MovieDB application and auth subsystem")
        try {
            SupabaseManager.initialize()
        } catch (t: Throwable) {
            Log.w("MovieDBApp", "Supabase initialization deferred: ${t.message}")
        }
    }
}
