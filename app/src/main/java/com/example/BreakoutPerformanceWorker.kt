package com.example

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BreakoutPerformanceWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val db = MyApplication.database
        val dao = db.breakoutPerformanceDao()
        val dailyLogDao = db.dailyBreakoutLogDao()

        // 6-month database archive retention (180 days) - auto delete older records to keep database small
        val sixMonthsAgo = System.currentTimeMillis() - (180L * 24 * 60 * 60 * 1000L)
        dao.deleteOlderThan(sixMonthsAgo)
        dailyLogDao.deleteOlderThan(sixMonthsAgo)

        try {
            val scanResults = StockScanner.scanMultiple("Breakouts")
            if (scanResults.isEmpty()) return@withContext Result.success()

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDateStr = dateFormat.format(Date())

            val calendar = Calendar.getInstance()
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val isWeekday = dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            val timeInMinutes = hour * 60 + minute
            // Indian Stock Market (NSE/BSE) trading hours: Monday to Friday, 09:15 AM to 03:30 PM IST
            val isMarketHours = isWeekday && (timeInMinutes in (9 * 60 + 15)..(15 * 60 + 30))

            // If market is closed (weekend or outside 09:15-15:30), do NOT log P&L
            if (!isMarketHours) {
                return@withContext Result.success()
            }

            var winningTrades = 0
            var losingTrades = 0
            var totalPnlAmount = 0.0
            var totalInvestment = 0.0
            val topPerformersList = mutableListOf<String>()

            for (res in scanResults) {
                val existing = dao.getByTicker(res.ticker)
                val recommendedPrice = existing?.recommendedPrice ?: (res.recommendedPrice.takeIf { it > 0 } ?: res.price)
                val currentPrice = res.price
                val pnlAmount = currentPrice - recommendedPrice
                val pnlPercent = if (recommendedPrice > 0) (pnlAmount / recommendedPrice) * 100 else 0.0
                
                // Virtual sell execution: Hits target or reaches 2% profit target
                val targetPrice = res.target1 ?: (recommendedPrice * 1.02)
                val isTargetHit = currentPrice >= targetPrice || pnlPercent >= 2.0
                val status = if (isTargetHit) "TARGET_HIT" else "ACTIVE"

                val entity = BreakoutPerformanceEntity(
                    id = existing?.id ?: 0,
                    ticker = res.ticker,
                    companyName = res.name,
                    recommendedPrice = recommendedPrice,
                    currentPrice = currentPrice,
                    pnlAmount = pnlAmount,
                    pnlPercent = pnlPercent,
                    isTargetHit = isTargetHit,
                    targetPrice = targetPrice,
                    appearedDate = existing?.appearedDate ?: currentDateStr,
                    appearedTimestamp = existing?.appearedTimestamp ?: res.appearedTimestamp,
                    status = status
                )
                dao.insertOrUpdate(entity)

                totalInvestment += recommendedPrice
                totalPnlAmount += pnlAmount
                if (isTargetHit || pnlPercent >= 0) {
                    winningTrades++
                    if (pnlPercent >= 1.5 && topPerformersList.size < 3) {
                        topPerformersList.add("${res.ticker.replace(".NS", "")} (+${"%.1f".format(pnlPercent)}%)")
                    }
                } else {
                    losingTrades++
                }
            }

            // Save or update the daily profit/loss log entry in Room database
            val totalPnlPercent = if (totalInvestment > 0) (totalPnlAmount / totalInvestment) * 100 else 0.0
            val isAtOrAfter315PM = timeInMinutes >= (15 * 60 + 15) // 3:15 PM IST
            val existingDailyLog = dailyLogDao.getLogByDate(currentDateStr)
            val isFinalBooked = isAtOrAfter315PM || (existingDailyLog?.isFinalBookedAt315 == true)

            val dailyLog = DailyBreakoutLogEntity(
                id = existingDailyLog?.id ?: 0,
                date = currentDateStr,
                timestamp = existingDailyLog?.timestamp ?: System.currentTimeMillis(),
                totalAmountInvested = totalInvestment,
                totalProfitLossAmount = totalPnlAmount,
                totalProfitLossPercent = totalPnlPercent,
                tradesCount = scanResults.size,
                winningTrades = winningTrades,
                losingTrades = losingTrades,
                topPerformers = topPerformersList.joinToString(", "),
                isAutoExecutedAt2Percent = true,
                isFinalBookedAt315 = isFinalBooked
            )
            dailyLogDao.insertOrUpdate(dailyLog)

            // Final 3:15 PM market close booking notification
            if (isAtOrAfter315PM && hour == 15 && minute in 15..30) {
                sendEndOfDayNotification(scanResults)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        Result.success()
    }

    private fun sendEndOfDayNotification(scanResults: List<ScanResult>) {
        val context = applicationContext
        try {
            var totalInvestment = 0.0
            var totalCurrentValue = 0.0
            var targetHitCount = 0

            for (res in scanResults) {
                val buyPrice = res.recommendedPrice.takeIf { it > 0 } ?: res.price
                val curPrice = res.price
                totalInvestment += buyPrice
                totalCurrentValue += curPrice
                val pnlPct = if (buyPrice > 0) ((curPrice - buyPrice) / buyPrice) * 100 else 0.0
                if (curPrice >= (res.target1 ?: (buyPrice * 1.02)) || pnlPct >= 2.0) {
                    targetHitCount++
                }
            }

            val totalPnlAmount = totalCurrentValue - totalInvestment
            val totalPnlPercent = if (totalInvestment > 0) (totalPnlAmount / totalInvestment) * 100 else 0.0
            val pnlSign = if (totalPnlAmount >= 0) "+" else ""
            val summaryText = "If 1 share of each of ${scanResults.size} breakout stocks was purchased: P&L = $pnlSign₹${"%.2f".format(totalPnlAmount)} ($pnlSign${"%.2f".format(totalPnlPercent)}%). Targets Hit: $targetHitCount/${scanResults.size}"

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, "PRICE_ALERTS")
                .setSmallIcon(android.R.drawable.ic_menu_agenda)
                .setContentTitle("Market Close Breakout Summary (15:45)")
                .setContentText(summaryText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(summaryText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) {
                notify(9999, builder.build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
