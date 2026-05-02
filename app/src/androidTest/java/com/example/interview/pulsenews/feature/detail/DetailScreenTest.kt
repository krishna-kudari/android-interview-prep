package com.example.interview.pulsenews.feature.detail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.interview.HiltTestActivity
import com.example.interview.pulsenews.core.ui.theme.PulseNewsTheme
import com.example.interview.pulsenews.domain.model.Article
import com.example.interview.pulsenews.feature.detail.testutil.TestTags
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DetailScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @BindValue
    @JvmField
    val viewModel: DetailViewModel = mockk(relaxed = true)

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
                url = "https://example.com/article/$i",
            )
        }
        private val fakeArticle = fakeArticles[0]
    }

    @Test
    fun showsArticleContent_whenStateIsSuccess() {
        every { viewModel.uiState } returns MutableStateFlow(DetailUiState.Success(fakeArticle))
        every { viewModel.isBookmarked } returns MutableStateFlow(false)

        composeRule.setContent {
            PulseNewsTheme {
                DetailScreen(
                    articleId = ARTICLE_ID,
                    onBackClick = {},
                )
            }
        }

        composeRule.onNodeWithTag(TestTags.DETAIL_CONTENT, useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText(fakeArticle.title, substring = true).assertIsDisplayed()
    }
}
