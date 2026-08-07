package com.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

data class NewsArticle(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val source: String = "Market News",
    val timeAgo: String = "Just now",
    val category: String = "General",
    val sentiment: String = "NEUTRAL", // BULLISH, BEARISH, NEUTRAL
    val url: String = ""
)

object NewsTickerService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    val DEFAULT_NEWS = listOf(
        "Nifty 50 surges past 25,000 mark driven by Banking & IT heavyweights rally",
        "Sensex gains 750 points following strong FII inflows of ₹4,200 Crore",
        "RBI keeps repo rate steady at 6.50% with optimistic GDP growth outlook",
        "TCS, Infosys & Wipro report strong Q3 revenue growth in Cloud & AI services",
        "Reliance Industries announces ₹20,000 Crore capex expansion in Green Energy",
        "Tata Motors reports 18% YoY growth in EV domestic passenger sales",
        "Vedanta announces Interim Dividend of ₹20/share with Ex-Date approaching",
        "US Fed hints at potential rate cut, driving global equity markets higher"
    )

    val SAMPLE_ARTICLES = listOf(
        NewsArticle(
            id = "1",
            title = "Nifty 50 Surges Past 25,000 Mark Driven by Banking & IT Heavyweights Rally",
            source = "Economic Times",
            timeAgo = "15 mins ago",
            category = "Nifty & Sensex",
            sentiment = "BULLISH",
            url = "https://economictimes.indiatimes.com/markets"
        ),
        NewsArticle(
            id = "2",
            title = "Sensex Gains 750 Points Following Strong FII Inflows of ₹4,200 Crore in Single Session",
            source = "Moneycontrol",
            timeAgo = "32 mins ago",
            category = "FII / DII",
            sentiment = "BULLISH",
            url = "https://www.moneycontrol.com/news/business/markets"
        ),
        NewsArticle(
            id = "3",
            title = "RBI Keeps Repo Rate Steady at 6.50% with Optimistic FY25 GDP Growth Outlook",
            source = "CNBC TV18",
            timeAgo = "1 hour ago",
            category = "Nifty & Sensex",
            sentiment = "NEUTRAL",
            url = "https://www.cnbctv18.com"
        ),
        NewsArticle(
            id = "4",
            title = "TCS & Infosys Lead Tech Rally After Securing Multi-Billion Dollar Cloud & AI Contracts",
            source = "Mint Markets",
            timeAgo = "2 hours ago",
            category = "Corporate & Q3",
            sentiment = "BULLISH",
            url = "https://www.livemint.com/market"
        ),
        NewsArticle(
            id = "5",
            title = "Reliance Industries Board Approves ₹20,000 Crore Strategic Green Energy Capex Expansion",
            source = "Business Standard",
            timeAgo = "3 hours ago",
            category = "Corporate & Q3",
            sentiment = "BULLISH",
            url = "https://www.business-standard.com"
        ),
        NewsArticle(
            id = "6",
            title = "US Fed Signals Dovish Interest Rate Policy as Inflation Cools to Target Range",
            source = "Reuters",
            timeAgo = "4 hours ago",
            category = "Global Markets",
            sentiment = "BULLISH",
            url = "https://www.reuters.com/markets"
        ),
        NewsArticle(
            id = "7",
            title = "Crude Oil Prices Ease Below $78/Barrel, Relieving Input Pressure on Indian Paint & Chemical Stocks",
            source = "Bloomberg",
            timeAgo = "5 hours ago",
            category = "Global Markets",
            sentiment = "NEUTRAL",
            url = "https://www.bloomberg.com"
        ),
        NewsArticle(
            id = "8",
            title = "Tata Motors Domestic Passenger EV Sales Jump 18% YoY with High Festival Demand",
            source = "Financial Express",
            timeAgo = "6 hours ago",
            category = "Corporate & Q3",
            sentiment = "BULLISH",
            url = "https://www.financialexpress.com"
        ),
        NewsArticle(
            id = "9",
            title = "Upcoming Mega IPO Subscriptions Open Next Week: Key Highlights & Anchor Allocations",
            source = "Zee Business",
            timeAgo = "7 hours ago",
            category = "IPO & Earnings",
            sentiment = "NEUTRAL",
            url = "https://www.zeebiz.com"
        ),
        NewsArticle(
            id = "10",
            title = "Vedanta Board Declares Interim Dividend of ₹20 Per Share; Ex-Dividend Date Fixed",
            source = "NDTV Profit",
            timeAgo = "8 hours ago",
            category = "Corporate & Q3",
            sentiment = "BULLISH",
            url = "https://www.ndtvprofit.com"
        )
    )

    suspend fun fetchLatestNews(): List<String> = withContext(Dispatchers.IO) {
        val articles = fetchNewsArticles("All")
        if (articles.isNotEmpty()) {
            articles.map { "${it.title} (${it.source})" }
        } else {
            DEFAULT_NEWS
        }
    }

    suspend fun fetchNewsForQuery(query: String): List<NewsArticle> = withContext(Dispatchers.IO) {
        try {
            val encoded = java.net.URLEncoder.encode(query, "UTF-8")
            val url = "https://news.google.com/rss/search?q=$encoded+when:2d&hl=en-IN&gl=IN&ceid=IN:en"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()

            val response = client.newCall(request).execute()
            val xml = response.body?.string() ?: return@withContext emptyList()
            parseRSSXml(xml, "All")
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchNewsArticles(category: String = "All"): List<NewsArticle> = withContext(Dispatchers.IO) {
        try {
            val queryParam = when (category) {
                "Nifty & Sensex" -> "Nifty+50+Sensex+Stock+Market+India"
                "Corporate & Q3" -> "India+Company+Quarterly+Results+Corporate+Stock+News"
                "FII / DII" -> "FII+DII+Flows+Stock+Market+India"
                "Global Markets" -> "US+Fed+Global+Stock+Markets+Crude+Oil"
                "IPO & Earnings" -> "IPO+Subscription+Allotment+Listing+India"
                else -> "NSE+Nifty+Sensex+Stock+Market+India"
            }

            val url = "https://news.google.com/rss/search?q=$queryParam+when:2d&hl=en-IN&gl=IN&ceid=IN:en"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()

            val response = client.newCall(request).execute()
            val xml = response.body?.string() ?: return@withContext SAMPLE_ARTICLES

            val articles = parseRSSXml(xml, category)
            if (articles.isNotEmpty()) articles else SAMPLE_ARTICLES
        } catch (e: Exception) {
            SAMPLE_ARTICLES
        }
    }

    private fun parseRSSXml(xml: String, fallbackCategory: String): List<NewsArticle> {
        val articles = mutableListOf<NewsArticle>()
        val itemRegex = "<item>(.*?)</item>".toRegex(setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
        val matches = itemRegex.findAll(xml)

        for ((index, match) in matches.withIndex()) {
            val itemXml = match.groupValues[1]

            val rawTitle = extractTagContent(itemXml, "title") ?: continue
            val link = extractTagContent(itemXml, "link") ?: ""
            val pubDate = extractTagContent(itemXml, "pubDate") ?: ""
            val sourceFromTag = extractTagContent(itemXml, "source")

            if (rawTitle.isBlank() || rawTitle.equals("Google News", ignoreCase = true)) continue

            // Unescape HTML entities & clean CDATA
            val cleanTitle = cleanHtmlEntities(rawTitle)

            // Split title and source correctly (source is after the LAST ' - ')
            val lastDashIndex = cleanTitle.lastIndexOf(" - ")
            val headline: String
            val source: String

            if (lastDashIndex != -1) {
                headline = cleanTitle.substring(0, lastDashIndex).trim()
                val parsedSource = cleanTitle.substring(lastDashIndex + 3).trim()
                source = if (parsedSource.isNotBlank()) parsedSource else (sourceFromTag ?: "Market News")
            } else {
                headline = cleanTitle
                source = sourceFromTag ?: "Economic Times"
            }

            if (headline.isBlank() || headline.equals("Google News", ignoreCase = true)) continue

            val (timeAgo, isTooOld) = calculateTimeAgoWithAgeCheck(pubDate, index)
            // Strictly exclude any news older than 2 days (48 hours)
            if (isTooOld) continue

            val sentiment = determineSentiment(headline)
            val articleCat = determineCategory(headline, fallbackCategory)

            articles.add(
                NewsArticle(
                    id = "rss_$index",
                    title = headline,
                    source = source,
                    timeAgo = timeAgo,
                    category = articleCat,
                    sentiment = sentiment,
                    url = link.trim()
                )
            )

            if (articles.size >= 15) break
        }

        return articles
    }

    private fun extractTagContent(xml: String, tagName: String): String? {
        val regex = "<$tagName.*?>(.*?)</$tagName>".toRegex(setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
        val match = regex.find(xml) ?: return null
        return match.groupValues[1]
            .replace("<![CDATA[", "")
            .replace("]]>", "")
            .trim()
    }

    private fun cleanHtmlEntities(text: String): String {
        return text
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
            .replace("&#8217;", "'")
            .replace("&#8220;", "\"")
            .replace("&#8221;", "\"")
            .replace("&#8211;", "-")
            .replace("&#8212;", "—")
            .trim()
    }

    private fun calculateTimeAgoWithAgeCheck(pubDateStr: String, fallbackIndex: Int): Pair<String, Boolean> {
        if (pubDateStr.isBlank()) return Pair("${(fallbackIndex + 1) * 15} mins ago", false)
        return try {
            val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
            val date = sdf.parse(pubDateStr)
            if (date != null) {
                val diffMs = System.currentTimeMillis() - date.time
                val diffMins = TimeUnit.MILLISECONDS.toMinutes(diffMs)
                val diffHours = TimeUnit.MILLISECONDS.toHours(diffMs)

                // Enforce maximum 2 days (48 hours) threshold
                val isTooOld = diffHours > 48

                val timeStr = when {
                    diffMins <= 0 -> "Just now"
                    diffMins < 60 -> "$diffMins mins ago"
                    diffHours < 24 -> "$diffHours hrs ago"
                    diffHours < 48 -> "1 day ago"
                    else -> "${diffHours / 24} days ago"
                }
                Pair(timeStr, isTooOld)
            } else {
                Pair("${(fallbackIndex + 1) * 15} mins ago", false)
            }
        } catch (e: Exception) {
            Pair("${(fallbackIndex + 1) * 15} mins ago", false)
        }
    }

    private fun determineSentiment(headline: String): String {
        val text = headline.lowercase(Locale.ROOT)
        return when {
            text.contains("surge") || text.contains("gain") || text.contains("rally") ||
            text.contains("rise") || text.contains("high") || text.contains("jump") ||
            text.contains("bull") || text.contains("record") || text.contains("profit") ||
            text.contains("boost") || text.contains("soar") || text.contains("up ") -> "BULLISH"

            text.contains("fall") || text.contains("drop") || text.contains("plunge") ||
            text.contains("slide") || text.contains("down") || text.contains("cut") ||
            text.contains("bear") || text.contains("loss") || text.contains("slump") ||
            text.contains("sink") || text.contains("crash") -> "BEARISH"

            else -> "NEUTRAL"
        }
    }

    private fun determineCategory(headline: String, fallback: String): String {
        if (fallback != "All") return fallback

        val text = headline.lowercase(Locale.ROOT)
        return when {
            text.contains("nifty") || text.contains("sensex") || text.contains("market live") || text.contains("share market") -> "Nifty & Sensex"
            text.contains("fii") || text.contains("dii") || text.contains("inflow") || text.contains("outflow") || text.contains("fund") -> "FII / DII"
            text.contains("ipo") || text.contains("earnings") || text.contains("q1") || text.contains("q2") || text.contains("q3") || text.contains("q4") || text.contains("allotment") -> "IPO & Earnings"
            text.contains("fed") || text.contains("global") || text.contains("us ") || text.contains("wall street") || text.contains("crude") || text.contains("oil") -> "Global Markets"
            else -> "Corporate & Q3"
        }
    }
}


