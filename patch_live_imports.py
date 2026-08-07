import re

with open('app/src/main/java/com/example/LiveScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("import androidx.compose.ui.Modifier", "import androidx.compose.ui.Modifier\nimport com.example.LocalThemeMode\nimport androidx.compose.material.icons.filled.DarkMode\nimport androidx.compose.material.icons.filled.LightMode")

with open('app/src/main/java/com/example/LiveScreen.kt', 'w') as f:
    f.write(content)
