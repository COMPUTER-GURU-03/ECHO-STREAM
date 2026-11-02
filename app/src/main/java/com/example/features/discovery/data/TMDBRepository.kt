package com.example.features.discovery.data

import com.example.BuildConfig
import com.example.core.data.SupabaseManager
import com.example.features.discovery.domain.Movie
import com.example.features.discovery.domain.MovieResponse
import com.example.features.discovery.domain.Season
import io.github.jan.supabase.functions.functions
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class TMDBRepository {
    private val functions = SupabaseManager.client?.functions
    private val json = Json { ignoreUnknownKeys = true }
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val directApiKey: String
        get() {
            val key = BuildConfig.TMDB_API_KEY.trim()
            return if (key.isEmpty() || key.contains("YOUR_TMDB_API_KEY")) "" else key
        }

    suspend fun getTrendingMovies(): List<Movie> = withContext(Dispatchers.IO) {
        val result = fetchEndpoint("trending/movie/week").map { it.copy(mediaType = it.mediaType ?: "movie") }
        if (result.isNotEmpty()) result else CuratedCinemaData.trendingMovies
    }

    suspend fun getPopularMovies(): List<Movie> = withContext(Dispatchers.IO) {
        val result = fetchEndpoint("movie/popular").map { it.copy(mediaType = "movie") }
        if (result.isNotEmpty()) result else CuratedCinemaData.popularMovies
    }

    suspend fun getPopularTVShows(): List<Movie> = withContext(Dispatchers.IO) {
        val result = fetchEndpoint("tv/popular").map { it.copy(mediaType = "tv") }
        if (result.isNotEmpty()) result else CuratedCinemaData.popularTV
    }

    suspend fun getTopRatedMovies(): List<Movie> = withContext(Dispatchers.IO) {
        val result = fetchEndpoint("movie/top_rated").map { it.copy(mediaType = "movie") }
        if (result.isNotEmpty()) result else CuratedCinemaData.topRated
    }

    suspend fun search(query: String): List<Movie> = withContext(Dispatchers.IO) {
        val result = fetchEndpoint("search/multi", mapOf("query" to query))
        if (result.isNotEmpty()) result else {
            CuratedCinemaData.trendingMovies.filter {
                it.displayTitle.contains(query, ignoreCase = true) ||
                        (it.overview?.contains(query, ignoreCase = true) == true)
            }
        }
    }

    suspend fun getTitleDetails(id: Int, mediaType: String): Movie? = withContext(Dispatchers.IO) {
        val endpoint = "$mediaType/$id"

        // 1. Try Supabase Edge Function if available
        if (functions != null) {
            try {
                val body = buildJsonObject {
                    put("endpoint", endpoint)
                    putJsonObject("params") {
                        put("append_to_response", "credits")
                    }
                }
                val response = functions.invoke("tmdb-proxy", body)
                val text = response.bodyAsText()
                if (text.isNotBlank() && !text.contains("\"error\":")) {
                    return@withContext json.decodeFromString<Movie>(text)
                }
            } catch (e: Exception) {
                // fall through to direct call
            }
        }

        // 2. Direct TMDB API call with BuildConfig.TMDB_API_KEY
        if (directApiKey.isNotEmpty()) {
            try {
                val urlBuilder = "https://api.themoviedb.org/3/$endpoint".toHttpUrlOrNull()?.newBuilder()
                if (urlBuilder != null) {
                    urlBuilder.addQueryParameter("append_to_response", "credits")
                    val isBearer = directApiKey.startsWith("ey")
                    if (!isBearer) {
                        urlBuilder.addQueryParameter("api_key", directApiKey)
                    }
                    val requestBuilder = Request.Builder().url(urlBuilder.build())
                    if (isBearer) {
                        requestBuilder.addHeader("Authorization", "Bearer $directApiKey")
                    }
                    requestBuilder.addHeader("accept", "application/json")
                    val callResponse = okHttpClient.newCall(requestBuilder.build()).execute()
                    val bodyString = callResponse.body?.string()
                    if (callResponse.isSuccessful && !bodyString.isNullOrBlank()) {
                        return@withContext json.decodeFromString<Movie>(bodyString)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        CuratedCinemaData.findById(id)
    }

    suspend fun getSeasonDetails(tvId: Int, seasonNumber: Int): Season? = withContext(Dispatchers.IO) {
        val endpoint = "tv/$tvId/season/$seasonNumber"

        // 1. Try Supabase Edge Function
        if (functions != null) {
            try {
                val body = buildJsonObject {
                    put("endpoint", endpoint)
                }
                val response = functions.invoke("tmdb-proxy", body)
                val text = response.bodyAsText()
                if (text.isNotBlank() && !text.contains("\"error\":")) {
                    return@withContext json.decodeFromString<Season>(text)
                }
            } catch (e: Exception) {
                // fall through
            }
        }

        // 2. Direct TMDB API call
        if (directApiKey.isNotEmpty()) {
            try {
                val urlBuilder = "https://api.themoviedb.org/3/$endpoint".toHttpUrlOrNull()?.newBuilder()
                if (urlBuilder != null) {
                    val isBearer = directApiKey.startsWith("ey")
                    if (!isBearer) {
                        urlBuilder.addQueryParameter("api_key", directApiKey)
                    }
                    val requestBuilder = Request.Builder().url(urlBuilder.build())
                    if (isBearer) {
                        requestBuilder.addHeader("Authorization", "Bearer $directApiKey")
                    }
                    requestBuilder.addHeader("accept", "application/json")
                    val callResponse = okHttpClient.newCall(requestBuilder.build()).execute()
                    val bodyString = callResponse.body?.string()
                    if (callResponse.isSuccessful && !bodyString.isNullOrBlank()) {
                        return@withContext json.decodeFromString<Season>(bodyString)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        CuratedCinemaData.findById(tvId)?.seasons?.find { it.seasonNumber == seasonNumber }
    }

    private suspend fun fetchEndpoint(endpoint: String, params: Map<String, String> = emptyMap()): List<Movie> {
        // 1. Try Supabase Edge Function
        if (functions != null) {
            try {
                val body = buildJsonObject {
                    put("endpoint", endpoint)
                    putJsonObject("params") {
                        params.forEach { (key, value) -> put(key, value) }
                    }
                }
                val response = functions.invoke("tmdb-proxy", body)
                val text = response.bodyAsText()
                if (text.isNotBlank() && !text.contains("\"error\":")) {
                    val movieResponse = json.decodeFromString<MovieResponse>(text)
                    if (movieResponse.results.isNotEmpty()) {
                        return movieResponse.results
                    }
                }
            } catch (e: Exception) {
                // fall through to direct call
            }
        }

        // 2. Direct TMDB API Call
        if (directApiKey.isNotEmpty()) {
            try {
                val urlBuilder = "https://api.themoviedb.org/3/$endpoint".toHttpUrlOrNull()?.newBuilder()
                if (urlBuilder != null) {
                    params.forEach { (key, value) -> urlBuilder.addQueryParameter(key, value) }
                    val isBearer = directApiKey.startsWith("ey")
                    if (!isBearer) {
                        urlBuilder.addQueryParameter("api_key", directApiKey)
                    }
                    val requestBuilder = Request.Builder().url(urlBuilder.build())
                    if (isBearer) {
                        requestBuilder.addHeader("Authorization", "Bearer $directApiKey")
                    }
                    requestBuilder.addHeader("accept", "application/json")
                    val callResponse = okHttpClient.newCall(requestBuilder.build()).execute()
                    val bodyString = callResponse.body?.string()
                    if (callResponse.isSuccessful && !bodyString.isNullOrBlank()) {
                        val movieResponse = json.decodeFromString<MovieResponse>(bodyString)
                        return movieResponse.results
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return emptyList()
    }
}
