import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# 1. Update Screen Enum
content = content.replace(
    "enum class Screen {\n    HOME, CHARTS, PORTFOLIO, CONFIG\n}",
    "enum class Screen {\n    HOME, CHARTS, PORTFOLIO, LIVE, CONFIG\n}"
)

# 2. Update MainApp when block
content = content.replace(
    """        when (currentScreen) {
            Screen.HOME -> DashboardScreen(modifier = Modifier.padding(innerPadding))
            Screen.CHARTS -> ChartsScreen(modifier = Modifier.padding(innerPadding))
            Screen.PORTFOLIO -> PortfolioScreen(modifier = Modifier.padding(innerPadding))
            Screen.CONFIG -> ConfigScreen(modifier = Modifier.padding(innerPadding))
        }""",
    """        when (currentScreen) {
            Screen.HOME -> DashboardScreen(modifier = Modifier.padding(innerPadding))
            Screen.CHARTS -> ChartsScreen(modifier = Modifier.padding(innerPadding))
            Screen.PORTFOLIO -> PortfolioScreen(modifier = Modifier.padding(innerPadding))
            Screen.LIVE -> LiveScreen(modifier = Modifier.padding(innerPadding))
            Screen.CONFIG -> ConfigScreen(modifier = Modifier.padding(innerPadding))
        }"""
)

# 3. Update AppBottomNavigation
old_nav = """@Composable
fun AppBottomNavigation(currentScreen: Screen, onScreenSelected: (Screen) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 0.dp,
        modifier = Modifier.border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        NavigationBarItem(selected = currentScreen == Screen.HOME, onClick = { onScreenSelected(Screen.HOME) }, icon = { Icon(Icons.Default.Home, contentDescription = "Home") }, label = { Text("Home") })
        NavigationBarItem(selected = currentScreen == Screen.CHARTS, onClick = { onScreenSelected(Screen.CHARTS) }, icon = { Icon(Icons.Default.Insights, contentDescription = "Charts") }, label = { Text("Charts") })
        NavigationBarItem(selected = currentScreen == Screen.PORTFOLIO, onClick = { onScreenSelected(Screen.PORTFOLIO) }, icon = { Icon(Icons.Default.BarChart, contentDescription = "Portfolio") }, label = { Text("Portfolio") })
        NavigationBarItem(selected = currentScreen == Screen.CONFIG, onClick = { onScreenSelected(Screen.CONFIG) }, icon = { Icon(Icons.Default.Settings, contentDescription = "Config") }, label = { Text("Config") })
    }
}"""

new_nav = """@Composable
fun AppBottomNavigation(currentScreen: Screen, onScreenSelected: (Screen) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 0.dp,
        modifier = Modifier.border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        NavigationBarItem(selected = currentScreen == Screen.HOME, onClick = { onScreenSelected(Screen.HOME) }, icon = { Icon(Icons.Default.Home, contentDescription = "Home") }, label = { Text("Home") })
        NavigationBarItem(selected = currentScreen == Screen.CHARTS, onClick = { onScreenSelected(Screen.CHARTS) }, icon = { Icon(Icons.Default.Insights, contentDescription = "Charts") }, label = { Text("Charts") })
        NavigationBarItem(selected = currentScreen == Screen.PORTFOLIO, onClick = { onScreenSelected(Screen.PORTFOLIO) }, icon = { Icon(Icons.Default.BarChart, contentDescription = "Portfolio") }, label = { Text("Portfolio") })
        NavigationBarItem(selected = currentScreen == Screen.LIVE, onClick = { onScreenSelected(Screen.LIVE) }, icon = { Icon(Icons.Default.ShowChart, contentDescription = "Live") }, label = { Text("Live") })
        NavigationBarItem(selected = currentScreen == Screen.CONFIG, onClick = { onScreenSelected(Screen.CONFIG) }, icon = { Icon(Icons.Default.Settings, contentDescription = "Config") }, label = { Text("Config") })
    }
}"""

content = content.replace(old_nav, new_nav)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
