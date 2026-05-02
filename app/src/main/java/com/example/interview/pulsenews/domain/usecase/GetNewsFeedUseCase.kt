package com.example.interview.pulsenews.domain.usecase

import com.example.interview.pulsenews.domain.repository.NewsRepository
import javax.inject.Inject

class GetNewsFeedUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    operator fun invoke() = newsRepository.getNewsFeed()
}