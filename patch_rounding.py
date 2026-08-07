import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Fix in Dashboard
content = content.replace(
    'Text("₹${res.target1 ?: "N/A"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)',
    'Text("₹${res.target1?.let { Math.round(it) } ?: "N/A"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)'
)

content = content.replace(
    'Text("₹${res.stopLoss ?: "N/A"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)',
    'Text("₹${res.stopLoss?.let { Math.round(it) } ?: "N/A"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)'
)

# Fix in TimeframeAnalysisCard
content = content.replace(
    'Text("🎯 Target 1: ${res.target1?.let { "₹%.2f".format(it) } ?: "N/A"}", fontSize = 14.sp)',
    'Text("🎯 Target 1: ${res.target1?.let { "₹${Math.round(it)}" } ?: "N/A"}", fontSize = 14.sp)'
)

content = content.replace(
    'Text("🎯 Target 2: ${res.target2?.let { "₹%.2f".format(it) } ?: "N/A"}", fontSize = 14.sp)',
    'Text("🎯 Target 2: ${res.target2?.let { "₹${Math.round(it)}" } ?: "N/A"}", fontSize = 14.sp)'
)

content = content.replace(
    'Text("🛑 Stop Loss: ${res.stopLoss?.let { "₹%.2f".format(it) } ?: "N/A"}", fontSize = 14.sp)',
    'Text("🛑 Stop Loss: ${res.stopLoss?.let { "₹${Math.round(it)}" } ?: "N/A"}", fontSize = 14.sp)'
)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
