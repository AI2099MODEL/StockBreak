import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

replacement = """@Composable
fun MainApp() {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var selectedSymbol by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { AppBottomNavigation(currentScreen) { currentScreen = it } }
    ) { innerPadding ->
        when (currentScreen) {
            Screen.HOME -> DashboardScreen(modifier = Modifier.padding(innerPadding)) { symbol -> 
                selectedSymbol = symbol
                currentScreen = Screen.LIVE
            }
            Screen.WATCHLIST -> WatchlistScreen(modifier = Modifier.padding(innerPadding)) { symbol ->
                selectedSymbol = symbol
                currentScreen = Screen.LIVE
            }
            Screen.MARKET -> MarketScreen(modifier = Modifier.padding(innerPadding))
            Screen.LIVE -> LiveScreen(modifier = Modifier.padding(innerPadding), initialSymbol = selectedSymbol)
        }
    }
}"""

content = re.sub(r'@Composable\s*fun MainApp\(\)\s*\{.*?\}\s*\}', replacement, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
