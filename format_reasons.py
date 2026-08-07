import re

with open('app/src/main/java/com/example/StockScanner.kt', 'r') as f:
    content = f.read()

replacement = 'reasons = if (reasons.isEmpty()) "No significant momentum detected in this timeframe." else "• " + reasons.joinToString("\\n• ")'
content = re.sub(r'reasons = reasons\.joinToString\("\\n• "\)\.ifEmpty \{ "No significant momentum detected in this timeframe\." \}', replacement, content)

with open('app/src/main/java/com/example/StockScanner.kt', 'w') as f:
    f.write(content)
