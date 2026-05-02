package com.example.interview.pulsenews.data.remote.api

import com.example.interview.pulsenews.data.remote.model.ArticleDto
import com.example.interview.pulsenews.data.remote.model.ArticlesResponse
import kotlinx.coroutines.delay

object FakeNewsApi {

    private val fakeArticles = (1..200).map { i ->
        ArticleDto(
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

    suspend fun getArticles(page: Int, pageSize: Int): ArticlesResponse {
        delay(800)
        val start = (page - 1) * pageSize
        val end = minOf(fakeArticles.size, start + pageSize)
        val pageData =
            if (start >= fakeArticles.size) emptyList() else fakeArticles.subList(start, end)
        return ArticlesResponse(
            articles = pageData,
            page = page,
            pageSize = pageSize,
            totalResults = fakeArticles.size
        )
    }

    suspend fun getArticle(id: String): ArticleDto {
        delay(400)
        return fakeArticles.firstOrNull { it.id == id }
            ?: throw NoSuchElementException("Article $id not found")
    }

    suspend fun searchArticles(query: String, page: Int, pageSize: Int): ArticlesResponse {
        delay(1000)
        val filteredList = fakeArticles.filter { it.title.contains(query, ignoreCase = true) }
        val start = (page - 1) * pageSize
        val end = minOf(filteredList.size, start + pageSize)
        val pageData =
            if (start >= filteredList.size) emptyList() else filteredList.subList(start, end)
        return ArticlesResponse(
            articles = pageData,
            page = page,
            pageSize = pageSize,
            totalResults = filteredList.size
        )
    }


}