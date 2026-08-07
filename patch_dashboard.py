import re

new_dashboard = '''@Composable
fun DashboardScreen(modifier: Modifier = Modifier, onSymbolSelected: (String) -> Unit = {}) {
    var scanResults by remember { mutableStateOf<List<ScanResult>>(emptyList()) }
    var isScanning by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    var niftyPrice by remember { mutableStateOf("24,850.40") }
    var niftyChange by remember { mutableStateOf("+1.77%") }
    var niftyIsPositive by remember { mutableStateOf(true) }

    var sensexPrice by remember { mutableStateOf("81,200.15") }
    var sensexChange by remember { mutableStateOf("+1.85%") }
    var sensexIsPositive by remember { mutableStateOf(true) }

    // Live Indices updates
    LaunchedEffect(Unit) {
        while (isActive) {
            try {
                withContext(Dispatchers.IO) {
                    try {
                        val resNifty = YahooRetrofit.service.getChart("^NSEI", "1d", "1m")
                        val meta = resNifty.chart?.result?.firstOrNull()?.meta
                        val p = meta?.regularMarketPrice
                        val prev = meta?.previousClose
                        if (p != null && prev != null && prev > 0) {
                            val chg = p - prev
                            val pct = (chg / prev) * 100
                            niftyPrice = String.format("%,.2f", p)
                            niftyChange = String.format("%+.2f%%", pct)
                            niftyIsPositive = chg >= 0
                        }
                    } catch (e: Exception) {}

                    try {
                        val resSensex = YahooRetrofit.service.getChart("^BSESN", "1d", "1m")
                        val meta = resSensex.chart?.result?.firstOrNull()?.meta
                        val p = meta?.regularMarketPrice
                        val prev = meta?.previousClose
                        if (p != null && prev != null && prev > 0) {
                            val chg = p - prev
                            val pct = (chg / prev) * 100
                            sensexPrice = String.format("%,.2f", p)
                            sensexChange = String.format("%+.2f%%", pct)
                            sensexIsPositive = chg >= 0
                        }
                    } catch (e: Exception) {}
                }
            } catch (e: Exception) {}
            delay(10000)
        }
    }

    // Initial Auto-scan
    LaunchedEffect(Unit) {
        try {
            isScanning = true
            scanResults = StockScanner.scanMultiple("Breakouts")
        } finally {
            isScanning = false
        }
    }

    // Live CMP Refresh for Top 15 Breakout Stocks
    LaunchedEffect(scanResults.size) {
        if (scanResults.isEmpty()) return@LaunchedEffect
        while (isActive) {
            delay(4000)
            try {
                val updatedList = withContext(Dispatchers.IO) {
                    scanResults.map { item ->
                        async {
                            try {
                                val res = YahooRetrofit.service.getChart(item.ticker, "1d", "1m")
                                val meta = res.chart?.result?.firstOrNull()?.meta
                                val livePrice = meta?.regularMarketPrice ?: item.price
                                val prevClose = meta?.previousClose ?: item.previousClose
                                val change = livePrice - prevClose
                                val changePercent = if (prevClose > 0) (change / prevClose) * 100 else 0.0
                                item.copy(
                                    price = livePrice,
                                    change = change,
                                    changePercent = changePercent
                                )
                            } catch (e: Exception) {
                                item
                            }
                        }
                    }.awaitAll()
                }
                scanResults = updatedList
            } catch (e: Exception) {}
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with Indices
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Investify", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                val themeMode = LocalThemeMode.current
                IconButton(onClick = { themeMode.value = !themeMode.value }) {
                    Icon(
                        imageVector = if (themeMode.value) androidx.compose.material.icons.Icons.Default.LightMode else androidx.compose.material.icons.Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("NIFTY 50", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(niftyPrice, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                    Text(niftyChange, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (niftyIsPositive) Color(0xFF10B981) else Color(0xFFEF4444))
                }
                Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.outlineVariant))
                Column(horizontalAlignment = Alignment.End) {
                    Text("SENSEX", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(sensexPrice, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                    Text(sensexChange, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (sensexIsPositive) Color(0xFF10B981) else Color(0xFFEF4444))
                }
            }
        }

        // Main Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header for Breakout Stocks
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Top 15 Breakouts", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
                    Text("Live Technical Signals (RSI, MACD, Volume)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                IconButton(
                    onClick = {
                        if (!isScanning) {
                            isScanning = true
                            coroutineScope.launch {
                                try {
                                    scanResults = StockScanner.scanMultiple("Breakouts")
                                } finally {
                                    isScanning = false
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimaryContainer, strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Refresh,
                            contentDescription = "Scan Breakouts",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (isScanning && scanResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Scanning NIFTY 200 for breakout signals...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else if (scanResults.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(vertical = 12.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Share", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(0.26f))
                            Text("CMP", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(0.18f), textAlign = TextAlign.End)
                            Text("T1", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(0.18f), textAlign = TextAlign.End)
                            Text("T2", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(0.18f), textAlign = TextAlign.End)
                            Text("SL", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(0.20f), textAlign = TextAlign.End)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        scanResults.forEachIndexed { index, res ->
                            RecommendationTableRow(res, index, onSymbolSelected)
                            if (index < scanResults.lastIndex) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}'''

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Replace DashboardScreen block
pattern = r'@Composable\nfun DashboardScreen\(.*?\n}\n}'
match = re.search(pattern, content, re.DOTALL)
if match:
    content = content[:match.start()] + new_dashboard + content[match.end():]
    print("Replaced DashboardScreen")
else:
    print("DashboardScreen pattern match failed")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
