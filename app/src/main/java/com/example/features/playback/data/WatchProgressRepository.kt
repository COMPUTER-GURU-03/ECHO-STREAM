package com.example.features.playback.data

import com.example.core.data.SupabaseManager
import com.example.features.playback.domain.WatchProgress
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WatchProgressRepository {
    private val client get() = SupabaseManager.client

    companion object {
        // Shared in-memory cache to guarantee instant responsiveness across screens & fallback
        private val localProgressList = mutableListOf<WatchProgress>()
    }

    suspend fun getProgress(tmdbId: Int, mediaType: String): WatchProgress? = withContext(Dispatchers.IO) {
        // Check local cache first
        synchronized(localProgressList) {
            val local = localProgressList.find { it.tmdbId == tmdbId && it.mediaType == mediaType }
            if (local != null) return@withContext local
        }

        val supabase = client ?: return@withContext null
        val userId = try { supabase.auth.currentUserOrNull()?.id } catch (_: Exception) { null }
            ?: return@withContext null

        try {
            val remote = supabase.postgrest["watch_progress"]
                .select(columns = Columns.ALL) {
                    filter {
                        eq("user_id", userId)
                        eq("tmdb_id", tmdbId)
                        eq("media_type", mediaType)
                    }
                }
                .decodeSingleOrNull<WatchProgress>()

            if (remote != null) {
                synchronized(localProgressList) {
                    val index = localProgressList.indexOfFirst { it.tmdbId == remote.tmdbId && it.mediaType == remote.mediaType }
                    if (index >= 0) {
                        localProgressList[index] = remote
                    } else {
                        localProgressList.add(0, remote)
                    }
                }
            }
            remote
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveProgress(progress: WatchProgress): Boolean = withContext(Dispatchers.IO) {
        // Save to local cache first
        synchronized(localProgressList) {
            val index = localProgressList.indexOfFirst { it.tmdbId == progress.tmdbId && it.mediaType == progress.mediaType }
            if (index >= 0) {
                localProgressList[index] = progress
            } else {
                localProgressList.add(0, progress)
            }
        }

        val supabase = client ?: return@withContext true
        try {
            supabase.postgrest["watch_progress"].upsert(
                progress,
                onConflict = "user_id, tmdb_id, media_type"
            )
            true
        } catch (e: Exception) {
            // Local save still succeeded
            true
        }
    }

    suspend fun getAllProgress(): List<WatchProgress> = withContext(Dispatchers.IO) {
        val supabase = client
        val userId = try { supabase?.auth?.currentUserOrNull()?.id } catch (_: Exception) { null }

        if (supabase != null && userId != null) {
            try {
                val remoteList = supabase.postgrest["watch_progress"]
                    .select(columns = Columns.ALL) {
                        filter {
                            eq("user_id", userId)
                        }
                    }
                    .decodeList<WatchProgress>()

                synchronized(localProgressList) {
                    remoteList.forEach { remote ->
                        val index = localProgressList.indexOfFirst { it.tmdbId == remote.tmdbId && it.mediaType == remote.mediaType }
                        if (index >= 0) {
                            localProgressList[index] = remote
                        } else {
                            localProgressList.add(remote)
                        }
                    }
                }
            } catch (_: Exception) {
                // Ignore remote fetch error and fallback to local cache
            }
        }

        synchronized(localProgressList) {
            localProgressList
                .filter { it.lastPosition > 5000L && !it.isFinished }
                .toList()
        }
    }

    suspend fun clearProgress(tmdbId: Int, mediaType: String): Boolean = withContext(Dispatchers.IO) {
        synchronized(localProgressList) {
            localProgressList.removeAll { it.tmdbId == tmdbId && it.mediaType == mediaType }
        }

        val supabase = client ?: return@withContext true
        val userId = try { supabase.auth.currentUserOrNull()?.id } catch (_: Exception) { null }
            ?: return@withContext true

        try {
            supabase.postgrest["watch_progress"].delete {
                filter {
                    eq("user_id", userId)
                    eq("tmdb_id", tmdbId)
                    eq("media_type", mediaType)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
