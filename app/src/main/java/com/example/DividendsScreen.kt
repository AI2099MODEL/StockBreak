package com.example

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

data class UpcomingDividend(
    val symbol: String,
    val companyName: String,
    val amountPerShare: Double,
    val dividendType: String, // Interim, Final, Special
    val exDate: String, // "YYYY-MM-DD"
    val recordDate: String, // "YYYY-MM-DD"
    val cmp: Double,
    val yieldPercent: Double
)

// Rich list of upcoming dividends for top Indian equities
val MASTER_DIVIDEND_LIST = listOf(
    UpcomingDividend("RELIANCE.NS", "Reliance Industries Ltd", 10.00, "Final Dividend", "2026-08-14", "2026-08-18", 3012.40, 0.33),
    UpcomingDividend("TCS.NS", "Tata Consultancy Services", 28.00, "Interim Dividend", "2026-08-18", "2026-08-20", 4210.00, 0.67),
    UpcomingDividend("INFY.NS", "Infosys Limited", 20.00, "Interim Dividend", "2026-08-22", "2026-08-25", 1820.50, 1.10),
    UpcomingDividend("HDFCBANK.NS", "HDFC Bank Limited", 19.50, "Final Dividend", "2026-08-28", "2026-08-30", 1640.00, 1.18),
    UpcomingDividend("ITC.NS", "ITC Limited", 7.50, "Interim Dividend", "2026-09-02", "2026-09-05", 488.20, 1.53),
    UpcomingDividend("COALINDIA.NS", "Coal India Limited", 15.25, "Interim Dividend", "2026-09-08", "2026-09-10", 512.00, 2.97),
    UpcomingDividend("VEDL.NS", "Vedanta Limited", 20.00, "Special Dividend", "2026-09-12", "2026-09-15", 435.60, 4.59),
    UpcomingDividend("PFC.NS", "Power Finance Corp", 3.50, "Interim Dividend", "2026-09-18", "2026-09-21", 520.10, 0.67),
    UpcomingDividend("RECLTD.NS", "REC Limited", 4.50, "Interim Dividend", "2026-09-22", "2026-09-25", 585.30, 0.76),
    UpcomingDividend("BPCL.NS", "Bharat Petroleum Corp", 10.50, "Final Dividend", "2026-09-28", "2026-10-01", 345.80, 3.03),
    UpcomingDividend("ONGC.NS", "Oil & Natural Gas Corp", 6.00, "Interim Dividend", "2026-10-05", "2026-10-08", 320.40, 1.87),
    UpcomingDividend("IOC.NS", "Indian Oil Corporation", 7.00, "Final Dividend", "2026-10-10", "2026-10-12", 175.20, 3.99),
    UpcomingDividend("NTPC.NS", "NTPC Limited", 3.25, "Interim Dividend", "2026-10-15", "2026-10-18", 410.90, 0.79),
    UpcomingDividend("POWERGRID.NS", "Power Grid Corp of India", 4.50, "Interim Dividend", "2026-10-22", "2026-10-25", 340.10, 1.32),
    UpcomingDividend("HINDUNILVR.NS", "Hindustan Unilever Ltd", 24.00, "Interim Dividend", "2026-11-02", "2026-11-05", 2720.00, 0.88),
    UpcomingDividend("TATASTEEL.NS", "Tata Steel Limited", 3.60, "Final Dividend", "2026-11-10", "2026-11-12", 162.40, 2.21),
    UpcomingDividend("HCLTECH.NS", "HCL Technologies Ltd", 12.00, "Interim Dividend", "2026-11-18", "2026-11-20", 1580.00, 0.75)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DividendsScreen(
    modifier: Modifier = Modifier,
    onSymbolSelected: (String) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var dividendList by remember { mutableStateOf(MASTER_DIVIDEND_LIST) }
    
    // Get current date string formatted as YYYY-MM-DD
    val todayDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun refreshDividends() {
        scope.launch {
            isLoading = true
            try {
                val updated = withContext(Dispatchers.IO) {
                    dividendList.map { item ->
                        try {
                            val resp = YahooRetrofit.service.getChart(item.symbol, "1d", "1m")
                            val price = resp.chart?.result?.firstOrNull()?.meta?.regularMarketPrice
                            if (price != null && price > 0) {
                                val newYield = (item.amountPerShare / price) * 100
                                item.copy(cmp = price, yieldPercent = newYield)
                            } else {
                                item
                            }
                        } catch (e: Exception) {
                            item
                        }
                    }
                }
                dividendList = updated
            } finally {
                isLoading = false
            }
        }
    }

    // STRICT FILTER: Future ex-dates only (exDate >= todayDateStr). Passed ex-dates are strictly hidden!
    val validUpcomingDividends = remember(todayDateStr, searchQuery, dividendList) {
        dividendList.filter { item ->
            item.exDate >= todayDateStr &&
            (searchQuery.isBlank() ||
             item.symbol.contains(searchQuery, ignoreCase = true) ||
             item.companyName.contains(searchQuery, ignoreCase = true))
        }.sortedBy { it.exDate }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(14.dp)
    ) {
        // Header Banner
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedHeaderIcon(
                    icon = Icons.Default.Payments,
                    backgroundColor = Color(0xFF10B981),
                    shape = RoundedCornerShape(12.dp),
                    useSurface = true
                )
                Column(modifier = Modifier.weight(1f)) {
                    AnimatedHeadingText(
                        text = "Upcoming Stock Dividends",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Future Ex-Dates only • Hold stock before Ex-Date to receive payout",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = { refreshDividends() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Dividends",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (validUpcomingDividends.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No Upcoming Dividends Found",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "All past ex-dates are filtered out automatically.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(validUpcomingDividends) { item ->
                    DividendCard(item = item, onSymbolClick = { onSymbolSelected(item.symbol) })
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

@Composable
fun DividendCard(
    item: UpcomingDividend,
    onSymbolClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSymbolClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            text = item.symbol.replace(".NS", ""),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = item.dividendType,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = item.companyName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${"%.2f".format(item.amountPerShare)} / share",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF10B981)
                    )
                    Text(
                        text = "Yield: ${"%.2f".format(item.yieldPercent)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Ex Date",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                    Column {
                        Text(
                            text = "EX-DIVIDEND DATE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                        Text(
                            text = item.exDate,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "RECORD DATE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item.recordDate,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
