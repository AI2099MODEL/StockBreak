import re

with open('app/src/main/java/com/example/StockScanner.kt', 'r') as f:
    content = f.read()

target = """    suspend fun analyzeStock(ticker: String, category: String, requireBullish: Boolean = true): ScanResult? {"""

replacement = """    suspend fun analyzeStock(ticker: String, category: String, requireBullish: Boolean = true): ScanResult? = withContext(Dispatchers.IO) {"""

content = content.replace(target, replacement)

# The end of analyzeStock needs an extra bracket if we did this? No, analyzeStock already had { ... }. Wait, if we replace `{` with `= withContext(Dispatchers.IO) {`, it's perfectly valid Kotlin!

with open('app/src/main/java/com/example/StockScanner.kt', 'w') as f:
    f.write(content)
