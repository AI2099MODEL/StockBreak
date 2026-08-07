with open('app/src/main/java/com/example/LiveScreen.kt', 'r') as f:
    content = f.read()

target = """@Composable
fun LiveScreen(modifier: Modifier = Modifier) {
        val stocks = remember { 
         mutableStateListOf<LiveStock>(
            LiveStock("RELIANCE.NS", "RELIANCE.NS"),
            LiveStock("TCS.NS", "TCS.NS"),
            LiveStock("HDFCBANK.NS", "HDFCBANK.NS"),
            LiveStock("INFY.NS", "INFY.NS")
        ) 
    }
    var activeSymbol by remember { mutableStateOf("RELIANCE.NS") }"""

replacement = """@Composable
fun LiveScreen(modifier: Modifier = Modifier, initialSymbol: String? = null) {
    val stocks = remember { 
        mutableStateListOf<LiveStock>(
            LiveStock("RELIANCE.NS", "RELIANCE.NS"),
            LiveStock("TCS.NS", "TCS.NS"),
            LiveStock("HDFCBANK.NS", "HDFCBANK.NS"),
            LiveStock("INFY.NS", "INFY.NS")
        ) 
    }
    var activeSymbol by remember { mutableStateOf(initialSymbol ?: "RELIANCE.NS") }

    LaunchedEffect(initialSymbol) {
        if (!initialSymbol.isNullOrEmpty()) {
            if (stocks.none { it.symbol == initialSymbol }) {
                stocks.add(LiveStock(initialSymbol, initialSymbol))
            }
            activeSymbol = initialSymbol
        }
    }"""

if target in content:
    content = content.replace(target, replacement)
    print("Replaced successfully")
else:
    print("Target not found. Doing regex fallback...")
    import re
    # use a non-greedy .*? with re.DOTALL
    content = re.sub(
        r'@Composable\s*fun LiveScreen\(modifier: Modifier = Modifier\)\s*\{\s*val stocks = remember\s*\{\s*mutableStateListOf<LiveStock>\(.*?\)\s*\}\s*var activeSymbol by remember\s*\{\s*mutableStateOf\("RELIANCE\.NS"\)\s*\}',
        replacement,
        content,
        flags=re.DOTALL
    )

with open('app/src/main/java/com/example/LiveScreen.kt', 'w') as f:
    f.write(content)
