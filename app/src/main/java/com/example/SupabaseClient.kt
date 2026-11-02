package com.example

import android.content.Context
import android.util.Log
import com.russhwolf.settings.SharedPreferencesSettings
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.CodeVerifierCache
import io.github.jan.supabase.gotrue.MemoryCodeVerifierCache
import io.github.jan.supabase.gotrue.MemorySessionManager
import io.github.jan.supabase.gotrue.SessionManager
import io.github.jan.supabase.gotrue.SettingsCodeVerifierCache
import io.github.jan.supabase.gotrue.SettingsSessionManager
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

/**
 * Initializes and provides the Supabase client using the SUPABASE_URL
 * and SUPABASE_ANON_KEY from the project's BuildConfig.
 */
val supabase: SupabaseClient by lazy {
    val validUrl = com.example.core.data.SupabaseConfig.supabaseUrl
    val validKey = com.example.core.data.SupabaseConfig.supabaseAnonKey

    Log.i("SupabaseClient", "Initializing SupabaseClient with URL: $validUrl")

    createSupabaseClient(
        supabaseUrl = validUrl,
        supabaseKey = validKey
    ) {
        install(Auth) {
            scheme = "moviedb"
            host = "auth-callback"
            val context = MovieDBApp.appContext
            if (context != null) {
                try {
                    val prefs = context.getSharedPreferences("supabase_session", Context.MODE_PRIVATE)
                    val settings = SharedPreferencesSettings(prefs)
                    sessionManager = SettingsSessionManager(settings)
                    codeVerifierCache = SettingsCodeVerifierCache(settings)
                } catch (_: Throwable) {
                    sessionManager = MemorySessionManager()
                    codeVerifierCache = MemoryCodeVerifierCache()
                }
            } else {
                sessionManager = MemorySessionManager()
                codeVerifierCache = MemoryCodeVerifierCache()
            }
        }
        install(Postgrest)
        install(Realtime)
        install(Functions)
        install(Storage)
    }
}

/**
 * Alias reference to the singleton Supabase client.
 */
val supabaseClient: SupabaseClient
    get() = supabase
