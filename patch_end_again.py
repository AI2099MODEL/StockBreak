import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

replacement = """
            OutlinedTextField(
                value = searchTicker,
                onValueChange = { searchTicker = it.uppercase() },
                modifier = Modifier.weight(1f),
                placeholder = { Text("e.g. RELIANCE") },
                singleLine = true
            )
            Button(
                onClick = {
                    if (searchTicker.isNotBlank() && !isSearching) {
                        isSearching = true
                        searchError = null
                        searchResultIntraday = null
                        searchResultWeekly = null
                        searchResultMonthly = null
                        coroutineScope.launch {
                            val ticker = if (searchTicker.endsWith(".NS") || searchTicker.endsWith(".BSE")) searchTicker else "$searchTicker.NS"
                            
                            val intraday = async { StockScanner.analyzeStock(ticker, "Intraday", requireBullish = false) }
                            val weekly = async { StockScanner.analyzeStock(ticker, "Weekly", requireBullish = false) }
                            val monthly = async { StockScanner.analyzeStock(ticker, "Monthly", requireBullish = false) }
                            
                            searchResultIntraday = intraday.await()
                            searchResultWeekly = weekly.await()
                            searchResultMonthly = monthly.await()
                            
                            if (searchResultIntraday == null && searchResultWeekly == null && searchResultMonthly == null) {
                                searchError = "Could not find data for $ticker. Please check the ticker symbol."
                            }
                            isSearching = false
                        }
                    }
                },
                modifier = Modifier.height(56.dp)
            ) {
                if (isSearching) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (searchError != null) {
            Text(searchError!!, color = MaterialTheme.colorScheme.error)
        }
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            searchResultIntraday?.let { res ->
                item {
                    TimeframeAnalysisCard("Intraday (15m)", res)
                }
            }
            searchResultWeekly?.let { res ->
                item {
                    TimeframeAnalysisCard("Weekly (Daily Chart)", res)
                }
            }
            searchResultMonthly?.let { res ->
                item {
                    TimeframeAnalysisCard("Monthly (Daily Chart, Long-term)", res)
                }
            }
        }
    }
}

@Composable
fun MiniPriceChart(prices: List<Double>, modifier: Modifier = Modifier, lineColor: Color) {
    if (prices.isEmpty()) return
    val pts = prices.takeLast(60) // Last 60 periods
    val maxPrice = pts.maxOrNull() ?: 1.0
    val minPrice = pts.minOrNull() ?: 0.0
    
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val priceRange = maxPrice - minPrice
        val stepX = width / (pts.size - 1).coerceAtLeast(1)
        
        val path = androidx.compose.ui.graphics.Path()
        pts.forEachIndexed { index, price ->
            val x = index * stepX
            val y = height - if (priceRange == 0.0) height / 2 else ((price - minPrice) / priceRange).toFloat() * height
            if (index == 0) {
                path.moveTo(x, y.toFloat())
            } else {
                path.lineTo(x, y.toFloat())
            }
        }
        drawPath(
            path = path,
            color = lineColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
        )
    }
}

@Composable
fun TimeframeAnalysisCard(timeframe: String, res: ScanResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val color = when(res.signalStrength) {
                "STRONG BUY", "BUY" -> Color(0xFF10B981)
                "SELL", "WEAK/SELL" -> Color(0xFFEF4444)
                else -> Color(0xFFF59E0B)
            }
            val stars = when(res.signalStrength) {
                "STRONG BUY" -> "⭐⭐⭐⭐⭐"
                "BUY" -> "⭐⭐⭐⭐"
                "MILD BUY" -> "⭐⭐⭐"
                "HOLD" -> "⭐⭐"
                "SELL", "WEAK/SELL" -> "⭐"
                else -> "⭐⭐"
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(timeframe, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)
                Text(stars, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Current Price: ₹${res.price}", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            
            if (res.historicalPrices.isNotEmpty()) {
                MiniPriceChart(
                    prices = res.historicalPrices,
                    modifier = Modifier.fillMaxWidth().height(100.dp).padding(vertical = 8.dp),
                    lineColor = color
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Text("Targets & Stop Loss", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text("🎯 Target 1: ${res.target1?.let { "₹%.2f".format(it) } ?: "N/A"}", fontSize = 14.sp)
            Text("🎯 Target 2: ${res.target2?.let { "₹%.2f".format(it) } ?: "N/A"}", fontSize = 14.sp)
            Text("🛑 Stop Loss: ${res.stopLoss?.let { "₹%.2f".format(it) } ?: "N/A"}", fontSize = 14.sp)
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Technical Indicators", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(res.strategies, fontSize = 14.sp)
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Analysis Insights", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(res.reasons, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
fun PortfolioScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Portfolio - Coming Soon", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
"""

cut_off = """            OutlinedTextField(
                value = searchTicker,
                onValueChange = { searchTicker = it.uppercase() },
                modifier = Modifier.weight(1f),
                placeholder = { Text("e.g. RELIANCE") },
                singleLine = true
            )"""

if cut_off in content:
    content = content.replace(cut_off, replacement)
    with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
        f.write(content)
else:
    print("Not found")

