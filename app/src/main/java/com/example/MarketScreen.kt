package com.example

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import android.widget.Toast
import java.util.Calendar
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object VideoCache {
    var cachedVideos: List<VideoItem> = listOf(
        VideoItem(
            id = "fallback_1",
            title = "Live Stock Market Analysis & Intraday Trading Setup Today",
            channel = "Dhan",
            tag = "DHAN LATEST",
            tagBgColor = Color(0xFF5B21B6),
            videoId = "jfKfPfyJRdk",
            directUrl = "https://www.youtube.com/watch?v=jfKfPfyJRdk",
            timeAgo = "1h ago",
            category = "TECHNICALS",
            isLive = true
        ),
        VideoItem(
            id = "fallback_2",
            title = "Top Breakout Stocks to Watch for Swing & Intraday Trading",
            channel = "Groww",
            tag = "GROWW LATEST",
            tagBgColor = Color(0xFF00D09C),
            videoId = "3JZ_D3ELwOQ",
            directUrl = "https://www.youtube.com/watch?v=3JZ_D3ELwOQ",
            timeAgo = "3h ago",
            category = "TECHNICALS"
        ),
        VideoItem(
            id = "fallback_3",
            title = "Bank Nifty & Nifty 50 Options Trading Strategy & Expiry Analysis",
            channel = "Zee Business",
            tag = "ZEE BIZ LATEST",
            tagBgColor = Color(0xFFDC2626),
            videoId = "dQw4w9WgXcQ",
            directUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            timeAgo = "5h ago",
            category = "OPTIONS"
        ),
        VideoItem(
            id = "fallback_4",
            title = "Fundamental Analysis & Q4 Earnings Preview for Top Bluechip Stocks",
            channel = "CNBC Awaaz",
            tag = "CNBC AWAAZ",
            tagBgColor = Color(0xFF0284C7),
            videoId = "L_LUpnjgPso",
            directUrl = "https://www.youtube.com/watch?v=L_LUpnjgPso",
            timeAgo = "1d ago",
            category = "FUNDAMENTALS"
        ),
        VideoItem(
            id = "fallback_5",
            title = "Swing Trading Blueprint: How to Identify Support & Resistance Breakouts",
            channel = "ET Now Swadesh",
            tag = "ET NOW SWADESH",
            tagBgColor = Color(0xFFD97706),
            videoId = "5qap5aO4i9A",
            directUrl = "https://www.youtube.com/watch?v=5qap5aO4i9A",
            timeAgo = "2d ago",
            category = "BASICS"
        )
    )
}

data class VideoItem(
    val id: String,
    val title: String,
    val channel: String,
    val tag: String,
    val tagBgColor: Color,
    val videoId: String,
    val directUrl: String,
    val timeAgo: String,
    val category: String, // "BASICS", "TECHNICALS", "OPTIONS", "FUNDAMENTALS", "LIVE"
    val isLive: Boolean = false,
    val isAvailable: Boolean = true,
    val publishedTimestamp: Long = 0L
)

