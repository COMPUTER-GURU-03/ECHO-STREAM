package com.example.features.profile.data

import android.util.Log
import com.example.core.data.SupabaseManager
import com.example.features.profile.domain.Profile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileRepository {
    private val table get() = SupabaseManager.postgrest?.let { it["profiles"] }

    suspend fun getProfile(userId: String): Profile? = withContext(Dispatchers.IO) {
        val postgrestTable = table ?: return@withContext null
        try {
            // First check user_id
            val byUserId = postgrestTable.select {
                filter {
                    eq("user_id", userId)
                }
            }.decodeSingleOrNull<Profile>()

            if (byUserId != null) return@withContext byUserId

            // Fallback check by primary id
            postgrestTable.select {
                filter {
                    eq("id", userId)
                }
            }.decodeSingleOrNull<Profile>()
        } catch (e: Exception) {
            Log.e("ProfileRepository", "Error loading profile for $userId", e)
            null
        }
    }

    suspend fun updateProfile(profile: Profile): Boolean = withContext(Dispatchers.IO) {
        val postgrestTable = table ?: return@withContext false
        try {
            postgrestTable.upsert(profile)
            true
        } catch (e: Exception) {
            try {
                postgrestTable.update(profile) {
                    filter {
                        eq("user_id", profile.userId)
                    }
                }
                true
            } catch (e2: Exception) {
                Log.e("ProfileRepository", "Error updating profile for ${profile.userId}", e2)
                false
            }
        }
    }
}
