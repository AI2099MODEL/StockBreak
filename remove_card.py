import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

replacement = """        // Main Content"""

# Replace the block
content = re.sub(r'\s*// Balance Card[\s\S]*?// Main Content', '\n\n        // Main Content', content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
