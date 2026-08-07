with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# 1. Ensure MarketScreen is rendered in Screen.MARKET block
target_market = '''            Screen.MARKET -> {
                // Feature removed
            }'''
replacement_market = '''            Screen.MARKET -> MarketScreen(modifier = Modifier.padding(innerPadding))'''

if target_market in content:
    content = content.replace(target_market, replacement_market)

# 2. Add Market back to bottom navigation bar if missing
nav_target = '''        NavigationBarItem(selected = currentScreen == Screen.WATCHLIST, onClick = { onScreenSelected(Screen.WATCHLIST) }, icon = { Icon(Icons.Default.Favorite, contentDescription = "Watchlist") }, label = { Text("Watchlist") })'''

nav_replacement = '''        NavigationBarItem(selected = currentScreen == Screen.WATCHLIST, onClick = { onScreenSelected(Screen.WATCHLIST) }, icon = { Icon(Icons.Default.Favorite, contentDescription = "Watchlist") }, label = { Text("Watchlist") })
        NavigationBarItem(selected = currentScreen == Screen.MARKET, onClick = { onScreenSelected(Screen.MARKET) }, icon = { Icon(Icons.Default.OndemandVideo, contentDescription = "Market") }, label = { Text("Market") })'''

if "Screen.MARKET" not in content.split("fun AppBottomNavigation")[1]:
    content = content.replace(nav_target, nav_replacement)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

print("Updated MainActivity.kt with MarketScreen!")
