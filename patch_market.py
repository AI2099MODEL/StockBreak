import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

target = """            Screen.MARKET -> MarketScreen(modifier = Modifier.padding(innerPadding))"""

replacement = """            Screen.MARKET -> {
                // Feature removed
            }"""

content = content.replace(target, replacement)
with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
