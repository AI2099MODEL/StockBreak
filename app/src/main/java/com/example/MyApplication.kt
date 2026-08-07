package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.room.Room

class MyApplication : Application() {
    companion object {
        lateinit var instance: MyApplication
            private set
        lateinit var database: AppDatabase
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "price-alert-db"
        ).fallbackToDestructiveMigration().build()

        createNotificationChannel()
        WorkerUtils.schedulePriceAlertWorker(this)
        WorkerUtils.scheduleDividendWorker(this)
        WorkerUtils.scheduleBreakoutPerformanceWorker(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Price Alerts"
            val descriptionText = "Notifications for stock price alerts"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("PRICE_ALERTS", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            val divName = "Dividend Alerts"
            val divDesc = "Notifications for upcoming dividend ex-dates and record dates"
            val divImportance = NotificationManager.IMPORTANCE_DEFAULT
            val divChannel = NotificationChannel("DIVIDEND_ALERTS", divName, divImportance).apply {
                description = divDesc
            }
            notificationManager.createNotificationChannel(divChannel)
        }
    }
}
