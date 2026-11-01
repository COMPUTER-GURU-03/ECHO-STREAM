package com.example.core.data

import android.util.Log
import com.example.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

/**
 * Manages the singleton Supabase client initialized using SUPABASE_URL
 * and SUPABASE_ANON_KEY loaded from the .env file via BuildConfig.
 */
object SupabaseManager {
    private const val TAG = "SupabaseManager"

    val supabaseUrl: String
        get() = BuildConfig.SUPABASE_URL.trim()

    val supabaseAnonKey: String
        get() = BuildConfig.SUPABASE_ANON_KEY.trim()

    /**
     * Checks if valid Supabase credentials have been configured in .env.
     */
    val isConfigured: Boolean
        get() = supabaseUrl.isNotBlank() &&
                !supabaseUrl.contains("YOUR_SUPABASE_URL") &&
                supabaseAnonKey.isNotBlank() &&
                !supabaseAnonKey.contains("YOUR_SUPABASE_ANON_KEY")

    /**
     * Lazily creates the SupabaseClient instance with Auth, Postgrest,
     * Realtime, Functions, and Storage installed.
     */
    val client: SupabaseClient? by lazy {
        if (!isConfigured) {
            Log.w(TAG, "Supabase client not initialized: SUPABASE_URL or SUPABASE_ANON_KEY missing in .env")
            null
        } else {
            runCatching {
                Log.i(TAG, "Initializing Supabase client with URL: $supabaseUrl")
                createSupabaseClient(
                    supabaseUrl = supabaseUrl,
                    supabaseKey = supabaseAnonKey
                ) {
                    install(Auth)
                    install(Postgrest)
                    install(Realtime)
                    install(Functions)
                    install(Storage)
                }
            }.onFailure { error ->
                Log.e(TAG, "Failed to initialize Supabase client", error)
            }.getOrNull()
        }
    }

    val auth get() = client?.auth
    val postgrest get() = client?.postgrest
    val realtime get() = client?.realtime
    val functions get() = client?.functions
    val storage get() = client?.storage
}
