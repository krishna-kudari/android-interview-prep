package com.example.interview.pulsenews.domain.usecase

import com.example.interview.pulsenews.domain.model.Article
import com.example.interview.pulsenews.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBookmarksUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    operator fun invoke(): Flow<List<Article>> = repository.observerBookmarks()
}