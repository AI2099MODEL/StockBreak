package com.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

data class ScanResult(
    val ticker: String,
    val name: String,
    val price: Double,
    val strategies: String,
    val score: Int,
    val reasons: String,
    val signalStrength: String,
    val stopLoss: Double?,
    val target1: Double?,
    val target2: Double?,
    val historicalPrices: List<Double> = emptyList(),
    val previousClose: Double? = null,
    val change: Double = 0.0,
    val changePercent: Double = 0.0,
    val isBtst: Boolean = false,
    val appearedTimestamp: Long = System.currentTimeMillis(),
    val recommendedPrice: Double = price
)

object StockScanner {
    val NIFTY_TICKERS = listOf(
        "360ONE.NS", "ABB.NS", "ACC.NS", "APLAPOLLO.NS", "AUBANK.NS", "ADANIENSOL.NS", 
        "ADANIENT.NS", "ADANIGREEN.NS", "ADANIPORTS.NS", "ADANIPOWER.NS", "ATGL.NS", 
        "ABCAPITAL.NS", "ALKEM.NS", "AMBUJACEM.NS", "APOLLOHOSP.NS", "ASHOKLEY.NS", 
        "ASIANPAINT.NS", "ASTRAL.NS", "AUROPHARMA.NS", "DMART.NS", "AXISBANK.NS", 
        "BSE.NS", "BAJAJ-AUTO.NS", "BAJFINANCE.NS", "BAJAJFINSV.NS", "BAJAJHLDNG.NS",
        "BAJAJHFL.NS", "BANKBARODA.NS", "BANKINDIA.NS", "BDL.NS", "BEL.NS", 
        "BHARATFORG.NS", "BHEL.NS", "BPCL.NS", "BHARTIARTL.NS", "BHARTIHEXA.NS", 
        "BIOCON.NS", "BLUESTARCO.NS", "BOSCHLTD.NS", "BRITANNIA.NS", "CGPOWER.NS", 
        "CANBK.NS", "CHOLAFIN.NS", "CIPLA.NS", "COALINDIA.NS", "COCHINSHIP.NS", 
        "COFORGE.NS", "COLPAL.NS", "CONCOR.NS", "COROMANDEL.NS", "CUMMINSIND.NS", 
        "DLF.NS", "DABUR.NS", "DIVISLAB.NS", "DIXON.NS", "DRREDDY.NS", "EICHERMOT.NS", 
        "ETERNAL.NS", "EXIDEIND.NS", "NYKAA.NS", "FEDERALBNK.NS", "FORTIS.NS", 
        "GAIL.NS", "GMRAIRPORT.NS", "GLENMARK.NS", "GODFRYPHLP.NS", "GODREJCP.NS", 
        "GODREJPROP.NS", "GRASIM.NS", "HCLTECH.NS", "HDFCAMC.NS", "HDFCBANK.NS", 
        "HDFCLIFE.NS", "HAVELLS.NS", "HEROMOTOCO.NS", "HINDALCO.NS", "HAL.NS", 
        "HINDPETRO.NS", "HINDUNILVR.NS", "HINDZINC.NS", "POWERINDIA.NS", "HUDCO.NS", 
        "HYUNDAI.NS", "ICICIBANK.NS", "ICICIGI.NS", "IDFCFIRSTB.NS", "IRB.NS", 
        "ITCHOTELS.NS", "ITC.NS", "INDIANB.NS", "INDHOTEL.NS", "IOC.NS", "IRCTC.NS", 
        "IRFC.NS", "IREDA.NS", "IGL.NS", "INDUSTOWER.NS", "INDUSINDBK.NS", "NAUKRI.NS", 
        "INFY.NS", "INDIGO.NS", "JSWENERGY.NS", "JSWSTEEL.NS", "JINDALSTEL.NS", 
        "JIOFIN.NS", "JUBLFOOD.NS", "KEI.NS", "KPITTECH.NS", "KALYANKJIL.NS", 
        "KOTAKBANK.NS", "LTF.NS", "LICHSGFIN.NS", "LTIM.NS", "LT.NS", "LICI.NS", 
        "LODHA.NS", "LUPIN.NS", "MRF.NS", "M&MFIN.NS", "M&M.NS", "MANKIND.NS", 
        "MARICO.NS", "MARUTI.NS", "MFSL.NS", "MAXHEALTH.NS", "MAZDOCK.NS", 
        "MOTILALOFS.NS", "MPHASIS.NS", "MUTHOOTFIN.NS", "NHPC.NS", "NMDC.NS", 
        "NTPCGREEN.NS", "NTPC.NS", "NATIONALUM.NS", "NESTLEIND.NS", "OBEROIRLTY.NS", 
        "ONGC.NS", "OIL.NS", "PAYTM.NS", "OFSS.NS", "POLICYBZR.NS", "PIIND.NS", 
        "PAGEIND.NS", "PATANJALI.NS", "PERSISTENT.NS", "PHOENIXLTD.NS", "PIDILITIND.NS", 
        "POLYCAB.NS", "PFC.NS", "POWERGRID.NS", "PREMIERENE.NS", "PRESTIGE.NS", 
        "PNB.NS", "RECLTD.NS", "RVNL.NS", "RELIANCE.NS", "SBICARD.NS", "SBILIFE.NS", 
        "SRF.NS", "MOTHERSON.NS", "SHREECEM.NS", "SHRIRAMFIN.NS", "ENRIN.NS", 
        "SIEMENS.NS", "SOLARINDS.NS", "SONACOMS.NS", "SBIN.NS", "SAIL.NS", 
        "SUNPHARMA.NS", "SUPREMEIND.NS", "SUZLON.NS", "SWIGGY.NS", "TVSMOTOR.NS", 
        "TATACOMM.NS", "TCS.NS", "TATACONSUM.NS", "TATAELXSI.NS", "TMPV.NS", 
        "TATAPOWER.NS", "TATASTEEL.NS", "TATATECH.NS", "TECHM.NS", "TITAN.NS", 
        "TORNTPHARM.NS", "TORNTPOWER.NS", "TRENT.NS", "TIINDIA.NS", "UPL.NS", 
        "ULTRACEMCO.NS", "UNIONBANK.NS", "UNITDSPR.NS", "VBL.NS", "VEDL.NS", 
        "VMM.NS", "IDEA.NS", "VOLTAS.NS", "WAAREEENER.NS", "WIPRO.NS", "YESBANK.NS", 
        "ZYDUSLIFE.NS"
    )
    
