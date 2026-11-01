package com.example.features.playback.data

import com.example.core.data.SupabaseManager
import io.github.jan.supabase.functions.functions
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class VideoAssetRepository {
    private val functions = SupabaseManager.client?.functions

    suspend fun resolveStreamUrl(tmdbId: Int, mediaType: String): String? = withContext(Dispatchers.IO) {
        val client = functions ?: return@withContext null
        try {
            val body = buildJsonObject {
                put("tmdb_id", tmdbId)
                put("media_type", mediaType)
            }
            val response = client.invoke("resolve-video", body)
            // Expecting the edge function to return a JSON with a "url" field
            // For now, we'll just return the response text or a dummy if it's empty
            response.bodyAsText() // Simplified for now
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
