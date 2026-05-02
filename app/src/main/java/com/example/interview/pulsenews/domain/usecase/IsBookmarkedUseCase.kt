package com.example.interview.pulsenews.domain.usecase

import com.example.interview.pulsenews.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IsBookmarkedUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    operator fun invoke(articleId: String): Flow<Boolean> = repository.isBookmarked(articleId)
}