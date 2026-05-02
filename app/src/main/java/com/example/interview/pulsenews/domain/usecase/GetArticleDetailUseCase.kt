package com.example.interview.pulsenews.domain.usecase

import com.example.interview.pulsenews.core.common.Result
import com.example.interview.pulsenews.domain.model.Article
import com.example.interview.pulsenews.domain.repository.NewsRepository
import javax.inject.Inject

class GetArticleDetailUseCase @Inject constructor(
    private val repository: NewsRepository
) {
    suspend operator fun invoke(id: String) : Result<Article> = repository.getArticle(id)
}