package com.example.features.discovery.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Movie(
    val id: Int,
    val title: String? = null,
    val name: String? = null, // for TV shows
    val tagline: String? = null,
    val runtime: Int? = null, // runtime in minutes for movies
    val status: String? = null,
    @SerialName("poster_path")
    val posterPath: String? = null,
    @SerialName("backdrop_path")
    val backdropPath: String? = null,
    @SerialName("release_date")
    val releaseDate: String? = null,
    @SerialName("first_air_date")
    val firstAirDate: String? = null,
    @SerialName("vote_average")
    val voteAverage: Double? = null,
    val overview: String? = null,
    @SerialName("media_type")
    val mediaType: String? = null,
    val genres: List<Genre>? = null,
    @SerialName("credits")
    val credits: Credits? = null,
    @SerialName("number_of_seasons")
    val numberOfSeasons: Int? = null,
    @SerialName("number_of_episodes")
    val numberOfEpisodes: Int? = null,
    val seasons: List<Season>? = null
) {
    val displayTitle: String get() = title ?: name ?: "Unknown"
    val releaseYear: String get() = (releaseDate ?: firstAirDate)?.take(4) ?: ""
    val posterUrl: String get() = "https://image.tmdb.org/t/p/w500$posterPath"
    val backdropUrl: String get() = "https://image.tmdb.org/t/p/original$backdropPath"

    val formattedRuntime: String
        get() {
            val mins = runtime ?: return ""
            val h = mins / 60
            val m = mins % 60
            return if (h > 0) "${h}h ${m}m" else "${m}m"
        }
}

@Serializable
data class Season(
    val id: Int = 0,
    val name: String? = null,
    val overview: String? = null,
    @SerialName("poster_path")
    val posterPath: String? = null,
    @SerialName("season_number")
    val seasonNumber: Int = 1,
    @SerialName("episode_count")
    val episodeCount: Int? = null,
    val episodes: List<Episode>? = null
) {
    val posterUrl: String get() = "https://image.tmdb.org/t/p/w500$posterPath"
    val displayName: String get() = name ?: "Season $seasonNumber"
}

@Serializable
data class Episode(
    val id: Int,
    val name: String,
    val overview: String? = null,
    @SerialName("episode_number")
    val episodeNumber: Int,
    @SerialName("season_number")
    val seasonNumber: Int? = null,
    @SerialName("still_path")
    val stillPath: String? = null,
    @SerialName("vote_average")
    val voteAverage: Double? = null,
    @SerialName("runtime")
    val runtime: Int? = null
) {
    val stillUrl: String get() = "https://image.tmdb.org/t/p/w500$stillPath"
    val formattedDuration: String
        get() {
            val mins = runtime ?: return ""
            return "${mins}m"
        }
}

@Serializable
data class Genre(
    val id: Int,
    val name: String
)

@Serializable
data class Credits(
    val cast: List<CastMember>
)

@Serializable
data class CastMember(
    val id: Int,
    val name: String,
    val character: String? = null,
    @SerialName("profile_path")
    val profilePath: String? = null
) {
    val profileUrl: String get() = "https://image.tmdb.org/t/p/w185$profilePath"
}

@Serializable
data class MovieResponse(
    val page: Int,
    val results: List<Movie>,
    @SerialName("total_pages")
    val totalPages: Int,
    @SerialName("total_results")
    val totalResults: Int
)
