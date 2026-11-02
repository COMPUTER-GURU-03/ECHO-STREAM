package com.example

import com.example.features.playback.domain.WatchProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun watchProgress_formatting_isCorrect() {
        assertEquals("00:00", WatchProgress.formatTimeMs(0L))
        assertEquals("01:05", WatchProgress.formatTimeMs(65_000L))
        assertEquals("1:01:05", WatchProgress.formatTimeMs(3_665_000L))
    }

    @Test
    fun watchProgress_calculations_areAccurate() {
        val progress = WatchProgress(
            userId = "test-user",
            tmdbId = 1234,
            mediaType = "movie",
            lastPosition = 30_000L,
            duration = 120_000L
        )

        assertEquals(0.25f, progress.progressPercentage, 0.001f)
        assertFalse(progress.isFinished)
        assertEquals(1L, progress.remainingMinutes)
        assertEquals("1m left", progress.formattedRemainingTime)
    }

    @Test
    fun watchProgress_finishedThreshold_isRespected() {
        val almostDone = WatchProgress(
            userId = "test-user",
            tmdbId = 5678,
            mediaType = "movie",
            lastPosition = 98_000L,
            duration = 100_000L
        )
        assertTrue(almostDone.isFinished)
    }

    @Test
    fun authRepository_inputValidation_flagsInvalidInputs() = kotlinx.coroutines.test.runTest {
        val repo = com.example.features.authentication.data.AuthRepository()
        val emptyEmailResult = repo.signIn("", "password123")
        assertTrue(emptyEmailResult.isFailure)

        val emptyPasswordResult = repo.signIn("user@example.com", "")
        assertTrue(emptyPasswordResult.isFailure)

        val invalidFormatResult = repo.signIn("invalid-email", "password123")
        assertTrue(invalidFormatResult.isFailure)
    }

    @Test
    fun supabaseConfig_isConfiguredAndAuthAvailable() {
        assertTrue(com.example.core.data.SupabaseConfig.isConfigured)
        assertEquals("https://yihabvxwytdfpgtiawex.supabase.co", com.example.core.data.SupabaseConfig.supabaseUrl)
        assertTrue(com.example.core.data.SupabaseConfig.supabaseAnonKey.startsWith("ey"))
    }
}
