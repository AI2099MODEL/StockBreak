import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

replacement = """
        // Balance Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("TOTAL PORTFOLIO VALUE", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("₹ 4,850.42", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFE6F4EA),
                    contentColor = Color(0xFF137333)
                ) {
                    Text("+ ₹ 432.10 (1.77%) Today", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
        }

        // Main Content
"""

content = content.replace("        // Main Content", replacement)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
