content = """package com.example

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage

data class VideoItem(
    val title: String,
    val channel: String,
    val language: String,
    val videoId: String,
    val timeAgo: String,
    val category: String
)

val sampleVideos = listOf(
    VideoItem("Nifty & Bank Nifty Daily Analysis & Target", "Invest Aaj For Kal", "Hindi", "_WaDHI8-wvY", "4 hours ago", "Nifty"),
    VideoItem("Top 5 Breakout Stocks for Tomorrow | BTST Signals", "Stock Pro", "Hindi", "v74_mH7cQik", "6 hours ago", "Breakout"),
    VideoItem("Intraday Trading Strategy using RSI & MACD Crossover", "Pushkar Raj Thakur", "Hindi", "3m6M8e1J-0U", "1 day ago", "Learning"),
    VideoItem("Indian Stock Market Live Commentary & Analysis", "Zee Business", "Hindi", "q3-g0NRE_bA", "Live", "News"),
    VideoItem("Swing Trading Masterclass: How to Identify Support & Resistance", "CA Rachana Phadke Ranade", "English", "8iQ_J4s7k0E", "2 days ago", "Learning"),
    VideoItem("Nifty 200 Weekly Target & Chart Analysis", "Trading With Vivek", "Hindi", "g52L_J3dG4M", "1 day ago", "Nifty")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(modifier: Modifier = Modifier) {
    var selectedFilter by remember { mutableStateOf("All") }
    var activePlayingVideoId by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val categories = listOf("All", "Nifty", "Breakout", "Learning", "News")

    val filteredVideos = if (selectedFilter == "All") {
        sampleVideos
    } else {
        sampleVideos.filter { it.category.equals(selectedFilter, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Market Insights & Videos",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Live YouTube analyses, breakout guides, and market updates",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Filter chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        val isSelected = selectedFilter == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = category },
                            label = { Text(category, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        // Active Inline Video Player Dialog / Card if selected
        activePlayingVideoId?.let { videoId ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp)),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Now Playing",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        TextButton(onClick = { activePlayingVideoId = null }) {
                            Text("Close", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                        }
                    }

                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                webViewClient = WebViewClient()
                                loadUrl("https://www.youtube.com/embed/$videoId?autoplay=1")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                    )
                }
            }
        }

        // Video Feed List
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredVideos) { video ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { activePlayingVideoId = video.videoId },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        // Thumbnail with Play Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        ) {
                            AsyncImage(
                                model = "https://img.youtube.com/vi/${video.videoId}/hqdefault.jpg",
                                contentDescription = video.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Dark overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f))
                            )

                            // Play Button Icon
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.Red,
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Video",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .size(32.dp)
                                )
                            }

                            // Language Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = video.language,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Details Section
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = video.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = video.channel,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${video.category} • ${video.timeAgo}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, "Watch '${video.title}' on YouTube: https://www.youtube.com/watch?v=${video.videoId}")
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share Video"))
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share Video",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
"""

with open('app/src/main/java/com/example/MarketScreen.kt', 'w') as f:
    f.write(content)

print("Created MarketScreen.kt")
