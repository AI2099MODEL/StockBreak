import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Categories
content = content.replace(
    'color = if (selected) Color(0xFF2DD4BF) else Color(0xFFF0FDF4),\n                        contentColor = if (selected) Color(0xFF0F172A) else Color(0xFF475569),',
    'color = if (selected) Color(0xFF2ECA8B) else Color(0xFFFCFDF2),\n                        contentColor = if (selected) Color.White else Color(0xFF475569),'
)

# Price/Rating Filters
content = content.replace(
    'color = if (selected) Color(0xFFD1FAE5) else Color.Transparent,\n                        contentColor = if (selected) Color(0xFF0F172A) else Color(0xFF475569),',
    'color = if (selected) Color(0xFFE5F5E0) else Color(0xFFF6FAF0),\n                        contentColor = if (selected) Color(0xFF0F172A) else Color(0xFF475569),'
)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
