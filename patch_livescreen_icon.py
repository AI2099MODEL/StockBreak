import re

with open('app/src/main/java/com/example/LiveScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'Icon(androidx.compose.material.icons.Icons.Default.Search, contentDescription = "Search")',
    'Icon(androidx.compose.material.icons.filled.Search, contentDescription = "Search")'
)

with open('app/src/main/java/com/example/LiveScreen.kt', 'w') as f:
    f.write(content)
