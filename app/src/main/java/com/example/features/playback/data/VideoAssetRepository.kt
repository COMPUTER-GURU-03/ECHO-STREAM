package com.example.features.playback.data

import com.example.core.data.SupabaseManager
import io.github.jan.supabase.functions.functions
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.math.abs

class VideoAssetRepository {
    private val client get() = SupabaseManager.client

    private val sampleStreams = listOf(
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
    )

    suspend fun resolveStreamUrl(tmdbId: Int, mediaType: String): String = withContext(Dispatchers.IO) {
        val supabase = client
        if (supabase != null) {
            try {
                val body = buildJsonObject {
                    put("tmdb_id", tmdbId)
                    put("media_type", mediaType)
                }
                val response = supabase.functions.invoke("resolve-video", body)
                val text = response.bodyAsText()
                if (text.isNotBlank()) {
                    try {
                        val json = Json.parseToJsonElement(text).jsonObject
                        val url = json["url"]?.jsonPrimitive?.content
                        if (!url.isNullOrBlank()) {
                            return@withContext url
                        }
                    } catch (_: Exception) {
                        if (text.startsWith("http://") || text.startsWith("https://")) {
                            return@withContext text.trim()
                        }
                    }
                }
            } catch (_: Exception) {
                // Edge function unavailable or failed, fallback to sample streams
            }
        }

        // Deterministic video URL per title so each title has consistent playback
        val streamIndex = abs(tmdbId) % sampleStreams.size
        sampleStreams[streamIndex]
    }
}
