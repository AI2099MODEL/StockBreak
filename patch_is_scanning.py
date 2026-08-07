import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

target_effect = """    LaunchedEffect(selectedCategory) {
        if (!isScanning) {
            isScanning = true
            scanResults = emptyList()
            scanResults = StockScanner.scanMultiple(selectedCategory)
            isScanning = false
        }
    }"""

replacement_effect = """    LaunchedEffect(selectedCategory) {
        try {
            isScanning = true
            scanResults = emptyList()
            scanResults = StockScanner.scanMultiple(selectedCategory)
        } finally {
            isScanning = false
        }
    }"""

content = content.replace(target_effect, replacement_effect)

target_refresh = """                        modifier = Modifier.clickable {
                            if (!isScanning) {
                                isScanning = true
                                scanResults = emptyList()
                                coroutineScope.launch {
                                    scanResults = StockScanner.scanMultiple(selectedCategory)
                                    isScanning = false
                                }
                            }
                        }"""

replacement_refresh = """                        modifier = Modifier.clickable {
                            if (!isScanning) {
                                isScanning = true
                                scanResults = emptyList()
                                coroutineScope.launch {
                                    try {
                                        scanResults = StockScanner.scanMultiple(selectedCategory)
                                    } finally {
                                        isScanning = false
                                    }
                                }
                            }
                        }"""

content = content.replace(target_refresh, replacement_refresh)

# Also fix the surface clicks to use onClick if possible, but clickable is fine.
# Let's just write back the content.
with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

