import re

with open('app/src/main/java/com/example/LiveScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'import androidx.compose.material3.*',
    'import androidx.compose.material3.*\nimport androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.filled.*'
)

content = content.replace(
    'Icon(androidx.compose.material.icons.filled.Search, contentDescription = "Search")',
    'Icon(Icons.Default.Search, contentDescription = "Search")'
)

with open('app/src/main/java/com/example/LiveScreen.kt', 'w') as f:
    f.write(content)

