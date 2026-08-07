import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

target = """                NavigationBarItem(
                    icon = { Icon(Icons.Default.OndemandVideo, contentDescription = "Market") },
                    label = { Text("Market") },
                    selected = currentScreen == Screen.MARKET,
                    onClick = { currentScreen = Screen.MARKET }
                )"""

content = content.replace(target, "")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
