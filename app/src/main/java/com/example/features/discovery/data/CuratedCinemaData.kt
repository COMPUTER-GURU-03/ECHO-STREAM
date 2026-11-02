package com.example.features.discovery.data

import com.example.features.discovery.domain.CastMember
import com.example.features.discovery.domain.Credits
import com.example.features.discovery.domain.Episode
import com.example.features.discovery.domain.Genre
import com.example.features.discovery.domain.Movie
import com.example.features.discovery.domain.Season

/**
 * Curated cinematic fallback metadata.
 * Ensures the app displays rich hero banners, rails, cast, and playback
 * even before the user supplies their own TMDB API key in the Secrets panel.
 */
object CuratedCinemaData {
    val dunePartTwo = Movie(
        id = 693134,
        title = "Dune: Part Two",
        tagline = "Long live the fighters.",
        runtime = 166,
        status = "Released",
        posterPath = "/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
        backdropPath = "/xOMo8BRK7PfcJv9JCnx7s52002.jpg",
        releaseDate = "2024-03-01",
        voteAverage = 8.2,
        overview = "Follow the mythic journey of Paul Atreides as he unites with Chani and the Fremen while on a warpath of revenge against the conspirators who destroyed his family.",
        mediaType = "movie",
        genres = listOf(Genre(878, "Science Fiction"), Genre(12, "Adventure")),
        credits = Credits(
            cast = listOf(
                CastMember(1190668, "Timothée Chalamet", "Paul Atreides", "/BE2sdjpgsa2rNTFa66f7upkaOP.jpg"),
                CastMember(505710, "Zendaya", "Chani", "/rWChA97fTfZvdD9B1bN8Q0gYw2C.jpg"),
                CastMember(932967, "Rebecca Ferguson", "Lady Jessica", "/6NRL2Z9qLw2t3d8J5Yt1B8Vq0wQ.jpg"),
                CastMember(7399, "Javier Bardem", "Stilgar", "/gT2k0Q8tFvT9vE6tB2y0WqA6vPq.jpg")
            )
        )
    )

    val interstellar = Movie(
        id = 157336,
        title = "Interstellar",
        tagline = "Mankind was born on Earth. It was never meant to die here.",
        runtime = 169,
        status = "Released",
        posterPath = "/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
        backdropPath = "/xJHokMbljvjADYdit5fK5VQsXEG.jpg",
        releaseDate = "2014-11-07",
        voteAverage = 8.4,
        overview = "The adventures of a group of explorers who make use of a newly discovered wormhole to surpass the limitations on human space travel and conquer the vast distances involved in an interstellar voyage.",
        mediaType = "movie",
        genres = listOf(Genre(12, "Adventure"), Genre(18, "Drama"), Genre(878, "Science Fiction")),
        credits = Credits(
            cast = listOf(
                CastMember(10297, "Matthew McConaughey", "Joseph Cooper", "/sY2mwpafCw9vr1UkYH25WxNVF3a.jpg"),
                CastMember(1813, "Anne Hathaway", "Dr. Amelia Brand", "/tLelKoPNiyJCSEtQT81FGg1wF9K.jpg"),
                CastMember(8210, "Jessica Chastain", "Murphy Cooper", "/lodMzLqiMYw7Z1n0pT5oQW9yB8K.jpg"),
                CastMember(3895, "Michael Caine", "Professor John Brand", "/klNx5vQ9pE3D0jM1wB5y0YQ9T6V.jpg")
            )
        )
    )

    val bladeRunner2049 = Movie(
        id = 335984,
        title = "Blade Runner 2049",
        tagline = "There's still a page left.",
        runtime = 164,
        status = "Released",
        posterPath = "/gajva2L0rPYkEWjzgFlBXCAVBE5.jpg",
        backdropPath = "/ilRyASDvt7vRnv3h9n19A4q5nkg.jpg",
        releaseDate = "2017-10-06",
        voteAverage = 7.6,
        overview = "Thirty years after the events of the first film, a new blade runner, LAPD Officer K, unearths a long-buried secret that has the potential to plunge what's left of society into chaos.",
        mediaType = "movie",
        genres = listOf(Genre(878, "Science Fiction"), Genre(9648, "Mystery")),
        credits = Credits(
            cast = listOf(
                CastMember(30614, "Ryan Gosling", "Officer K", "/3E6qK6wTfW4E3y8V1vA1Q6a4vPq.jpg"),
                CastMember(3, "Harrison Ford", "Rick Deckard", "/5c3gK4wTfW4E3y8V1vA1Q6a4vPq.jpg"),
                CastMember(224513, "Ana de Armas", "Joi", "/vkoG8W8g9vB4D9y1vA1Q6a4vPq.jpg")
            )
        )
    )

