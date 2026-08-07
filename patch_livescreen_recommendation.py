import re

with open('app/src/main/java/com/example/LiveScreen.kt', 'r') as f:
    content = f.read()

# 1. Add states and effect
state_injection = """    var tickTrigger by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    
    var activeScanResult by remember { mutableStateOf<ScanResult?>(null) }
    var isScanningActive by remember { mutableStateOf(false) }
    
    LaunchedEffect(activeSymbol) {
        if (activeSymbol.isNotEmpty()) {
            isScanningActive = true
            activeScanResult = null
            activeScanResult = StockScanner.analyzeStock(activeSymbol, "Intraday", requireBullish = false)
            isScanningActive = false
        }
    }"""
content = content.replace(
    '    var tickTrigger by remember { mutableIntStateOf(0) }\n    var isLoading by remember { mutableStateOf(true) }',
    state_injection
)

# 2. Add recommendation badge in Chart Panel
chart_panel_injection = """                    LiveStockChart(history = activeStock.history, modifier = Modifier.fillMaxWidth().height(160.dp), tickTrigger = tickTrigger)
                    
                    if (isScanningActive) {
                        Text("Analyzing trend...", fontSize = 12.sp, color = Color(0xFF64748B), modifier = Modifier.padding(top = 12.dp))
                    } else if (activeScanResult != null) {
                        val res = activeScanResult!!
                        val color = when(res.signalStrength) {
                            "STRONG BUY", "BUY" -> Color(0xFF10B981)
                            "SELL", "WEAK/SELL" -> Color(0xFFEF4444)
                            else -> Color(0xFFF59E0B)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.15f)) {
                                    Text(res.signalStrength, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                                Text("Score: ${res.score}", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                            }
                            
                            if (res.target1 != null) {
                                Text("🎯 Tgt: ₹${Math.round(res.target1)}  🛑 SL: ₹${res.stopLoss?.let { Math.round(it) } ?: "N/A"}", fontSize = 11.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Medium)
                            }
                        }
                    }"""

content = content.replace(
    '                    LiveStockChart(history = activeStock.history, modifier = Modifier.fillMaxWidth().height(160.dp), tickTrigger = tickTrigger)',
    chart_panel_injection
)

with open('app/src/main/java/com/example/LiveScreen.kt', 'w') as f:
    f.write(content)
