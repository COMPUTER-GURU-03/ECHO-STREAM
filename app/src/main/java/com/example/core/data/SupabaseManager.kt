package com.example.core.data

import io.github.jan.supabase.SupabaseClient

/**
 * SupabaseManager delegates directly to the centralized SupabaseConfig.
 * Ensures complete compatibility across existing repositories and views.
 */
object SupabaseManager {
    val supabaseUrl: String get() = SupabaseConfig.supabaseUrl
    val supabaseAnonKey: String get() = SupabaseConfig.supabaseAnonKey
    val isConfigured: Boolean get() = SupabaseConfig.isConfigured

    val client: SupabaseClient? get() = SupabaseConfig.client
    val auth get() = SupabaseConfig.auth
    val postgrest get() = SupabaseConfig.postgrest
    val realtime get() = SupabaseConfig.realtime
    val functions get() = SupabaseConfig.functions
    val storage get() = SupabaseConfig.storage

    fun initialize(): SupabaseClient? = SupabaseConfig.initialize()
}
