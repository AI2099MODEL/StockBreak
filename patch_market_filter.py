import re

with open('app/src/main/java/com/example/MarketScreen.kt', 'r') as f:
    content = f.read()

new_videos = """val allVideos = listOf(
    VideoItem("Nifty 50 Analysis for tomorrow", "Zee Business", "Hindi", "QKJG6dGJFLo", "10 mins ago", "Nifty"),
    VideoItem("Top Stocks to Buy For Tomorrow", "CNBC Awaaz", "Hindi", "YvCyYGIBoUg", "1 hour ago", "CNBC Awaaz"),
    VideoItem("BSE SENSEX hits new high! What next?", "Zee Business", "Hindi", "Gowyym-GHoQ", "3 hours ago", "BSE Sensex"),
    VideoItem("Nifty Daily Analysis & Target", "Invest Aaj For Kal", "Hindi", "_WaDHI8-wvY", "5 hours ago", "Nifty"),
    VideoItem("Bank Nifty & Sensex Expiry Strategy", "Zee Business", "Hindi", "c5pj7yc8x4o", "1 day ago", "Zee Business"),
    VideoItem("Market Strategy: Nifty support levels", "CNBC Awaaz", "Hindi", "DNEBlR6ykIE", "2 days ago", "CNBC Awaaz"),
    VideoItem("Anil Singhvi Strategy for Nifty", "Zee Business", "Hindi", "Ax4GThw0Njk", "1 hour ago", "Zee Business"),
    VideoItem("Stock Market Crash Reason", "CNBC Awaaz", "Hindi", "G8Y0lyrz7xM", "4 hours ago", "Nifty")
)"""

content = content.replace("data class VideoItem(\n    val title: String,\n    val channel: String,\n    val language: String,\n    val videoId: String,\n    val timeAgo: String\n)",
"""data class VideoItem(
    val title: String,
    val channel: String,
    val language: String,
    val videoId: String,
    val timeAgo: String,
    val category: String = "Nifty"
)""")

old_videos_block = """val allVideos = listOf(
    VideoItem("Share Market LIVE Updates: Nifty & Sensex", "Zee Business", "Hindi", "36YnV9STBqc", "10 mins ago"),
    VideoItem("Top Stocks to Buy For Tomorrow", "CNBC Awaaz", "Hindi", "k_QpdE3J5pU", "1 hour ago"),
    VideoItem("BSE SENSEX hits new high! What next?", "Zee Business", "Hindi", "W8z0O9kC3aY", "3 hours ago"),
    VideoItem("Nifty 50 Analysis for tomorrow", "CNBC Awaaz", "Hindi", "t0Q2otsqC4I", "5 hours ago"),
    VideoItem("Bank Nifty & Sensex Expiry Strategy", "Zee Business", "Hindi", "V_m98Z9tkZg", "1 day ago"),
    VideoItem("Market Strategy: Nifty support levels", "CNBC Awaaz", "Hindi", "dQw4w9WgXcQ", "2 days ago"),
    VideoItem("Zee Business LIVE TV", "Zee Business", "Hindi", "7WTd3-c-I9g", "1 hour ago"),
    VideoItem("CNBC Awaaz LIVE Updates", "CNBC Awaaz", "Hindi", "3_g2un5M350", "4 hours ago")
)"""

content = content.replace(old_videos_block, new_videos)

content = content.replace('import androidx.compose.foundation.lazy.items\n', 'import androidx.compose.foundation.lazy.items\nimport androidx.compose.foundation.lazy.LazyRow\n')

market_screen_content = """@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(modifier: Modifier = Modifier) {
    val filters = listOf("All", "Nifty", "Zee Business", "CNBC Awaaz", "BSE Sensex")
    var selectedFilter by remember { mutableStateOf("All") }
    
    var videos by remember { mutableStateOf(allVideos.shuffled().take(5)) }
    var selectedVideoId by remember { mutableStateOf<String?>(null) }
    
    // Auto refresh every 30 mins
    LaunchedEffect(selectedFilter) {
        while (true) {
            val filtered = if (selectedFilter == "All") allVideos else allVideos.filter { 
                it.category.contains(selectedFilter, ignoreCase = true) || 
                it.channel.contains(selectedFilter, ignoreCase = true) ||
                it.title.contains(selectedFilter, ignoreCase = true)
            }
            videos = filtered.shuffled().take(5)
            delay(30 * 60 * 1000L) // 30 minutes
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text("Market News & Videos", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
        
        if (selectedVideoId != null) {
            YouTubePlayer(
                videoId = selectedVideoId!!,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            )
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(videos) { video ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        selectedVideoId = video.videoId
                    },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        if (selectedVideoId != video.videoId) {
                            AsyncImage(
                                model = "https://img.youtube.com/vi/${video.videoId}/hqdefault.jpg",
                                contentDescription = "Video Thumbnail",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(video.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(video.channel, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(video.language, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(video.timeAgo, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}"""

# Need to replace the whole MarketScreen function
import re
pattern = re.compile(r'@OptIn\(ExperimentalMaterial3Api::class\)\s*@Composable\s*fun MarketScreen.*?^}$', re.MULTILINE | re.DOTALL)
content = pattern.sub(market_screen_content, content)

with open('app/src/main/java/com/example/MarketScreen.kt', 'w') as f:
    f.write(content)