// Helper to fetch live YouTube videos in guest mode via RSS-to-JSON API
suspend fun fetchYouTubeChannelVideos(
    channelId: String,
    channelName: String,
    tag: String,
    tagColor: Color
): List<VideoItem> = withContext(Dispatchers.IO) {
    val result = mutableListOf<VideoItem>()
    try {
        val rssUrl = "https://www.youtube.com/feeds/videos.xml?channel_id=$channelId"
        val apiUrl = "https://api.rss2json.com/v1/api.json?rss_url=" + URLEncoder.encode(rssUrl, "UTF-8")
        val conn = URL(apiUrl).openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        conn.requestMethod = "GET"
        // Guest mode headers (no user auth or cookies)
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
        conn.setRequestProperty("Accept", "application/json")

        if (conn.responseCode == 200) {
            val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
            val jsonObj = JSONObject(jsonStr)
            if (jsonObj.optString("status") == "ok") {
                val items = jsonObj.optJSONArray("items") ?: JSONArray()
                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val rawTitle = item.optString("title", "")
                    val title = android.text.Html.fromHtml(rawTitle, android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim()
                    val link = item.optString("link", "")
                    val guid = item.optString("guid", "")
                    val videoId = when {
                        guid.startsWith("yt:video:") -> guid.removePrefix("yt:video:")
                        link.contains("watch?v=") -> link.substringAfter("watch?v=").substringBefore("&")
                        link.contains("shorts/") -> link.substringAfter("shorts/").substringBefore("?")
                        else -> ""
                    }
                    if (videoId.isNotBlank()) {
                        val pubDate = item.optString("pubDate", "")
                        val parsedDate = try {
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(pubDate)
                        } catch (e: Exception) {
                            null
                        }
                        val publishedTimestamp = parsedDate?.time ?: 0L
                        val timeAgo = parsePubDateToTimeAgo(pubDate)
                        val category = when {
                            title.contains("Option", ignoreCase = true) || title.contains("F&O", ignoreCase = true) -> "OPTIONS"
                            title.contains("Pattern", ignoreCase = true) || title.contains("Breakout", ignoreCase = true) ||
                            title.contains("Chart", ignoreCase = true) || title.contains("Intraday", ignoreCase = true) ||
                            title.contains("Strategy", ignoreCase = true) -> "TECHNICALS"
                            title.contains("Fund", ignoreCase = true) || title.contains("Stock", ignoreCase = true) ||
                            title.contains("Result", ignoreCase = true) || title.contains("Earning", ignoreCase = true) -> "FUNDAMENTALS"
                            else -> "BASICS"
                        }

                        result.add(
                            VideoItem(
                                id = "yt_${channelId}_$videoId",
                                title = title,
                                channel = channelName,
                                tag = tag,
                                tagBgColor = tagColor,
                                videoId = videoId,
                                directUrl = if (link.isNotBlank()) link else "https://www.youtube.com/watch?v=$videoId",
                                timeAgo = timeAgo,
                                category = category,
                                isLive = false,
                                isAvailable = true,
                                publishedTimestamp = publishedTimestamp
                            )
                        )
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    result
}

fun parsePubDateToTimeAgo(pubDateStr: String): String {
    if (pubDateStr.isBlank()) return "Recently"
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val date = sdf.parse(pubDateStr) ?: return pubDateStr
        val now = System.currentTimeMillis()
        val diffMs = now - date.time
        val diffMins = diffMs / (1000 * 60)
        val diffHours = diffMins / 60
        val diffDays = diffHours / 24

        when {
            diffMins < 1 -> "Just now"
            diffMins < 60 -> "${diffMins}m ago"
            diffHours < 24 -> "${diffHours}h ago"
            diffDays < 7 -> "${diffDays}d ago"
            else -> SimpleDateFormat("MMM dd", Locale.US).format(date)
        }
    } catch (e: Exception) {
        "Recently"
    }
}

@Composable
fun BreakoutPerformanceSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val db = remember { MyApplication.database }
    val dao = remember { db.breakoutPerformanceDao() }
    val dailyLogDao = remember { db.dailyBreakoutLogDao() }

    val sixMonthsAgo = remember { System.currentTimeMillis() - (180L * 24 * 60 * 60 * 1000L) }
    val performances by dao.getBreakoutPerformanceSince(sixMonthsAgo).collectAsStateWithLifecycle(initialValue = emptyList())
    val dailyLogs by dailyLogDao.getDailyLogsSince(sixMonthsAgo).collectAsStateWithLifecycle(initialValue = emptyList())

    var isRefreshing by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("DAILY") } // "DAILY", "WEEKLY", "POSITIONS"
    val coroutineScope = rememberCoroutineScope()

    // Helper to verify Indian stock market hours (Mon-Fri 09:15 AM - 03:30 PM IST)
    val checkIsMarketOpen = {
        val cal = Calendar.getInstance()
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val isWeekday = dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val timeInMinutes = hour * 60 + minute
        isWeekday && (timeInMinutes in (9 * 60 + 15)..(15 * 60 + 30))
    }

    val isMarketOpen = remember { checkIsMarketOpen() }

    val totalInvestment = performances.sumOf { it.recommendedPrice }
    val totalCurrentValue = performances.sumOf { it.currentPrice }
    val totalPnlAmount = totalCurrentValue - totalInvestment
    val totalPnlPercent = if (totalInvestment > 0) (totalPnlAmount / totalInvestment) * 100 else 0.0
    val targetHitCount = performances.count { it.isTargetHit }

    // Group daily logs by week for 6-month weekly overview
    val weeklyPnlList = remember(dailyLogs) {
        if (dailyLogs.isEmpty()) emptyList()
        else {
            dailyLogs.chunked(7).mapIndexed { index, logsChunk ->
                val weekName = "Week ${index + 1}"
                val weekPnlAmount = logsChunk.sumOf { it.totalProfitLossAmount }
                val weekPnlPercent = logsChunk.map { it.totalProfitLossPercent }.average()
                val totalTrades = logsChunk.sumOf { it.tradesCount }
                val wins = logsChunk.sumOf { it.winningTrades }
                Triple(weekName, weekPnlAmount, Pair(weekPnlPercent, Pair(wins, totalTrades)))
            }
        }
    }

    val triggerManualScan = {
        if (!checkIsMarketOpen()) {
            Toast.makeText(
                context,
                "Market is Closed. Breakout P&L logging is active Monday-Friday 09:15 AM - 03:30 PM.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            coroutineScope.launch {
                try {
                    isRefreshing = true
                    val fetched = withContext(Dispatchers.IO) { StockScanner.scanMultiple("Breakouts") }
                    if (fetched.isNotEmpty()) {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val currentDateStr = dateFormat.format(Date())
                        var winningCount = 0
                        var losingCount = 0
                        var dayPnl = 0.0
                        var dayInvest = 0.0
                        val topList = mutableListOf<String>()

                        withContext(Dispatchers.IO) {
                            for (res in fetched) {
                                val existing = dao.getByTicker(res.ticker)
                                val recPrice = existing?.recommendedPrice ?: (res.recommendedPrice.takeIf { it > 0 } ?: res.price)
                                val curPrice = res.price
                                val pnlAmount = curPrice - recPrice
                                val pnlPct = if (recPrice > 0) (pnlAmount / recPrice) * 100 else 0.0
                                val targetPrice = res.target1 ?: (recPrice * 1.02)
                                val isTargetHit = curPrice >= targetPrice || pnlPct >= 2.0

                                val entity = BreakoutPerformanceEntity(
                                    id = existing?.id ?: 0,
                                    ticker = res.ticker,
                                    companyName = res.name,
                                    recommendedPrice = recPrice,
                                    currentPrice = curPrice,
                                    pnlAmount = pnlAmount,
                                    pnlPercent = pnlPct,
                                    isTargetHit = isTargetHit,
                                    targetPrice = targetPrice,
                                    appearedDate = existing?.appearedDate ?: currentDateStr,
                                    appearedTimestamp = existing?.appearedTimestamp ?: res.appearedTimestamp,
                                    status = if (isTargetHit) "TARGET_HIT" else "ACTIVE"
                                )
                                dao.insertOrUpdate(entity)

                                dayInvest += recPrice
                                dayPnl += pnlAmount
                                if (isTargetHit || pnlPct >= 0) {
                                    winningCount++
                                    if (pnlPct >= 1.5 && topList.size < 3) {
                                        topList.add("${res.ticker.replace(".NS", "")} (+${"%.1f".format(pnlPct)}%)")
                                    }
                                } else {
                                    losingCount++
                                }
                            }

                            val dayPnlPct = if (dayInvest > 0) (dayPnl / dayInvest) * 100 else 0.0
                            val calNow = Calendar.getInstance()
                            val curMins = calNow.get(Calendar.HOUR_OF_DAY) * 60 + calNow.get(Calendar.MINUTE)
                            val is315PM = curMins >= (15 * 60 + 15)

                            val existingLog = dailyLogDao.getLogByDate(currentDateStr)
                            val logEntity = DailyBreakoutLogEntity(
                                id = existingLog?.id ?: 0,
                                date = currentDateStr,
                                timestamp = existingLog?.timestamp ?: System.currentTimeMillis(),
                                totalAmountInvested = dayInvest,
                                totalProfitLossAmount = dayPnl,
                                totalProfitLossPercent = dayPnlPct,
                                tradesCount = fetched.size,
                                winningTrades = winningCount,
                                losingTrades = losingCount,
                                topPerformers = topList.joinToString(", "),
                                isAutoExecutedAt2Percent = true,
                                isFinalBookedAt315 = is315PM || (existingLog?.isFinalBookedAt315 == true)
                            )
                            dailyLogDao.insertOrUpdate(logEntity)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isRefreshing = false
                }
            }
        }
        Unit
    }

    LaunchedEffect(Unit) {
        if (checkIsMarketOpen() && (performances.isEmpty() || dailyLogs.isEmpty())) {
            triggerManualScan()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Market Status Indicator Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isMarketOpen) Color(0xFF10B981).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, if (isMarketOpen) Color(0xFF10B981).copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isMarketOpen) Color(0xFF10B981) else Color(0xFFEF4444))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isMarketOpen) "NSE Market Open • P&L Tracking Active" else "Market Closed • P&L Logging Paused",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isMarketOpen) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isMarketOpen) "Mon-Fri 09:15 AM - 03:30 PM IST" else "Logging will resume on upcoming Monday at 09:15 AM",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Summary Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Breakout Portfolio P&L Engine",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Auto virtual 2% profit target execution • 6-Month DB Archive",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (totalPnlAmount >= 0) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (totalPnlAmount >= 0) "+${"%.2f".format(totalPnlPercent)}%" else "${"%.2f".format(totalPnlPercent)}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (totalPnlAmount >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total Investment", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "₹${"%.2f".format(totalInvestment)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = "Current Value", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "₹${"%.2f".format(totalCurrentValue)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text(text = "2% Target Hits", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "$targetHitCount / ${performances.size}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tab Selector: DAILY LOGS vs WEEKLY P&L vs STOCKS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedTab == "DAILY",
                            onClick = { selectedTab = "DAILY" },
                            label = { Text("Daily Logs (${dailyLogs.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedTab == "WEEKLY",
                            onClick = { selectedTab = "WEEKLY" },
                            label = { Text("Weekly Results", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedTab == "POSITIONS",
                            onClick = { selectedTab = "POSITIONS" },
                            label = { Text("Active Stocks", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        if (selectedTab == "DAILY") {
            if (dailyLogs.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No daily logs recorded yet. Automated engine runs daily at 9:00 AM.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { triggerManualScan() }) {
                                Text("Sync Daily P&L Now", fontSize = 11.sp)
                            }
                        }
                    }
                }
            } else {
                items(dailyLogs, key = { it.id }) { log ->
                    val pnlColor = if (log.totalProfitLossAmount >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                    val pnlSign = if (log.totalProfitLossAmount >= 0) "+" else ""

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Text(text = log.date, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    if (log.isFinalBookedAt315) {
                                        Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFF10B981).copy(alpha = 0.2f)) {
                                            Text("BOOKED 3:15 PM", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981), modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                        }
                                    } else {
                                        Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                            Text("INTRADAY LIVE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "$pnlSign₹${"%.2f".format(log.totalProfitLossAmount)}", fontWeight = FontWeight.Black, fontSize = 13.sp, color = pnlColor)
                                    Text(text = "$pnlSign${"%.2f".format(log.totalProfitLossPercent)}% P&L", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = pnlColor)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Invested: ₹${"%.2f".format(log.totalAmountInvested)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Trades: ${log.tradesCount} (${log.winningTrades} Hits)",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (log.topPerformers.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "Top: ${log.topPerformers}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF10B981))
                            }
                        }
                    }
                }
            }
        } else if (selectedTab == "WEEKLY") {
            if (weeklyPnlList.isEmpty()) {
                item {
                    Text("Weekly breakdown will update as daily logs accumulate.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
                }
            } else {
                items(weeklyPnlList) { (weekName, weekPnlAmount, meta) ->
                    val (weekPnlPercent, tradeMeta) = meta
                    val (wins, total) = tradeMeta
                    val pnlColor = if (weekPnlAmount >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                    val pnlSign = if (weekPnlAmount >= 0) "+" else ""

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = weekName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "Winning Trades: $wins / $total", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "$pnlSign₹${"%.2f".format(weekPnlAmount)}", fontWeight = FontWeight.Black, fontSize = 14.sp, color = pnlColor)
                                Text(text = "$pnlSign${"%.2f".format(weekPnlPercent)}%", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = pnlColor)
                            }
                        }
                    }
                }
            }
        } else {
            // POSITIONS
            if (performances.isEmpty()) {
                item {
                    Text("No active breakout positions recorded.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
                }
            } else {
                items(performances, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = item.ticker.replace(".NS", ""),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (item.isTargetHit) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFF10B981).copy(alpha = 0.2f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Target Hit",
                                                        tint = Color(0xFF10B981),
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                    Text(
                                                        text = "TARGET HIT (2%)",
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color(0xFF10B981)
                                                    )
                                                }
                                            }
                                        } else {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFF38BDF8).copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    text = "ACTIVE",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF38BDF8),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = item.companyName,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    val pnlColor = if (item.pnlAmount >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
                                    val pnlSign = if (item.pnlAmount >= 0) "+" else ""
                                    Text(
                                        text = "$pnlSign₹${"%.2f".format(item.pnlAmount)}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = pnlColor
                                    )
                                    Text(
                                        text = "$pnlSign${"%.2f".format(item.pnlPercent)}%",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = pnlColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Buy: ₹${"%.2f".format(item.recommendedPrice)} • CMP: ₹${"%.2f".format(item.currentPrice)}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Date: ${item.appearedDate}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Ads Banner at the end of Breakout P&L tab page
        item {
            Spacer(modifier = Modifier.height(10.dp))
            AdBannerView()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MarketScreen(modifier: Modifier = Modifier) {
    var isRefreshingVideos by remember { mutableStateOf(false) }
    var refreshToastMessage by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var removedVideoIds by remember { mutableStateOf(setOf<String>()) }
    var lastRefreshedTime by remember { mutableStateOf("Just now") }
    
    val fiveDaysAgo = System.currentTimeMillis() - (5L * 24 * 60 * 60 * 1000L)
    val initialCached = remember {
        VideoCache.cachedVideos
            .filter { it.publishedTimestamp == 0L || it.publishedTimestamp >= fiveDaysAgo }
            .sortedByDescending { it.publishedTimestamp }
    }
    var liveVideos by remember { mutableStateOf<List<VideoItem>>(initialCached) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Fetch dynamic videos from YouTube channels (Dhan, Groww, Zee Biz, CNBC Awaaz, ET Now Swadesh) in parallel
    fun refreshLiveFeeds() {
        coroutineScope.launch {
            isRefreshingVideos = true
            if (liveVideos.isEmpty()) {
                refreshToastMessage = "Fetching latest YouTube videos from top financial channels..."
            }
            try {
                val fetched = withContext(Dispatchers.IO) {
                    coroutineScope {
                        listOf(
                            async { fetchYouTubeChannelVideos("UCEzHCpvFWoF85UabbzKTkOQ", "Dhan", "DHAN LATEST", Color(0xFF5B21B6)) },
                            async { fetchYouTubeChannelVideos("UCw5TLrz3qADabwezTEcOmgQ", "Groww", "GROWW LATEST", Color(0xFF00D09C)) },
                            async { fetchYouTubeChannelVideos("UCkXopQ3ubd-rnXnStZqCl2w", "Zee Business", "ZEE BIZ LATEST", Color(0xFFDC2626)) },
                            async { fetchYouTubeChannelVideos("UCQIycDaLsBpMKjOCeaKUYVg", "CNBC Awaaz", "CNBC AWAAZ", Color(0xFF0284C7)) },
                            async { fetchYouTubeChannelVideos("UCD3CdwT8lTCe5ZGHbUBxmWA", "ET Now Swadesh", "ET NOW SWADESH", Color(0xFFD97706)) }
                        ).awaitAll().flatten()
                            .filter { it.publishedTimestamp == 0L || it.publishedTimestamp >= (System.currentTimeMillis() - 5L * 24 * 60 * 60 * 1000L) }
                            .distinctBy { it.videoId }
                            .sortedByDescending { it.publishedTimestamp }
                    }
                }
                if (fetched.isNotEmpty()) {
                    liveVideos = fetched
                    VideoCache.cachedVideos = fetched
                    val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                    lastRefreshedTime = "Refreshed at $currentTime"
                    refreshToastMessage = "Loaded ${fetched.size} live videos from top 5 channels!"
                } else if (liveVideos.isEmpty()) {
                    refreshToastMessage = "No live video feeds found. Tap refresh to retry."
                }
            } catch (e: Exception) {
                if (liveVideos.isEmpty()) {
                    refreshToastMessage = "Unable to refresh feeds: ${e.message}"
                }
            } finally {
                isRefreshingVideos = false
                delay(3000)
                refreshToastMessage = null
            }
        }
    }

    // Automatic Initial & 30-Minute Refresh Loop
    LaunchedEffect(Unit) {
        refreshLiveFeeds()
        while (isActive) {
            delay(30 * 60 * 1000L) // Refresh every 30 minutes
            refreshLiveFeeds()
        }
    }

    val categories = listOf("ALL", "DHAN", "GROWW", "ZEE BIZ", "CNBC AWAAZ", "ET NOW")

    val visibleVideos = remember(selectedCategory, removedVideoIds, liveVideos) {
        liveVideos.filter { video ->
            video.id !in removedVideoIds &&
            when (selectedCategory) {
                "ALL" -> true
                "DHAN" -> video.channel.contains("Dhan", ignoreCase = true)
                "GROWW" -> video.channel.contains("Groww", ignoreCase = true)
                "ZEE BIZ" -> video.channel.contains("Zee", ignoreCase = true)
                "CNBC AWAAZ" -> video.channel.contains("CNBC", ignoreCase = true) || video.channel.contains("Awaaz", ignoreCase = true)
                "ET NOW" -> video.channel.contains("ET Now", ignoreCase = true) || video.channel.contains("Swadesh", ignoreCase = true)
                else -> true
            }
        }
    }

    fun openYouTubeDirectly(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            refreshToastMessage = "Could not open YouTube link"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AnimatedHeaderIcon(
                            icon = Icons.Default.PlayArrow,
                            backgroundColor = Color(0xFF10B981),
                            shape = RoundedCornerShape(12.dp),
                            useSurface = true
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            AnimatedHeadingText(
                                text = "Live Market Video Feeds",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Feeds: Dhan, Groww, Zee Biz, CNBC Awaaz & ET Now",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = {
                                if (!isRefreshingVideos) {
                                    removedVideoIds = emptySet()
                                    refreshLiveFeeds()
                                }
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            if (isRefreshingVideos) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Videos",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = refreshToastMessage != null, enter = fadeIn(), exit = fadeOut()) {
                        Surface(
                            color = Color(0xFF10B981).copy(alpha = 0.25f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = refreshToastMessage ?: "",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }
        }

        // Filter Bar (Category Chips)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentPadding = PaddingValues(end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = {
                        Text(
                            text = category,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        if (visibleVideos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = "No Videos",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No available videos found in this category.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Button(
                        onClick = {
                            selectedCategory = "ALL"
                            removedVideoIds = emptySet()
                        }
                    ) {
                        Text("Reset Video Filter")
                    }
                }
            }
        } else {
            // Video Feed List (Small Thumbnail Compact Cards fetched from YouTube)
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(visibleVideos, key = { it.id }) { video ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { openYouTubeDirectly(video.directUrl) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Small Thumbnail Container (YouTube Image)
                            Box(
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(80.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0F172A))
                                    .clickable { openYouTubeDirectly(video.directUrl) }
                            ) {
                                val ctx = LocalContext.current
                                val primaryThumbnailUrl = remember(video.videoId) { "https://i.ytimg.com/vi/${video.videoId}/hqdefault.jpg" }
                                val fallbackThumbnailUrl = remember(video.videoId) { "https://img.youtube.com/vi/${video.videoId}/hqdefault.jpg" }

                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(ctx)
                                        .data(primaryThumbnailUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = video.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    loading = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color(0xFF0F172A)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                color = Color(0xFF38BDF8),
                                                strokeWidth = 2.dp
                                            )
                                        }
                                    },
                                    error = {
                                        SubcomposeAsyncImage(
                                            model = ImageRequest.Builder(ctx)
                                                .data(fallbackThumbnailUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = video.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                            error = {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(
                                                            Brush.linearGradient(
                                                                colors = listOf(video.tagBgColor.copy(alpha = 0.8f), Color(0xFF0F172A))
                                                            )
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = video.channel,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        )
                                    }
                                )

                                // Red Play Button Center Overlay
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFEF4444),
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(28.dp),
                                    shadowElevation = 4.dp
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Play Embedded Video",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .fillMaxSize()
                                    )
                                }

                                // Live or Duration Badge
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.Black.copy(alpha = 0.8f),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        if (video.isLive) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFEF4444))
                                            )
                                            Text(
                                                text = "LIVE",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFFEF4444)
                                            )
                                        } else {
                                            Text(
                                                text = video.timeAgo,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Text Details & External Actions Column
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                // Tag Badge
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = video.tagBgColor.copy(alpha = 0.15f),
                                    border = BorderStroke(0.5.dp, video.tagBgColor.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = video.tag,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = video.tagBgColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = video.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 16.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = video.channel,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        IconButton(
                                            onClick = { openYouTubeDirectly(video.directUrl) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.OpenInNew,
                                                contentDescription = "Watch on YouTube",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                val sendIntent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(
                                                        Intent.EXTRA_TEXT,
                                                        "Watch '${video.title}' on YouTube: ${video.directUrl}"
                                                    )
                                                    type = "text/plain"
                                                }
                                                context.startActivity(Intent.createChooser(sendIntent, "Share Video"))
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Share Video",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        // Hide/Remove button for broken/unavailable items
                                        IconButton(
                                            onClick = {
                                                removedVideoIds = removedVideoIds + video.id
                                                refreshToastMessage = "Removed video from feed"
                                                coroutineScope.launch {
                                                    delay(2000)
                                                    refreshToastMessage = null
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.VisibilityOff,
                                                contentDescription = "Remove video",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    AdBannerView()
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}



