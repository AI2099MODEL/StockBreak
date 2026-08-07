import re

with open('app/src/main/java/com/example/StockScanner.kt', 'r') as f:
    content = f.read()

# Update period
content = content.replace('"Weekly" -> Pair("90d", "1d")', '"Weekly" -> Pair("1y", "1d")')
content = content.replace('else -> Pair("90d", "1d")', 'else -> Pair("1y", "1d")')

# Add supertrend
replacement = """
            val (stochSig, stochReason, stochS) = TechnicalAnalysis.stochasticOscillator(highs, lows, closes)
            if (stochSig) {
                signals.add("Stochastic")
                if (stochReason != null) reasons.add(stochReason)
                score += stochS
            }
            
            val (superSig, superReason, superS) = TechnicalAnalysis.supertrendSignal(highs, lows, closes)
            if (superSig) {
                signals.add("SuperTrend")
                if (superReason != null) reasons.add(superReason)
                score += superS
            }
            
            val (volSig, volReason, volS) = TechnicalAnalysis.volumeBreakout(volumes, closes)
"""

content = content.replace("""
            val (stochSig, stochReason, stochS) = TechnicalAnalysis.stochasticOscillator(highs, lows, closes)
            if (stochSig) {
                signals.add("Stochastic")
                if (stochReason != null) reasons.add(stochReason)
                score += stochS
            }
            
            val (volSig, volReason, volS) = TechnicalAnalysis.volumeBreakout(volumes, closes)""", replacement)

with open('app/src/main/java/com/example/StockScanner.kt', 'w') as f:
    f.write(content)
