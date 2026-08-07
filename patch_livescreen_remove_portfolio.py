import re

with open('app/src/main/java/com/example/LiveScreen.kt', 'r') as f:
    content = f.read()

# 1. Initialize stocks to empty
content = content.replace(
    """    val stocks = remember {
        mutableStateListOf(
            LiveStock("RELIANCE.NS", "Reliance Industries Ltd."),
            LiveStock("TCS.NS", "Tata Consultancy Services"),
            LiveStock("HDFCBANK.NS", "HDFC Bank Limited"),
            LiveStock("INFY.NS", "Infosys Limited"),
            LiveStock("ICICIBANK.NS", "ICICI Bank Limited")
        )
    }""",
    """    val stocks = remember { mutableStateListOf<LiveStock>() }"""
)

# 2. Initialize activeSymbol to empty
content = content.replace(
    """    var activeSymbol by remember { mutableStateOf("RELIANCE.NS") }""",
    """    var activeSymbol by remember { mutableStateOf("") }"""
)

# 3. Change activeStock and remove totalBalance calculations
content = content.replace(
    """    val activeStock = stocks.find { it.symbol == activeSymbol } ?: stocks.first()
    var totalBalance = 0.0
    var totalChange = 0.0
    var totalInvested = 0.0
    stocks.forEach { 
        val qty = 100
        totalBalance += it.price * qty 
        totalChange += it.change * qty
        totalInvested += (it.price - it.change) * qty
    }
    val totalChangePercent = if (totalInvested > 0) (totalChange / totalInvested) * 100 else 0.0""",
    """    val activeStock = stocks.find { it.symbol == activeSymbol } ?: stocks.firstOrNull()"""
)

# 4. Remove Portfolio Metrics Section
portfolio_regex = re.compile(r'        // Portfolio Metrics.*?        Text\("LIVE TICKER CHART \(TODAY\)"', re.DOTALL)
content = portfolio_regex.sub('        Text("LIVE TICKER CHART (TODAY)"', content)

# 5. Fix Chart Panel rendering if activeStock is null
chart_panel_regex = re.compile(r'        // Chart Panel.*?        Surface\((.*?)\) \{\n            Column\(modifier = Modifier\.padding\(16\.dp\)\) \{\n                Row(.*?) \{\n                    Text\(activeStock\.symbol\.replace\("\.NS", ""\), fontSize = 18\.sp, fontWeight = FontWeight\.ExtraBold, color = Color\(0xFF0F172A\)\)\n                    if \(isLoading\) \{\n                        Text\("\.\.\.", fontSize = 18\.sp, fontWeight = FontWeight\.Bold, color = Color\(0xFF0F172A\)\)\n                    \} else \{\n                        Text\("₹\$\{"%,.2f"\.format\(activeStock\.price\)\}", fontSize = 18\.sp, fontWeight = FontWeight\.Bold, color = Color\(0xFF0F172A\)\)\n                    \}\n                \}\n                \n                LiveStockChart\(history = activeStock\.history, modifier = Modifier\.fillMaxWidth\(\)\.height\(160\.dp\), tickTrigger = tickTrigger\)\n            \}\n        \}', re.DOTALL)

new_chart_panel = """        // Chart Panel
        Surface(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = borderStroke()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (activeStock != null) {
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Text(activeStock.symbol.replace(".NS", ""), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                        if (isLoading && activeStock.price == 0.0) {
                            Text("...", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        } else {
                            Text("₹${"%,.2f".format(activeStock.price)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                    }
                    LiveStockChart(history = activeStock.history, modifier = Modifier.fillMaxWidth().height(160.dp), tickTrigger = tickTrigger)
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        Text("Add a stock to view live chart", color = Color(0xFF64748B))
                    }
                }
            }
        }"""

content = chart_panel_regex.sub(new_chart_panel, content)

# 6. Fix Button panel at the end to check activeStock
buttons_regex = re.compile(r'        // Buttons.*?        Row\((.*?)\) \{\n            Button\((.*?)\) \{\n                Text\("Buy \$\{activeSymbol\.replace\("\.NS", ""\)\}", (.*?)\n            \}\n            Button\((.*?)\) \{\n                Text\("Sell", (.*?)\n            \}\n        \}', re.DOTALL)

new_buttons = """        // Buttons
        if (activeStock != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Buy ${activeStock.symbol.replace(".NS", "")}", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Sell", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }"""

content = buttons_regex.sub(new_buttons, content)

with open('app/src/main/java/com/example/LiveScreen.kt', 'w') as f:
    f.write(content)

