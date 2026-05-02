package com.example.interview.coinwatch.feature.feed.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.interview.coinwatch.domain.model.Coin

@Composable
fun CoinCard(
    modifier: Modifier = Modifier,
    coin: Coin,
    onCoinClick: (String) -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable {
                onCoinClick(coin.id)
            },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {

            AsyncImage(
                model = coin.icon,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${coin.name} ${coin.symbol}",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = "${coin.priceChangePercentageIn24Hrs}",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (coin.priceChangePercentageIn24Hrs > 0f) Color.Green else Color.Red
                    )
                }

                Spacer(modifier.height(4.dp))

                Text(
                    text = "${coin.currentPrice} $",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewCoinCard() {
    CoinCard(
        coin = Coin(
            id = "bitcoin",
            name = "Bitcoin",
            icon = "https://coin-images.coingecko.com/coins/images/1/large/bitcoin.png?1696501400",
            symbol = "btc",
            currentPrice = 75962.0,
            marketCap = 1521085717719,
            priceChangePercentageIn24Hrs = -1.38807,
            circulatingSupply = 20021959.0,
            totalVolume = 35748918354,
            high24h = 77432.0,
            low24h = 75706.0,
            lastUpdated = "2026-04-28T15:39:45.998Z"
        )
    ) { }
}