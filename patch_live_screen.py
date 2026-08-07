with open('app/src/main/java/com/example/LiveScreen.kt', 'r') as f:
    content = f.read()

target = """    LaunchedEffect(Unit) {
        while (isActive) {
            for (i in stocks.indices) {"""

replacement = """    LaunchedEffect(Unit) {
        while (isActive) {
            if (MarketUtils.isMarketOpen()) {
                for (i in stocks.indices) {"""

target_end = """                        if (stock.price != 0.0 && (crossedUp || crossedDown)) {
                            updatedStock.isTargetTriggered = true
                            triggeredAlert = updatedStock
                        }

                        stocks[i] = updatedStock
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            delay(5000)
        }
    }"""

if target in content:
    content = content.replace(target, replacement)
    # add closing brace for if
    old_end = """                        stocks[i] = updatedStock
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            delay(5000)
        }
    }"""
    if old_end in content:
        content = content.replace(old_end, target_end)

with open('app/src/main/java/com/example/LiveScreen.kt', 'w') as f:
    f.write(content)

print("Patched LiveScreen.kt for market hours polling")
