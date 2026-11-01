package com.example.core.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Home : Screen

    @Serializable
    data object Discover : Screen

    @Serializable
    data object Library : Screen
    
    @Serializable
    data object Downloads : Screen

    @Serializable
    data object Profile : Screen

    @Serializable
    data class TitleDetails(val tmdbId: Int, val mediaType: String) : Screen

    @Serializable
    data class Player(
        val tmdbId: Int,
        val mediaType: String,
        val title: String? = null,
        val posterPath: String? = null
    ) : Screen

    @Serializable
    data object Auth : Screen
}
