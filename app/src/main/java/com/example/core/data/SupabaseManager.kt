package com.example.core.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.storage.Storage
import com.example.BuildConfig

object SupabaseManager {
    val client: SupabaseClient? by lazy {
        val url = BuildConfig.SUPABASE_URL
        val key = BuildConfig.SUPABASE_ANON_KEY
        
        if (url.isBlank() || url.contains("YOUR_SUPABASE_URL") || key.isBlank() || key.contains("YOUR_SUPABASE_ANON_KEY")) {
            null
        } else {
            runCatching {
                createSupabaseClient(
                    supabaseUrl = url,
                    supabaseKey = key
                ) {
                    install(Auth)
                    install(Postgrest)
                    install(Realtime)
                    install(Functions)
                    install(Storage)
                }
            }.getOrNull()
        }
    }
}
