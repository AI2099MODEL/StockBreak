package com.example

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "price_alerts")
data class PriceAlert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ticker: String,
    val name: String = "",
    val priceTarget: Double,
    val isTriggered: Boolean = false,
    val isAlertActive: Boolean = true,
    val userId: String = ""
)

@Dao
interface PriceAlertDao {
    @Query("SELECT * FROM price_alerts")
    fun getAllAlerts(): Flow<List<PriceAlert>>

    @Query("SELECT * FROM price_alerts WHERE userId = :userId OR (:userId = '' AND (userId IS NULL OR userId = ''))")
    fun getAlertsForUser(userId: String): Flow<List<PriceAlert>>
    
    @Query("SELECT * FROM price_alerts WHERE isAlertActive = 1")
    suspend fun getActiveAlerts(): List<PriceAlert>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: PriceAlert)
    
    @Update
    suspend fun updateAlert(alert: PriceAlert)

    @Query("DELETE FROM price_alerts WHERE id = :id")
    suspend fun deleteAlertById(id: Int)
}

@Entity(tableName = "breakout_performance")
data class BreakoutPerformanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ticker: String,
    val companyName: String,
    val recommendedPrice: Double,
    val currentPrice: Double,
    val pnlAmount: Double,
    val pnlPercent: Double,
    val isTargetHit: Boolean,
    val targetPrice: Double,
    val appearedDate: String,
    val appearedTimestamp: Long,
    val status: String // "ACTIVE", "TARGET_HIT", "CLOSED"
)

@Entity(tableName = "daily_breakout_logs")
data class DailyBreakoutLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // "YYYY-MM-DD" e.g., "2026-08-07"
    val timestamp: Long = System.currentTimeMillis(),
    val totalAmountInvested: Double = 0.0,
    val totalProfitLossAmount: Double,
    val totalProfitLossPercent: Double,
    val tradesCount: Int,
    val winningTrades: Int,
    val losingTrades: Int,
    val topPerformers: String = "",
    val isAutoExecutedAt2Percent: Boolean = true,
    val isFinalBookedAt315: Boolean = false
)

@Dao
interface DailyBreakoutLogDao {
    @Query("SELECT * FROM daily_breakout_logs ORDER BY timestamp DESC")
    fun getAllDailyLogs(): Flow<List<DailyBreakoutLogEntity>>

    @Query("SELECT * FROM daily_breakout_logs WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getDailyLogsSince(sinceTimestamp: Long): Flow<List<DailyBreakoutLogEntity>>

    @Query("SELECT * FROM daily_breakout_logs WHERE date = :date LIMIT 1")
    suspend fun getLogByDate(date: String): DailyBreakoutLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(log: DailyBreakoutLogEntity)

    @Query("DELETE FROM daily_breakout_logs WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteOlderThan(cutoffTimestamp: Long)

    @Query("DELETE FROM daily_breakout_logs")
    suspend fun clearAll()
}

@Dao
interface BreakoutPerformanceDao {
    @Query("SELECT * FROM breakout_performance ORDER BY appearedTimestamp DESC")
    fun getAllBreakoutPerformance(): Flow<List<BreakoutPerformanceEntity>>

    @Query("SELECT * FROM breakout_performance WHERE appearedTimestamp >= :sinceTimestamp ORDER BY appearedTimestamp DESC")
    fun getBreakoutPerformanceSince(sinceTimestamp: Long): Flow<List<BreakoutPerformanceEntity>>

    @Query("SELECT * FROM breakout_performance WHERE ticker = :ticker ORDER BY appearedTimestamp ASC LIMIT 1")
    suspend fun getByTicker(ticker: String): BreakoutPerformanceEntity?

    @Query("SELECT * FROM breakout_performance WHERE ticker = :ticker AND appearedDate = :date LIMIT 1")
    suspend fun getByTickerAndDate(ticker: String, date: String): BreakoutPerformanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: BreakoutPerformanceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<BreakoutPerformanceEntity>)

    @Query("DELETE FROM breakout_performance WHERE appearedTimestamp < :cutoffTimestamp")
    suspend fun deleteOlderThan(cutoffTimestamp: Long)

    @Query("DELETE FROM breakout_performance")
    suspend fun clearAll()
}

@Database(entities = [PriceAlert::class, BreakoutPerformanceEntity::class, DailyBreakoutLogEntity::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun priceAlertDao(): PriceAlertDao
    abstract fun breakoutPerformanceDao(): BreakoutPerformanceDao
    abstract fun dailyBreakoutLogDao(): DailyBreakoutLogDao
}
