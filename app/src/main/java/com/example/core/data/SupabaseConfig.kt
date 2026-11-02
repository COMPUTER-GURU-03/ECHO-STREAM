package com.example.core.data

import android.util.Log
import com.example.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.storage.storage

/**
 * Supabase Client Configuration File.
 * Centralizes client initialization, credentials validation,
 * and plugin configuration for Supabase Auth, Postgrest, Realtime, Functions, and Storage.
 */
object SupabaseConfig {
    private const val TAG = "SupabaseConfig"

    const val DEFAULT_SUPABASE_URL = "https://yihabvxwytdfpgtiawex.supabase.co"
    const val DEFAULT_SUPABASE_ANON_KEY =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlpaGFidnh3eXRkZnBndGlhd2V4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTM1MzE4ODksImV4cCI6MjEwOTEwNzg4OX0.thdjkNEBfMJBnothwKioBQFaMshCQA8YacliZfv2pqs"

    val supabaseUrl: String
        get() {
            val buildUrl = try { BuildConfig.SUPABASE_URL.trim() } catch (_: Exception) { "" }
            return if (buildUrl.isNotBlank() && !buildUrl.contains("YOUR_SUPABASE_URL")) {
                buildUrl
            } else {
                DEFAULT_SUPABASE_URL
            }
        }

    val supabaseAnonKey: String
        get() {
            val buildKey = try { BuildConfig.SUPABASE_ANON_KEY.trim() } catch (_: Exception) { "" }
            return if (buildKey.isNotBlank() && !buildKey.contains("YOUR_SUPABASE_ANON_KEY")) {
                buildKey
            } else {
                DEFAULT_SUPABASE_ANON_KEY
            }
        }

    val isConfigured: Boolean
        get() = supabaseUrl.isNotBlank() && supabaseAnonKey.isNotBlank()

    val client: SupabaseClient?
        get() = try {
            com.example.supabase
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to resolve supabase client", t)
            null
        }

    val auth get() = client?.auth
    val postgrest get() = client?.postgrest
    val realtime get() = client?.realtime
    val functions get() = client?.functions
    val storage get() = client?.storage

    fun initialize(): SupabaseClient? {
        val c = client
        if (c != null) {
            Log.i(TAG, "Supabase client active with Auth for $supabaseUrl")
        }
        return c
    }
}
