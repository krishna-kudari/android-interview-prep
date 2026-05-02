package com.example.interview.pulsenews.domain.usecase

import com.example.interview.pulsenews.domain.model.Article
import com.example.interview.pulsenews.domain.repository.NewsRepository
import javax.inject.Inject

class ToggleBookmarkUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    suspend operator fun invoke(article: Article) = repository.toggleBookmark(article)
}