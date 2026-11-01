package com.example.features.playback.data

import com.example.core.data.SupabaseManager
import com.example.features.playback.domain.WatchProgress
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WatchProgressRepository {
    private val client = SupabaseManager.client

    suspend fun getProgress(tmdbId: Int, mediaType: String): WatchProgress? = withContext(Dispatchers.IO) {
        val supabase = client ?: return@withContext null
        val userId = supabase.auth.currentUserOrNull()?.id ?: return@withContext null
        try {
            supabase.postgrest["watch_progress"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                        eq("tmdb_id", tmdbId)
                        eq("media_type", mediaType)
                    }
                }
                .decodeSingleOrNull<WatchProgress>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun saveProgress(progress: WatchProgress): Boolean = withContext(Dispatchers.IO) {
        val supabase = client ?: return@withContext false
        try {
            // Use upsert to update if exists, otherwise insert
            supabase.postgrest["watch_progress"].upsert(progress, onConflict = "user_id, tmdb_id, media_type")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
