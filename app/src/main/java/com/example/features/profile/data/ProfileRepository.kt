package com.example.features.profile.data

import com.example.core.data.SupabaseManager
import com.example.features.profile.domain.Profile
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileRepository {
    private val client = SupabaseManager.client?.postgrest?.let { it["profiles"] }

    suspend fun getProfile(userId: String): Profile? = withContext(Dispatchers.IO) {
        try {
            client?.select {
                filter {
                    eq("user_id", userId)
                }
            }?.decodeSingleOrNull<Profile>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateProfile(profile: Profile) = withContext(Dispatchers.IO) {
        try {
            client?.update(profile) {
                filter {
                    eq("user_id", profile.userId)
                }
            }
            client != null
        } catch (e: Exception) {
            false
        }
    }
}
