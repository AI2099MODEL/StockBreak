import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'val y = height - if (priceRange == 0.0) height / 2 else ((price - minPrice) / priceRange) * height',
    'val y = height - if (priceRange == 0.0) height / 2 else ((price - minPrice) / priceRange).toFloat() * height'
)

content = content.replace("""
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(timeframe, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)
                val color = when(res.signalStrength) {
                    "STRONG BUY", "BUY" -> Color(0xFF10B981)
                    "SELL", "WEAK/SELL" -> Color(0xFFEF4444)
                    else -> Color(0xFFF59E0B)
                }
                Text(res.signalStrength, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            }
""", """
            val color = when(res.signalStrength) {
                "STRONG BUY", "BUY" -> Color(0xFF10B981)
                "SELL", "WEAK/SELL" -> Color(0xFFEF4444)
                else -> Color(0xFFF59E0B)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(timeframe, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onBackground)
                Text(res.signalStrength, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            }
""")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
