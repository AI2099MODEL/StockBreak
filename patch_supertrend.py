import re

with open('app/src/main/java/com/example/TechnicalAnalysis.kt', 'r') as f:
    content = f.read()

supertrend_code = """
    fun supertrendSignal(highs: List<Double>, lows: List<Double>, closes: List<Double>): Triple<Boolean, String?, Int> {
        val period = 10
        val multiplier = 3.0
        if (closes.size < period + 1) return Triple(false, null, 0)
        
        val atr = mutableListOf<Double>()
        var currentTrSum = 0.0
        
        for (i in 1 until closes.size) {
            val hl = highs[i] - lows[i]
            val hc = abs(highs[i] - closes[i - 1])
            val lc = abs(lows[i] - closes[i - 1])
            val tr = max(hl, max(hc, lc))
            
            if (i <= period) {
                currentTrSum += tr
                if (i == period) atr.add(currentTrSum / period)
            } else {
                val lastAtr = atr.last()
                atr.add((lastAtr * (period - 1) + tr) / period)
            }
        }
        
        val basicUb = mutableListOf<Double>()
        val basicLb = mutableListOf<Double>()
        for (i in period until closes.size) {
            val hl2 = (highs[i] + lows[i]) / 2.0
            val currentAtr = atr[i - period]
            basicUb.add(hl2 + multiplier * currentAtr)
            basicLb.add(hl2 - multiplier * currentAtr)
        }
        
        val finalUb = mutableListOf<Double>()
        val finalLb = mutableListOf<Double>()
        val supertrend = mutableListOf<Double>()
        val trend = mutableListOf<Int>() // 1 for up, -1 for down
        
        finalUb.add(basicUb[0])
        finalLb.add(basicLb[0])
        trend.add(1)
        supertrend.add(finalLb[0])
        
        for (i in 1 until basicUb.size) {
            val closeIdx = i + period
            val prevClose = closes[closeIdx - 1]
            val currClose = closes[closeIdx]
            
            val ub = if (basicUb[i] < finalUb[i - 1] || prevClose > finalUb[i - 1]) basicUb[i] else finalUb[i - 1]
            val lb = if (basicLb[i] > finalLb[i - 1] || prevClose < finalLb[i - 1]) basicLb[i] else finalLb[i - 1]
            
            finalUb.add(ub)
            finalLb.add(lb)
            
            var currentTrend = trend[i - 1]
            if (supertrend[i - 1] == finalUb[i - 1] && currClose > finalUb[i]) {
                currentTrend = 1
            } else if (supertrend[i - 1] == finalUb[i - 1] && currClose <= finalUb[i]) {
                currentTrend = -1
            } else if (supertrend[i - 1] == finalLb[i - 1] && currClose >= finalLb[i]) {
                currentTrend = 1
            } else if (supertrend[i - 1] == finalLb[i - 1] && currClose < finalLb[i]) {
                currentTrend = -1
            }
            
            trend.add(currentTrend)
            supertrend.add(if (currentTrend == 1) lb else ub)
        }
        
        if (trend.size >= 2) {
            val currentTrend = trend.last()
            val prevTrend = trend[trend.size - 2]
            
            if (currentTrend == 1 && prevTrend == -1) {
                return Triple(true, "SuperTrend Buy Signal", 30)
            } else if (currentTrend == 1) {
                return Triple(true, "SuperTrend Bullish", 10)
            }
        }
        
        return Triple(false, null, 0)
    }
"""

content = content.replace("fun calculateTargets", supertrend_code + "\n    fun calculateTargets")

with open('app/src/main/java/com/example/TechnicalAnalysis.kt', 'w') as f:
    f.write(content)
