import re

with open('app/src/main/java/com/example/LiveScreen.kt', 'r') as f:
    content = f.read()

old_header = """        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Investify", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
            
            Surface("""

new_header = """        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Investify", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                val themeMode = LocalThemeMode.current
                IconButton(onClick = { themeMode.value = !themeMode.value }) {
                    Icon(
                        imageVector = if (themeMode.value) androidx.compose.material.icons.Icons.Default.LightMode else androidx.compose.material.icons.Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            
            Surface("""
            
content = content.replace(old_header, new_header)

with open('app/src/main/java/com/example/LiveScreen.kt', 'w') as f:
    f.write(content)
