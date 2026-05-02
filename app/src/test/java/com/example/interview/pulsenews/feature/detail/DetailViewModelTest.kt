package com.example.interview.pulsenews.feature.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.interview.pulsenews.core.common.Result
import com.example.interview.pulsenews.domain.model.Article
import com.example.interview.pulsenews.domain.usecase.GetArticleDetailUseCase
import com.example.interview.pulsenews.domain.usecase.IsBookmarkedUseCase
import com.example.interview.pulsenews.domain.usecase.ToggleBookmarkUseCase
import com.example.interview.pulsenews.navigation.Screen
import com.example.interview.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs


class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getArticleUseCase = mockk<GetArticleDetailUseCase>()
    private val isBookmarkedUseCase = mockk<IsBookmarkedUseCase>()
    private val toggleBookmarkUseCase = mockk<ToggleBookmarkUseCase>()

    private val savedStateHandle = SavedStateHandle(
        mapOf(Screen.Detail.ARG_ARTICLE_ID to ARTICLE_ID)
    )

    private lateinit var viewModel: DetailViewModel

    companion object {
        private const val ARTICLE_ID = "article-1"
        private val fakeArticles = (1..200).map { i ->
            Article(
                id = "article-$i",
                title = "Breaking: Story number $i shocks the world",
                description = "A short blurb about story $i that makes you want to read more.",
                content = "Full content of article $i. Lorem ipsum dolor sit amet, " +
                        "consectetur adipiscing elit. Pellentesque habitant morbi tristique.",
                author = listOf("Priya S.", "Rahul K.", "Amit D.", "Sara T.")[i % 4],
                sourceName = listOf("The Hindu", "NDTV", "Mint", "TOI")[i % 4],
                imageUrl = "https://picsum.photos/seed/$i/400/200",
                publishedAt = "2025-04-${(i % 28 + 1).toString().padStart(2, '0')}T${
                    (i % 24).toString().padStart(2, '0')
                }:00:00Z",
                url = "https://example.com/article/$i"
            )
        }
        private val fakeArticle = fakeArticles[0]
    }

    @Before
    fun setup() {
        coEvery { getArticleUseCase(ARTICLE_ID) } returns Result.Success(fakeArticle)
        every { isBookmarkedUseCase(ARTICLE_ID) } returns flowOf(false)
        viewModel = DetailViewModel(
            isBookmarkedUseCase,
            toggleBookmarkUseCase,
            getArticleUseCase,
            savedStateHandle
        )
    }

    @Test
    fun `init triggers loadArticle and emits success state`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertIs<DetailUiState.Success>(state)
            assertEquals(fakeArticle, state.article)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadArticle emits loading then success on happy path`() = runTest {
        viewModel.uiState.test {
            skipItems(1)

            viewModel.loadArticle()

            assertIs<DetailUiState.Loading>(awaitItem())
            val success = awaitItem()
            assertIs<DetailUiState.Success>(success)
            assertEquals(fakeArticle, success.article)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `ViewModel throws if articleId is missing from SavedStateHandle`() = runTest {
        DetailViewModel(
            isBookmarkedUseCase,
            toggleBookmarkUseCase,
            getArticleUseCase,
            SavedStateHandle()
        )
    }

}