with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Remove Market nav item
market_nav = '''        NavigationBarItem(selected = currentScreen == Screen.MARKET, onClick = { onScreenSelected(Screen.MARKET) }, icon = { Icon(Icons.Default.OndemandVideo, contentDescription = "Market") }, label = { Text("Market") })'''
content = content.replace(market_nav, '')

# Replace Intraday with Breakouts in RecommendationsScreen
content = content.replace('StockScanner.scanMultiple("Intraday")', 'StockScanner.scanMultiple("Breakouts")')

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

print("Updated bottom nav & recommendations screen")
