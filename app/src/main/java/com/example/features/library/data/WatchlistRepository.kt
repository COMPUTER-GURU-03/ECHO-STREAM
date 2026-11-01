package com.example.features.library.data

import android.util.Log
import com.example.core.data.SupabaseManager
import com.example.features.library.domain.WatchlistItem
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WatchlistRepository {
    private val client get() = SupabaseManager.client

    // In-memory cache to guarantee instant optimistic UI and offline resilience
    private val localWatchlist = mutableListOf<WatchlistItem>()

    suspend fun getWatchlist(): List<WatchlistItem> = withContext(Dispatchers.IO) {
        val supabase = client
        val userId = supabase?.auth?.currentUserOrNull()?.id

        if (supabase != null && userId != null) {
            try {
                val remoteItems = supabase.postgrest["watchlists"]
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<WatchlistItem>()

                synchronized(localWatchlist) {
                    localWatchlist.clear()
                    localWatchlist.addAll(remoteItems)
                }
                return@withContext remoteItems
            } catch (e: Exception) {
                Log.w("WatchlistRepository", "Could not fetch remote watchlist, falling back to local cache", e)
            }
        }

        synchronized(localWatchlist) {
            localWatchlist.toList()
        }
    }

    suspend fun addToWatchlist(item: WatchlistItem): Boolean = withContext(Dispatchers.IO) {
        synchronized(localWatchlist) {
            if (localWatchlist.none { it.tmdbId == item.tmdbId && it.mediaType == item.mediaType }) {
                localWatchlist.add(item)
            }
        }

        val supabase = client ?: return@withContext true
        try {
            supabase.postgrest["watchlists"].upsert(item)
            true
        } catch (e: Exception) {
            try {
                supabase.postgrest["watchlists"].insert(item)
                true
            } catch (e2: Exception) {
                Log.e("WatchlistRepository", "Failed to insert into Supabase watchlists", e2)
                true // still preserved in localWatchlist
            }
        }
    }

    suspend fun removeFromWatchlist(tmdbId: Int, mediaType: String): Boolean = withContext(Dispatchers.IO) {
        synchronized(localWatchlist) {
            localWatchlist.removeAll { it.tmdbId == tmdbId && it.mediaType == mediaType }
        }

        val supabase = client ?: return@withContext true
        val userId = supabase.auth.currentUserOrNull()?.id ?: return@withContext true
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
            Log.e("WatchlistRepository", "Failed to delete from Supabase watchlists", e)
            true
        }
    }

    suspend fun isInWatchlist(tmdbId: Int, mediaType: String): Boolean = withContext(Dispatchers.IO) {
        synchronized(localWatchlist) {
            if (localWatchlist.any { it.tmdbId == tmdbId && it.mediaType == mediaType }) {
                return@withContext true
            }
        }

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

            val exists = result != null
            if (result != null) {
                synchronized(localWatchlist) {
                    if (localWatchlist.none { it.tmdbId == result.tmdbId && it.mediaType == result.mediaType }) {
                        localWatchlist.add(result)
                    }
                }
            }
            exists
        } catch (e: Exception) {
            false
        }
    }
}
