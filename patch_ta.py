with open('app/src/main/java/com/example/TechnicalAnalysis.kt', 'r') as f:
    content = f.read()

price_breakout_code = """
    fun priceBreakout(highs: List<Double>, closes: List<Double>): Triple<Boolean, String?, Int> {
        if (highs.size < 21 || closes.size < 21) return Triple(false, null, 0)
        val currentClose = closes.last()
        val prev20Highs = highs.takeLast(21).dropLast(1)
        val max20High = prev20Highs.maxOrNull() ?: 0.0

        if (max20High > 0.0 && currentClose > max20High) {
            val pct = ((currentClose - max20High) / max20High) * 100
            return Triple(true, "20-Day Resistance Breakout (+%.1f%%)".format(pct), 40)
        }

        if (highs.size >= 51 && closes.size >= 51) {
            val prev50Highs = highs.takeLast(51).dropLast(1)
            val max50High = prev50Highs.maxOrNull() ?: 0.0
            if (max50High > 0.0 && currentClose > max50High) {
                val pct = ((currentClose - max50High) / max50High) * 100
                return Triple(true, "50-Day Resistance Breakout (+%.1f%%)".format(pct), 50)
            }
        }
        return Triple(false, null, 0)
    }
"""

if "fun priceBreakout" not in content:
    idx = content.rfind("}")
    content = content[:idx] + price_breakout_code + "\n}\n"
    with open('app/src/main/java/com/example/TechnicalAnalysis.kt', 'w') as f:
        f.write(content)
    print("Added priceBreakout to TechnicalAnalysis.kt")
else:
    print("priceBreakout already in TechnicalAnalysis.kt")
