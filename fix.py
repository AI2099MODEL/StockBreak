with open('app/src/main/java/com/example/StockScanner.kt', 'r') as f:
    content = f.read()

lines = content.split('\n')
for i, line in enumerate(lines):
    if 'reasons = if (reasons.isEmpty())' in line:
        lines[i] = '                reasons = if (reasons.isEmpty()) "No significant momentum detected in this timeframe." else "• " + reasons.joinToString("\\n• "),'
    elif '• "),' in line:
        lines[i] = '' # Clear the broken line
        
content = '\n'.join(lines)
with open('app/src/main/java/com/example/StockScanner.kt', 'w') as f:
    f.write(content)
