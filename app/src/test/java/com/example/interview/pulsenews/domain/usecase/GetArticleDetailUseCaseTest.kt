package com.example.interview.pulsenews.domain.usecase

import com.example.interview.pulsenews.core.common.Result
import com.example.interview.pulsenews.domain.model.Article
import com.example.interview.pulsenews.domain.repository.NewsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okio.IOException
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs


class GetArticleDetailUseCaseTest {

    private val repository: NewsRepository = mockk()
    private val useCase = GetArticleDetailUseCase(repository)

    @Test
    fun `invoke returns success when repository returns success`() = runTest {
        val article = fakeArticles[0]
        val articleId = "article-1"
        coEvery { repository.getArticle(articleId) } returns Result.Success(article)

        val result = useCase(articleId)
        assertIs<Result.Success<Article>>(result)
        assertEquals(article, result.data)
        coVerify(exactly = 1) { repository.getArticle(articleId) }
    }

    @Test
    fun `invoke returns Error when repository returns Error`() = runTest {
        val articleId = "article-1"
        val exception = IOException("Article Not Found")
        coEvery { repository.getArticle(articleId) } returns Result.Error(
            exception,
            message = "Article Not Found"
        )

        val result = useCase(articleId)
        assertIs<Result.Error>(result)
        assertEquals(exception, result.exception)
    }

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
}