package com.example

import com.squareup.moshi.JsonClass
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.OkHttpClient

@JsonClass(generateAdapter = true)
data class YahooChartResponse(
    val chart: ChartResultWrapper?
)

@JsonClass(generateAdapter = true)
data class ChartResultWrapper(
    val result: List<ChartData>?,
    val error: Any?
)

@JsonClass(generateAdapter = true)
data class ChartData(
    val meta: ChartMeta?,
    val timestamp: List<Long>?,
    val indicators: ChartIndicators?
)

@JsonClass(generateAdapter = true)
data class ChartMeta(
    val regularMarketPrice: Double?,
    val symbol: String?,
    val previousClose: Double?,
    val shortName: String?,
    val longName: String?
)

@JsonClass(generateAdapter = true)
data class ChartIndicators(
    val quote: List<ChartQuote>?
)

@JsonClass(generateAdapter = true)
data class ChartQuote(
    val close: List<Double?>?,
    val high: List<Double?>?,
    val low: List<Double?>?,
    val volume: List<Long?>?
)

interface YahooFinanceService {
    @GET("v8/finance/chart/{ticker}")
    suspend fun getChart(
        @Path("ticker") ticker: String,
        @Query("range") range: String = "1d",
        @Query("interval") interval: String = "1m"
    ): YahooChartResponse
}

object YahooRetrofit {
    private val memoryCache = java.util.concurrent.ConcurrentHashMap<String, Pair<Long, okhttp3.Response>>()

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                val url = request.url.toString()
                
                // Simple memory cache (60 seconds)
                memoryCache[url]?.let { (timestamp, cachedResponse) ->
                    if (System.currentTimeMillis() - timestamp < 60000) {
                        return@addInterceptor cachedResponse.newBuilder()
                            .body(cachedResponse.peekBody(Long.MAX_VALUE))
                            .build()
                    }
                }
                
                val builder = request.newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                    .header("Accept", "application/json")
                
                val response = chain.proceed(builder.build())
                if (response.isSuccessful) {
                    memoryCache[url] = Pair(System.currentTimeMillis(), response.newBuilder().body(response.peekBody(Long.MAX_VALUE)).build())
                }
                response
            }
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    val service: YahooFinanceService by lazy {
        Retrofit.Builder()
            .baseUrl("https://query1.finance.yahoo.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(YahooFinanceService::class.java)
    }
}