    val oppenheimer = Movie(
        id = 872585,
        title = "Oppenheimer",
        tagline = "The world forever changes.",
        runtime = 181,
        status = "Released",
        posterPath = "/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
        backdropPath = "/fm6KqXpk3M2HVveHwCrBSSBaO0V.jpg",
        releaseDate = "2023-07-21",
        voteAverage = 8.1,
        overview = "The story of J. Robert Oppenheimer's role in the development of the atomic bomb during World War II.",
        mediaType = "movie",
        genres = listOf(Genre(18, "Drama"), Genre(36, "History")),
        credits = Credits(
            cast = listOf(
                CastMember(2037, "Cillian Murphy", "J. Robert Oppenheimer", "/dm6vK4wTfW4E3y8V1vA1Q6a4vPq.jpg"),
                CastMember(5081, "Emily Blunt", "Katherine 'Kitty' Oppenheimer", "/n3O2vK4wTfW4E3y8V1vA1Q6a4vPq.jpg"),
                CastMember(3223, "Robert Downey Jr.", "Lewis Strauss", "/5qW8g9vB4D9y1vA1Q6a4vPq.jpg")
            )
        )
    )

    val cyberpunkSeries = Movie(
        id = 105248,
        name = "Cyberpunk: Edgerunners",
        tagline = "City of Dreams, City of Gangs.",
        status = "Ended",
        posterPath = "/7jSWta09n0QxGaBlz7v6k7i81wR.jpg",
        backdropPath = "/vfvyk2s45244S11L4v33g904y8G.jpg",
        firstAirDate = "2022-09-13",
        voteAverage = 8.6,
        overview = "A street kid trying to survive in a technology and body modification-obsessed city of the future. Having everything to lose, he chooses to stay alive by becoming an edgerunner.",
        mediaType = "tv",
        numberOfSeasons = 1,
        numberOfEpisodes = 10,
        genres = listOf(Genre(16, "Animation"), Genre(878, "Sci-Fi & Fantasy"), Genre(10759, "Action & Adventure")),
        seasons = listOf(
            Season(
                id = 1,
                name = "Season 1",
                seasonNumber = 1,
                episodeCount = 10,
                episodes = listOf(
                    Episode(1, "Let You Down", "A tech tragedy drives street kid David to alter his destiny with a high-grade military cyberware.", 1, 1, "/vfvyk2s45244S11L4v33g904y8G.jpg", 8.4, 25),
                    Episode(2, "Like a Boy", "With his new cybernetic spine pushing his limits, David runs into Lucy, a mysterious netrunner.", 2, 1, "/7jSWta09n0QxGaBlz7v6k7i81wR.jpg", 8.6, 24),
                    Episode(3, "Smooth Criminal", "Surrounded by a crew of veteran mercenaries, David aims to prove he has what it takes.", 3, 1, "/ilRyASDvt7vRnv3h9n19A4q5nkg.jpg", 8.8, 26)
                )
            )
        ),
        credits = Credits(
            cast = listOf(
                CastMember(1, "KENN", "David Martinez", null),
                CastMember(2, "Aoi Yuuki", "Lucy", null)
            )
        )
    )

    val strangerThings = Movie(
        id = 66732,
        name = "Stranger Things",
        tagline = "Every ending has a beginning.",
        status = "Returning Series",
        posterPath = "/49WJfeN0moxb9IPfGn8AIqMGskD.jpg",
        backdropPath = "/56v2KjBlU4XaOv9rVYEQypROD7P.jpg",
        firstAirDate = "2016-07-15",
        voteAverage = 8.6,
        overview = "When a young boy vanishes, a small town uncovers a mystery involving secret experiments, terrifying supernatural forces and one strange little girl.",
        mediaType = "tv",
        numberOfSeasons = 4,
        numberOfEpisodes = 34,
        genres = listOf(Genre(10765, "Sci-Fi & Fantasy"), Genre(18, "Drama"), Genre(9648, "Mystery")),
        seasons = listOf(
            Season(
                id = 1,
                name = "Season 1",
                seasonNumber = 1,
                episodeCount = 8,
                episodes = listOf(
                    Episode(1, "The Vanishing of Will Byers", "On his way home from a friend's house, young Will sees something terrifying.", 1, 1, "/56v2KjBlU4XaOv9rVYEQypROD7P.jpg", 8.5, 48),
                    Episode(2, "The Weirdo on Maple Street", "Lucas, Mike and Dustin try to talk to the girl they found in the woods.", 2, 1, "/49WJfeN0moxb9IPfGn8AIqMGskD.jpg", 8.7, 55)
                )
            )
        ),
        credits = Credits(
            cast = listOf(
                CastMember(135651, "Millie Bobby Brown", "Eleven", "/2cLjX5vQ9pE3D0jM1wB5y0YQ9T6V.jpg"),
                CastMember(55638, "Winona Ryder", "Joyce Byers", "/dm6vK4wTfW4E3y8V1vA1Q6a4vPq.jpg"),
                CastMember(31519, "David Harbour", "Jim Hopper", "/n3O2vK4wTfW4E3y8V1vA1Q6a4vPq.jpg")
            )
        )
    )

    val trendingMovies: List<Movie> = listOf(dunePartTwo, interstellar, cyberpunkSeries, oppenheimer, bladeRunner2049, strangerThings)
    val popularMovies: List<Movie> = listOf(dunePartTwo, interstellar, oppenheimer, bladeRunner2049)
    val popularTV: List<Movie> = listOf(cyberpunkSeries, strangerThings)
    val topRated: List<Movie> = listOf(interstellar, dunePartTwo, cyberpunkSeries, oppenheimer)

    fun findById(id: Int): Movie? {
        return trendingMovies.find { it.id == id }
    }
}
