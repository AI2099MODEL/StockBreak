with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace('import androidx.compose.ui.platform.LocalFocusManager', 'import androidx.compose.ui.platform.LocalFocusManager\nimport androidx.compose.ui.platform.LocalContext\nimport android.content.Context')

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
