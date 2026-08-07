with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

if "@Composable\nfun AppBottomNavigation" not in content:
    nav_code = """
@Composable
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
}
"""
    with open('app/src/main/java/com/example/MainActivity.kt', 'a') as f:
        f.write(nav_code)
