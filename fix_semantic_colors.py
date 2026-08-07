import re

with open('app/src/main/java/com/example/LiveScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("MaterialTheme.colorScheme.primaryContainer", "Color(0xFF10B981).copy(alpha = 0.1f)")
content = content.replace("MaterialTheme.colorScheme.onPrimaryContainer", "Color(0xFF10B981)")
content = content.replace("Color(0xFFC5221F)", "Color(0xFFEF4444)")
content = content.replace("Color(0xFF137333)", "Color(0xFF10B981)")

with open('app/src/main/java/com/example/LiveScreen.kt', 'w') as f:
    f.write(content)
