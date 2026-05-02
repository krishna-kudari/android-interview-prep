package com.example.interview.pulsenews.feature.feed.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.interview.pulsenews.core.common.toRelativeTime
import com.example.interview.pulsenews.core.ui.theme.BookmarkYellow
import com.example.interview.pulsenews.domain.model.Article

@Composable
fun ArticleCard(
    modifier: Modifier = Modifier,
    article: Article,
    isBookmarked: Boolean = false,
    onArticleClick: (String) -> Unit,
    onBookmarkToggle: (Article) -> Unit,
) {

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                onArticleClick(article.id)
            },
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            AsyncImage(
                model = article.imageUrl,
                contentDescription = article.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                if (isBookmarked) {
                    Surface(
                        color = BookmarkYellow.copy(alpha = 0.1f),
                        modifier = Modifier.padding(bottom = 4.dp),
                        shape = RoundedCornerShape(size = 4.dp)
                    ) {
                        Text(
                            text = "Saved Offline",
                            style = MaterialTheme.typography.labelSmall,
                            color = BookmarkYellow,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "${article.sourceName} · ${article.publishedAt.toRelativeTime()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { onBookmarkToggle(article) }
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = if (isBookmarked) "Remove Bookmark" else "Add Bookmark",
                    tint = if (isBookmarked) BookmarkYellow else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
@RequiresApi(Build.VERSION_CODES.O)
@Preview
fun ArticleCardPreview() {
    ArticleCard(
        modifier = Modifier,
        article = Article(
            id = "1",
            title = "Article 1",
            description = "Description of Article 1",
            content = "Lorem Ipsum Desc Content",
            author = "Raju K",
            sourceName = "NDTV",
            publishedAt = "",
            isBookmarked = true,
            imageUrl = "",
            url = ""
        ),
        isBookmarked = true,
        onArticleClick = {},
    ) { }
}