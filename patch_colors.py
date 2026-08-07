with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Update colors for Categories
content = content.replace('Color(0xFF10B981) else Color(0xFFF1F5F9)', 'Color(0xFF2DD4BF) else Color(0xFFF0FDF4)')
content = content.replace('Color.White else Color(0xFF64748B)', 'Color.White else Color(0xFF475569)')

# Update colors for Prices
content = content.replace('Color(0xFF3B82F6) else Color(0xFFF1F5F9)', 'Color(0xFFD1FAE5) else Color.Transparent')
content = content.replace('Color.White else Color(0xFF64748B)', 'Color(0xFF0F172A) else Color(0xFF475569)')

# Update colors for Ratings
content = content.replace('Color(0xFF8B5CF6) else Color(0xFFF1F5F9)', 'Color(0xFFD1FAE5) else Color.Transparent')

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
