package com.example.interview.coinwatch.data.repository

import com.example.interview.coinwatch.domain.model.UserProfile
import com.example.interview.coinwatch.domain.repository.ProfileRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor() : ProfileRepository {

    override suspend fun getUserProfile(): UserProfile {
        delay(1_000L)
        return UserProfile(
            name = "Lal Laadle",
            email = "Laadle@gmail.com",
            bio = "hat ja laadle",
            avatarUrl = "https://i.pravatar.cc/300"
        )
    }
}
