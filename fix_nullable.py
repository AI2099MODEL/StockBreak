with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

target = '''                                val livePrice = meta?.regularMarketPrice ?: item.price
                                val prevClose = meta?.previousClose ?: item.previousClose
                                val change = livePrice - prevClose
                                val changePercent = if (prevClose > 0) (change / prevClose) * 100 else 0.0'''

replacement = '''                                val livePrice = meta?.regularMarketPrice ?: item.price
                                val prevClose = meta?.previousClose ?: item.previousClose ?: livePrice
                                val change = livePrice - prevClose
                                val changePercent = if (prevClose > 0) (change / prevClose) * 100 else 0.0'''

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

print("Fixed nullable previousClose")
