with open('app/src/main/java/com/example/StockScanner.kt', 'r') as f:
    content = f.read()

content = content.replace('results.sortedByDescending { it.score }.take(10)', 'results.sortedByDescending { it.score }.take(15)')
content = content.replace('suspend fun scanMultiple(category: String): List<ScanResult>', 'suspend fun scanMultiple(category: String = "Breakouts"): List<ScanResult>')

with open('app/src/main/java/com/example/StockScanner.kt', 'w') as f:
    f.write(content)

print("Updated StockScanner.kt")
