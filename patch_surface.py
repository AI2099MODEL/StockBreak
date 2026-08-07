import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

target = """                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable { selectedCategory = title }
                        ) {"""

replacement = """                        Surface(
                            onClick = { selectedCategory = title },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        ) {"""

content = content.replace(target, replacement)

target2 = """                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.clickable {
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
                        }
                    ) {"""

replacement2 = """                    Surface(
                        onClick = {
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
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {"""

content = content.replace(target2, replacement2)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
