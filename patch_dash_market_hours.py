with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

target_indices_polling = """    // Live Indices updates
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
    }"""

replacement_indices_polling = """    // Live Indices updates (Only polls during live market hours: Mon-Fri 9:15 AM - 3:30 PM IST)
    LaunchedEffect(Unit) {
        while (isActive) {
            if (MarketUtils.isMarketOpen()) {
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
            }
            delay(10000)
        }
    }"""

target_cmp_polling = """    // Live CMP Refresh for Top 15 Breakout Stocks
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
                                val prevClose = meta?.previousClose ?: item.previousClose ?: livePrice
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
    }"""

replacement_cmp_polling = """    // Live CMP Refresh for Top 15 Breakout Stocks (Only polls during live market hours)
    LaunchedEffect(scanResults.size) {
        if (scanResults.isEmpty()) return@LaunchedEffect
        while (isActive) {
            delay(4000)
            if (MarketUtils.isMarketOpen()) {
                try {
                    val updatedList = withContext(Dispatchers.IO) {
                        scanResults.map { item ->
                            async {
                                try {
                                    val res = YahooRetrofit.service.getChart(item.ticker, "1d", "1m")
                                    val meta = res.chart?.result?.firstOrNull()?.meta
                                    val livePrice = meta?.regularMarketPrice ?: item.price
                                    val prevClose = meta?.previousClose ?: item.previousClose ?: livePrice
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
    }"""

content = content.replace(target_indices_polling, replacement_indices_polling)
content = content.replace(target_cmp_polling, replacement_cmp_polling)

# Add Market status badge under title
sub_title = """                    Text("Live Technical Signals (RSI, MACD, Volume)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)"""
sub_title_badge = """                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val isOpen = MarketUtils.isMarketOpen()
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isOpen) Color(0xFF10B981) else Color.Gray)
                        )
                        Text(
                            text = if (isOpen) "Live Market Polling Active" else "Market Closed (Polls Paused)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOpen) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }"""

content = content.replace(sub_title, sub_title_badge)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

print("Updated MainActivity market hours polling & status indicator!")
