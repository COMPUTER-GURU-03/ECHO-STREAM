package com.example.features.discovery.data

import com.example.core.data.SupabaseManager
import com.example.features.discovery.domain.Movie
import com.example.features.discovery.domain.MovieResponse
import io.github.jan.supabase.functions.functions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import io.ktor.client.statement.bodyAsText

class TMDBRepository {
    private val functions = SupabaseManager.client?.functions
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getTrendingMovies(): List<Movie> = withContext(Dispatchers.IO) {
        callProxy("trending/movie/week")
    }

    suspend fun getPopularMovies(): List<Movie> = withContext(Dispatchers.IO) {
        callProxy("movie/popular")
    }

    suspend fun getPopularTVShows(): List<Movie> = withContext(Dispatchers.IO) {
        callProxy("tv/popular")
    }

    suspend fun search(query: String): List<Movie> = withContext(Dispatchers.IO) {
        callProxy("search/multi", mapOf("query" to query))
    }

    suspend fun getTitleDetails(id: Int, mediaType: String): Movie? = withContext(Dispatchers.IO) {
        val endpoint = "$mediaType/$id"
        val client = functions ?: return@withContext null
        
        try {
            val body = buildJsonObject {
                put("endpoint", endpoint)
                putJsonObject("params") {
                    put("append_to_response", "credits")
                }
            }

            val response = client.invoke("tmdb-proxy", body)
            json.decodeFromString<Movie>(response.bodyAsText())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun callProxy(endpoint: String, params: Map<String, String> = emptyMap()): List<Movie> {
        val client = functions ?: return emptyList()
        
        return try {
            val body = buildJsonObject {
                put("endpoint", endpoint)
                putJsonObject("params") {
                    params.forEach { (key, value) -> put(key, value) }
                }
            }

            val response = client.invoke("tmdb-proxy", body)
            val movieResponse = json.decodeFromString<MovieResponse>(response.bodyAsText())
            movieResponse.results
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
