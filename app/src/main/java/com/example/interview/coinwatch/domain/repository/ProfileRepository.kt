package com.example.interview.coinwatch.domain.repository

import com.example.interview.coinwatch.domain.model.UserProfile

interface ProfileRepository {
    suspend fun getUserProfile(): UserProfile
}
