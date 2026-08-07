import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Let's see what is currently at the end
# The file ends with:
#             OutlinedTextField(
#                 value = searchTicker,
#                 onValueChange = { searchTicker = it.uppercase() },
#                 modifier = Modifier.weight(1f),
#                 placeholder = { Text("e.g. RELIANCE") },
#                 singleLine = true
#             )

missing_code = """
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
        
        androidx.compose.foundation.lazy.LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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

@Composable
fun ConfigScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("InvestifyPrefs", android.content.Context.MODE_PRIVATE)
    
    var activeProvider by remember { mutableStateOf(sharedPrefs.getString("active_provider", "Angel One") ?: "Angel One") }
    
    // Angel One
    var angelClientCode by remember { mutableStateOf(sharedPrefs.getString("angel_client_code", "") ?: "") }
    var angelApiKey by remember { mutableStateOf(sharedPrefs.getString("angel_api_key", "") ?: "") }
    
    // Fyers
    var fyersAppId by remember { mutableStateOf(sharedPrefs.getString("fyers_app_id", "") ?: "") }
    var fyersToken by remember { mutableStateOf(sharedPrefs.getString("fyers_token", "") ?: "") }
    
    // Dhan
    var dhanClientId by remember { mutableStateOf(sharedPrefs.getString("dhan_client_id", "") ?: "") }
    var dhanToken by remember { mutableStateOf(sharedPrefs.getString("dhan_token", "") ?: "") }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Broker Configuration", fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
        Text("Select and configure your broker API for live CMP and Indices.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text("Active Data Provider", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("Angel One", "Fyers", "Dhan").forEach { provider ->
                val selected = activeProvider == provider
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f).clickable { 
                        activeProvider = provider 
                        sharedPrefs.edit().putString("active_provider", provider).apply()
                    }
                ) {
                    Text(provider, textAlign = TextAlign.Center, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (activeProvider == "Angel One") {
            OutlinedTextField(
                value = angelClientCode,
                onValueChange = { angelClientCode = it; sharedPrefs.edit().putString("angel_client_code", it).apply() },
                label = { Text("SmartAPI Client Code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = angelApiKey,
                onValueChange = { angelApiKey = it; sharedPrefs.edit().putString("angel_api_key", it).apply() },
                label = { Text("SmartAPI Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        } else if (activeProvider == "Fyers") {
            OutlinedTextField(
                value = fyersAppId,
                onValueChange = { fyersAppId = it; sharedPrefs.edit().putString("fyers_app_id", it).apply() },
                label = { Text("Fyers App ID (Client ID)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = fyersToken,
                onValueChange = { fyersToken = it; sharedPrefs.edit().putString("fyers_token", it).apply() },
                label = { Text("Fyers Access Token") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        } else if (activeProvider == "Dhan") {
            OutlinedTextField(
                value = dhanClientId,
                onValueChange = { dhanClientId = it; sharedPrefs.edit().putString("dhan_client_id", it).apply() },
                label = { Text("Dhan Client ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = dhanToken,
                onValueChange = { dhanToken = it; sharedPrefs.edit().putString("dhan_token", it).apply() },
                label = { Text("Dhan Access Token") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Security Notice", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Your API keys are stored locally on your device in SharedPreferences. Do not share your API keys.", fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
"""

with open('app/src/main/java/com/example/MainActivity.kt', 'a') as f:
    f.write(missing_code)
