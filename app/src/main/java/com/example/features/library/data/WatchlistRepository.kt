package com.example.features.library.data

import com.example.core.data.SupabaseManager
import com.example.features.library.domain.WatchlistItem
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WatchlistRepository {
    private val client = SupabaseManager.client

    suspend fun getWatchlist(): List<WatchlistItem> = withContext(Dispatchers.IO) {
        val supabase = client ?: return@withContext emptyList()
        val userId = supabase.auth.currentUserOrNull()?.id ?: return@withContext emptyList()
        
        try {
            supabase.postgrest["watchlists"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<WatchlistItem>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun addToWatchlist(item: WatchlistItem): Boolean = withContext(Dispatchers.IO) {
        val supabase = client ?: return@withContext false
        try {
            supabase.postgrest["watchlists"].insert(item)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun removeFromWatchlist(tmdbId: Int, mediaType: String): Boolean = withContext(Dispatchers.IO) {
        val supabase = client ?: return@withContext false
        val userId = supabase.auth.currentUserOrNull()?.id ?: return@withContext false
        try {
            supabase.postgrest["watchlists"].delete {
                filter {
                    eq("user_id", userId)
                    eq("tmdb_id", tmdbId)
                    eq("media_type", mediaType)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun isInWatchlist(tmdbId: Int, mediaType: String): Boolean = withContext(Dispatchers.IO) {
        val supabase = client ?: return@withContext false
        val userId = supabase.auth.currentUserOrNull()?.id ?: return@withContext false
        try {
            val result = supabase.postgrest["watchlists"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                        eq("tmdb_id", tmdbId)
                        eq("media_type", mediaType)
                    }
                }
                .decodeSingleOrNull<WatchlistItem>()
            result != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
