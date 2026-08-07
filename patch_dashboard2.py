import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Make the stock card in DashboardScreen clickable too
card_target = """Card(
                    modifier = Modifier.width(160.dp),"""
card_replacement = """Card(
                    modifier = Modifier.width(160.dp).clickable { onSymbolSelected(res.ticker) },"""
content = content.replace(card_target, card_replacement)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
