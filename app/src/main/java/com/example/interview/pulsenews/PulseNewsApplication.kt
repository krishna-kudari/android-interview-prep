package com.example.interview.pulsenews

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PulseNewsApplication : Application()

/**
 * app/
 * ├── src/main/
 * │   ├── AndroidManifest.xml
 * │   └── java/com/krishna/pulsenews/
 * │
 * ├── core/                          ← Shared across features
 * │   ├── common/
 * │   │   ├── Result.kt              ← sealed class Result<T>
 * │   │   ├── UiState.kt             ← sealed class UiState<T>
 * │   │   └── Extensions.kt
 * │   ├── network/
 * │   │   └── NetworkMonitor.kt      ← connectivity awareness
 * │   └── ui/
 * │       ├── theme/
 * │       │   ├── Color.kt
 * │       │   ├── Type.kt
 * │       │   └── Theme.kt
 * │       └── components/
 * │           ├── ErrorView.kt
 * │           ├── LoadingView.kt
 * │           └── ShimmerCard.kt
 * │
 * ├── domain/                        ← Pure Kotlin. Zero Android imports
 * │   ├── model/
 * │   │   └── Article.kt             ← clean domain model
 * │   ├── repository/
 * │   │   └── NewsRepository.kt      ← interface only
 * │   └── usecase/
 * │       ├── GetNewsFeedUseCase.kt
 * │       ├── SearchArticlesUseCase.kt
 * │       ├── GetArticleDetailUseCase.kt
 * │       ├── ToggleBookmarkUseCase.kt
 * │       └── GetBookmarksUseCase.kt
 * │
 * ├── data/                          ← implements domain contracts
 * │   ├── remote/
 * │   │   ├── model/
 * │   │   │   ├── ArticleDto.kt
 * │   │   │   └── ArticlesResponseDto.kt
 * │   │   ├── FakeNewsApi.kt
 * │   │   └── mapper/
 * │   │       └── ArticleDtoMapper.kt
 * │   ├── local/
 * │   │   ├── db/
 * │   │   │   └── PulseNewsDatabase.kt
 * │   │   ├── entity/
 * │   │   │   └── BookmarkEntity.kt
 * │   │   ├── dao/
 * │   │   │   └── BookmarkDao.kt
 * │   │   └── mapper/
 * │   │       └── BookmarkEntityMapper.kt
 * │   └── repository/
 * │       └── NewsRepositoryImpl.kt
 * │
 * ├── feature/                       ← Vertical slice per screen
 * │   ├── feed/
 * │   │   ├── FeedScreen.kt
 * │   │   ├── FeedViewModel.kt
 * │   │   └── FeedUiState.kt
 * │   ├── detail/
 * │   │   ├── DetailScreen.kt
 * │   │   ├── DetailViewModel.kt
 * │   │   └── DetailUiState.kt
 * │   └── bookmarks/
 * │       ├── BookmarksScreen.kt
 * │       ├── BookmarksViewModel.kt
 * │       └── BookmarksUiState.kt
 * │
 * ├── di/                            ← Hilt wiring
 * │   ├── DatabaseModule.kt
 * │   ├── RepositoryModule.kt
 * │   └── UseCaseModule.kt
 * │
 * └── navigation/
 *     ├── NavGraph.kt
 *     └── Screen.kt                  ← sealed class for routes
 */