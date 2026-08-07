with open('app/src/main/java/com/example/StockScanner.kt', 'r') as f:
    content = f.read()

target_block = """            val (volSig, volReason, volS) = TechnicalAnalysis.volumeBreakout(volumes, closes)
            if (volSig) {
                signals.add("Volume")
                if (volReason != null) reasons.add(volReason)
                score += volS
            }"""

replacement_block = """            val (volSig, volReason, volS) = TechnicalAnalysis.volumeBreakout(volumes, closes)
            if (volSig) {
                signals.add("Volume Surge")
                if (volReason != null) reasons.add(volReason)
                score += volS
            }

            val (priceBreakSig, priceBreakReason, priceBreakS) = TechnicalAnalysis.priceBreakout(highs, closes)
            if (priceBreakSig) {
                signals.add("Resistance Breakout")
                if (priceBreakReason != null) reasons.add(priceBreakReason)
                score += priceBreakS
            }"""

if "priceBreakout" not in content:
    content = content.replace(target_block, replacement_block)

target_strength = """            val isGenuineBreakout = score >= 130 && rsiValue in 55.0..75.0 && price > vwapValue * 1.002
            
            val strength = if (isGenuineBreakout) "STRONG BUY"
                else if (score >= 90) "BUY"
                else if (score >= 50) "MILD BUY"
                else if (score > 0) "HOLD"
                else "WEAK/SELL" """

replacement_strength = """            val isGenuineBreakout = (priceBreakSig || volSig) && score >= 60 && rsiValue in 52.0..82.0
            
            val strength = if (isGenuineBreakout) "STRONG BREAKOUT"
                else if (score >= 80) "STRONG BUY"
                else if (score >= 50) "BUY"
                else if (score > 0) "MILD BUY"
                else "NEUTRAL/WEAK" """

# Fallback replace string
if "val isGenuineBreakout =" in content:
    idx1 = content.find("val isGenuineBreakout =")
    idx2 = content.find("ScanResult(", idx1)
    old_section = content[idx1:idx2]
    new_section = """val (priceBreakSig, _, _) = TechnicalAnalysis.priceBreakout(highs, closes)
            val isGenuineBreakout = (priceBreakSig || volSig) && score >= 60 && rsiValue in 52.0..82.0
            
            val strength = if (isGenuineBreakout) "STRONG BREAKOUT"
                else if (score >= 80) "STRONG BUY"
                else if (score >= 50) "BUY"
                else if (score > 0) "MILD BUY"
                else "NEUTRAL/WEAK"

            """
    content = content[:idx1] + new_section + content[idx2:]

with open('app/src/main/java/com/example/StockScanner.kt', 'w') as f:
    f.write(content)

print("Patched StockScanner.kt successfully")
