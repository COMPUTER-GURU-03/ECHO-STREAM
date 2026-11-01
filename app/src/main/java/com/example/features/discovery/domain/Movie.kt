package com.example.features.discovery.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Movie(
    val id: Int,
    val title: String? = null,
    val name: String? = null, // for TV shows
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
    val credits: Credits? = null
) {
    val displayTitle: String get() = title ?: name ?: "Unknown"
    val releaseYear: String get() = (releaseDate ?: firstAirDate)?.take(4) ?: ""
    val posterUrl: String get() = "https://image.tmdb.org/t/p/w500$posterPath"
    val backdropUrl: String get() = "https://image.tmdb.org/t/p/original$backdropPath"
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
