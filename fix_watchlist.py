import re

with open('app/src/main/java/com/example/WatchlistScreen.kt', 'r') as f:
    content = f.read()

if 'import androidx.compose.foundation.clickable' not in content:
    content = content.replace('import androidx.compose.foundation.background', 'import androidx.compose.foundation.background\nimport androidx.compose.foundation.clickable\n')

with open('app/src/main/java/com/example/WatchlistScreen.kt', 'w') as f:
    f.write(content)
