import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Add compositionLocalOf
content = content.replace("import com.example.ui.theme.MyApplicationTheme", 
                          "import com.example.ui.theme.MyApplicationTheme\nimport androidx.compose.runtime.compositionLocalOf\nimport androidx.compose.foundation.isSystemInDarkTheme\nimport androidx.compose.runtime.CompositionLocalProvider\nimport androidx.compose.material.icons.filled.DarkMode\nimport androidx.compose.material.icons.filled.LightMode")

content = content.replace("class MainActivity : ComponentActivity() {", 
                          "val LocalThemeMode = compositionLocalOf<androidx.compose.runtime.MutableState<Boolean>> { error(\"No theme provided\") }\n\nclass MainActivity : ComponentActivity() {")

# Update setContent
old_setcontent = """        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }"""
new_setcontent = """        setContent {
            val isSystemDark = isSystemInDarkTheme()
            val isDarkTheme = remember { mutableStateOf(isSystemDark) }
            
            CompositionLocalProvider(LocalThemeMode provides isDarkTheme) {
                MyApplicationTheme(darkTheme = isDarkTheme.value) {
                    MainApp()
                }
            }
        }"""
content = content.replace(old_setcontent, new_setcontent)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
