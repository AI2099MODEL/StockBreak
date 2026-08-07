import re

with open('app/src/main/java/com/example/LiveScreen.kt', 'r') as f:
    content = f.read()

replacement = """        while (isActive) {
            for (i in stocks.indices) {
                try {
                    val stock = stocks[i]
                    val response = YahooRetrofit.service.getChart(stock.symbol, "1d", "1m")
                    val result = response.chart?.result?.firstOrNull()
                    val price = result?.meta?.regularMarketPrice ?: continue
                    val previousClose = result.meta.previousClose ?: price
                    val fetchedName = result.meta.shortName ?: result.meta.longName ?: stock.symbol
                    
                    val quote = result.indicators?.quote?.firstOrNull()
                    val closes = quote?.close?.filterNotNull() ?: emptyList()
                    
                    val updatedStock = stock.copy(
                        name = fetchedName,
                        price = price,
                        change = price - previousClose,
                        history = closes.takeLast(60).toMutableList(), // last 60 minutes
                        isBullish = price >= previousClose
                    )
                    
                    if (updatedStock.targetPrice != null && !updatedStock.isTargetTriggered) {
                        val target = updatedStock.targetPrice!!
                        val crossedUp = stock.price < target && price >= target
                        val crossedDown = stock.price > target && price <= target
                        if (stock.price != 0.0 && (crossedUp || crossedDown)) {
                            updatedStock.isTargetTriggered = true
                            triggeredAlert = updatedStock
                        }
                    }
                    stocks[i] = updatedStock
                } catch (e: Exception) {
                    // Ignore errors and retry next tick
                }
            }
            isLoading = false
            tickTrigger++
            delay(5000) // update every 5 seconds
        }"""

pattern = re.compile(r'while\s*\(isActive\)\s*\{.*?delay\(5000\) // update every 5 seconds\s*\}', re.MULTILINE | re.DOTALL)
content = pattern.sub(replacement, content)

with open('app/src/main/java/com/example/LiveScreen.kt', 'w') as f:
    f.write(content)