    suspend fun analyzeStock(ticker: String, category: String, requireBullish: Boolean = true): ScanResult? = withContext(Dispatchers.IO) {
        try {
            val (period, interval) = when (category) {
                "Intraday" -> Pair("5d", "15m")
                "BTST" -> Pair("5d", "15m")
                "Weekly" -> Pair("1y", "1d")
                "Monthly" -> Pair("2y", "1d") // Yahoo doesn't reliably do 1mo for all features, 1d is safer for TA.
                else -> Pair("1y", "1d")
            }
            val response = YahooRetrofit.service.getChart(ticker, period, interval)
            val result = response.chart?.result?.firstOrNull() ?: return@withContext null
            val name = result.meta?.shortName ?: result.meta?.longName ?: ticker
            val price = result.meta?.regularMarketPrice ?: return@withContext null
            val previousClose = result.meta?.previousClose ?: price
            val change = price - previousClose
            val changePercent = if (previousClose > 0.0) (change / previousClose) * 100 else 0.0
            val quote = result.indicators?.quote?.firstOrNull() ?: return@withContext null
            
            val closes = quote.close?.filterNotNull() ?: return@withContext null
            
            val highs = quote.high?.filterNotNull() ?: closes
            val lows = quote.low?.filterNotNull() ?: closes
            val volumes = quote.volume?.filterNotNull() ?: List(closes.size) { 0L }
            
            if (closes.size < 30) return@withContext null
            
            val signals = mutableListOf<String>()
            val reasons = mutableListOf<String>()
            var score = 0
            
            val strategies = listOf(
                Pair(TechnicalAnalysis::rsiSignal, "RSI"),
                Pair(TechnicalAnalysis::macdSignal, "MACD"),
                Pair(TechnicalAnalysis::emaCrossover, "EMA"),
                Pair(TechnicalAnalysis::bollingerSqueeze, "Bollinger"),
                Pair(TechnicalAnalysis::smaCrossover, "SMA 50/200")
            )
            
            for ((func, name) in strategies) {
                val (sig, reason, s) = func(closes)
                if (sig) {
                    signals.add(name)
                    if (reason != null) reasons.add(reason)
                    score += s
                }
            }
            
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

            if (volSig) {
                signals.add("Volume")
                if (volReason != null) reasons.add(volReason)
                score += volS
            }

            
            if (requireBullish && score == 0) return@withContext null
            
            val targets = TechnicalAnalysis.calculateTargets(highs, lows, closes, price)
            val rsiValue = TechnicalAnalysis.calculateRSI(closes)
            val vwapValue = TechnicalAnalysis.calculateVWAP(highs, lows, closes, volumes, 20)
            
            val (priceBreakSig, _, _) = TechnicalAnalysis.priceBreakout(highs, closes)
            val isGenuineBreakout = (priceBreakSig || volSig) && score >= 60 && rsiValue in 52.0..82.0
            
            val isBtstCandidate = (isGenuineBreakout || score >= 55) && (changePercent >= 0.2 || signals.contains("Volume") || signals.contains("SuperTrend") || signals.contains("RSI"))

            val strength = if (isGenuineBreakout) "STRONG BREAKOUT"
                else if (score >= 80) "STRONG BUY"
                else if (score >= 50) "BUY"
                else if (score > 0) "MILD BUY"
                else "NEUTRAL/WEAK"

            ScanResult(
                ticker = ticker,
                name = name,
                price = price,
                strategies = signals.joinToString(", ").ifEmpty { "No clear signals" },
                score = score,
                reasons = if (reasons.isEmpty()) "No significant momentum detected in this timeframe." else "• " + reasons.joinToString("\n• "),

                signalStrength = strength,
                stopLoss = targets["stop_loss"],
                target1 = targets["target_1"],
                target2 = targets["target_2"],
                historicalPrices = closes,
                previousClose = previousClose,
                change = change,
                changePercent = changePercent,
                isBtst = isBtstCandidate,
                appearedTimestamp = System.currentTimeMillis(),
                recommendedPrice = price
            )
            
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun scanMultiple(category: String = "Breakouts"): List<ScanResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<ScanResult>()
        val chunks = NIFTY_TICKERS.chunked(25)
        for (chunk in chunks) {
            val deferreds = chunk.map { ticker ->
                async { analyzeStock(ticker, category, requireBullish = false) }
            }
            results.addAll(deferreds.awaitAll().filterNotNull())
            kotlinx.coroutines.delay(150) // Avoid Yahoo rate limit
        }
        // Top 15 Breakout stocks sorted by technical score & momentum
        results.sortedByDescending { it.score }.take(15)
    }
}
