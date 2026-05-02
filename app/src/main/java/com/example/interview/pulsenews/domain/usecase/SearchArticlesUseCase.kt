package com.example.interview.pulsenews.domain.usecase

import androidx.paging.PagingData
import com.example.interview.pulsenews.domain.model.Article
import com.example.interview.pulsenews.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchArticlesUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    operator fun invoke(query: String): Flow<PagingData<Article>> = repository.searchArticles(query)
}