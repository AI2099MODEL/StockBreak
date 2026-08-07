import re

with open('app/src/main/java/com/example/LiveScreen.kt', 'r') as f:
    content = f.read()

match = re.search(r'while\s*\(isActive\)\s*\{.*?delay\(5000\).*?\}', content, re.MULTILINE | re.DOTALL)
if match:
    print(match.group(0))
