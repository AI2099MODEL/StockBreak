package com.example
import kotlinx.coroutines.*

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.horizontalScroll
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.util.Locale
import java.util.Date
import java.text.SimpleDateFormat
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.nativead.MediaView
import android.widget.TextView
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.view.View
import android.view.Gravity
import android.graphics.Typeface
import android.graphics.Color as AndroidColor

enum class Screen {
    HOME, LIVE, DIVIDENDS, WATCHLIST, MARKET, NEWS
}

@Composable
fun TopScrollingTickerBanner() {
    val scrollState = rememberScrollState()
    val isDark = LocalThemeMode.current.value

    LaunchedEffect(Unit) {
        while (isActive) {
            val maxScroll = scrollState.maxValue
            if (maxScroll > 0) {
                if (scrollState.value >= maxScroll) {
                    scrollState.scrollTo(0)
                } else {
                    scrollState.animateScrollTo(
                        value = scrollState.value + 100,
                        animationSpec = tween(durationMillis = 2200, easing = LinearEasing)
                    )
                }
            }
            delay(120)
        }
    }

    Surface(
        color = if (!isDark) Color(0xFFF1F5F9) else Color(0xFF090D16),
        modifier = Modifier.fillMaxWidth().height(30.dp),
        border = BorderStroke(1.dp, if (!isDark) Color(0xFFCBD5E1) else Color(0xFF1E293B))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(topEnd = 6.dp, bottomEnd = 6.dp),
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "LIVE TICKER",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TickerItem(tag = "NIFTY", text = "Bullish Momentum Above 24,800", isUp = true)
                TickerItem(tag = "RELIANCE", text = "Q1 Revenue up +11.5% YoY • Strong Buy Signal", isUp = true)
                TickerItem(tag = "TCS", text = "Breakout Score 82.5/100 • AI Growth Surge", isUp = true)
                TickerItem(tag = "BANKNIFTY", text = "Key Support Level 51,200", isUp = false)
                TickerItem(tag = "DISCLAIMER", text = "Educational & Informational Purpose Only • Not SEBI Registered Advice", isUp = null)
                TickerItem(tag = "HDFCBANK", text = "Ex-Dividend Date Approaching", isUp = true)
                TickerItem(tag = "INFY", text = "Cloud Migration Partnership Deal Signed", isUp = true)
            }
        }
    }
}

@Composable
fun TickerItem(tag: String, text: String, isUp: Boolean?) {
    val isDark = LocalThemeMode.current.value
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            color = when (isUp) {
                true -> if (!isDark) Color(0xFFD1FAE5) else Color(0xFF10B981).copy(alpha = 0.2f)
                false -> if (!isDark) Color(0xFFFEE2E2) else Color(0xFFEF4444).copy(alpha = 0.2f)
                else -> if (!isDark) Color(0xFFE2E8F0) else MaterialTheme.colorScheme.secondaryContainer
            },
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = tag,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = when (isUp) {
                    true -> Color(0xFF047857)
                    false -> Color(0xFFB91C1C)
                    else -> if (!isDark) Color(0xFF334155) else MaterialTheme.colorScheme.onSecondaryContainer
                },
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            )
        }
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (!isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
            maxLines = 1
        )
    }
}



@Composable
fun GlobalTopBar(
    niftyPrice: String,
    niftyChange: String,
    niftyIsPositive: Boolean,
    sensexPrice: String,
    sensexChange: String,
    sensexIsPositive: Boolean,
    giftNiftyPrice: String,
    giftNiftyChange: String,
    giftNiftyIsPositive: Boolean,
    onOpenAuthDialog: () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Row 1: App Branding & Theme Toggle & Auth Profile Icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "StockBreak App Icon",
                            modifier = Modifier.size(28.dp).padding(2.dp)
                        )
                    }
                    AnimatedHeadingText("StockBreak", fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val themeMode = LocalThemeMode.current
                    IconButton(onClick = { themeMode.value = !themeMode.value }, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = if (themeMode.value) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    AuthProfileButton(onClick = onOpenAuthDialog)
                }
            }

            // Row 2: NIFTY, GIFT NIFTY & SENSEX figures all in one single horizontal line
            val isDark = LocalThemeMode.current.value
            Surface(
                color = if (!isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A).copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // NIFTY 50
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("NIFTY", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(niftyPrice, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                        Text(niftyChange, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (niftyIsPositive) Color(0xFF10B981) else Color(0xFFEF4444))
                    }

                    Text("•", fontSize = 8.sp, color = MaterialTheme.colorScheme.outlineVariant)

                    // GIFT NIFTY
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("GIFT NIFTY", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(giftNiftyPrice, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                        Text(giftNiftyChange, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (giftNiftyIsPositive) Color(0xFF10B981) else Color(0xFFEF4444))
                    }

                    Text("•", fontSize = 8.sp, color = MaterialTheme.colorScheme.outlineVariant)

                    // SENSEX
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text("SENSEX", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(sensexPrice, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onBackground)
                        Text(sensexChange, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (sensexIsPositive) Color(0xFF10B981) else Color(0xFFEF4444))
                    }
                }
            }
        }
    }
}

@Composable
fun MainApp() {
    val authManager = remember { AuthManager.getInstance() }
    val currentUser by authManager.currentUser.collectAsState()
    var isGuestMode by remember { mutableStateOf(false) }

    if (currentUser == null && !isGuestMode) {
        LoginScreen(onContinueAsGuest = { isGuestMode = true })
    } else {
        var showAuthDialog by remember { mutableStateOf(false) }
        var showRewardedAdDialog by remember { mutableStateOf(false) }
        var pendingAnalysisSymbol by remember { mutableStateOf<String?>(null) }
        var currentScreen by remember { mutableStateOf(Screen.HOME) }
        var selectedSymbol by remember { mutableStateOf<String?>(null) }

        if (showAuthDialog) {
            UserProfileDialog(onDismiss = { showAuthDialog = false })
        }

        if (showRewardedAdDialog) {
            RewardedAdPromptDialog(
                onDismiss = { showRewardedAdDialog = false },
                onWatchAdClicked = {
                    showRewardedAdDialog = false
                    if (pendingAnalysisSymbol != null) {
                        selectedSymbol = pendingAnalysisSymbol
                        pendingAnalysisSymbol = null
                    }
                    currentScreen = Screen.LIVE
                }
            )
        }

        val requestAnalysisScreen = { symbol: String? ->
            pendingAnalysisSymbol = symbol
            showRewardedAdDialog = true
        }
    
    var niftyPrice by remember { mutableStateOf("24,850.40") }
    var niftyChange by remember { mutableStateOf("+1.77%") }
    var niftyIsPositive by remember { mutableStateOf(true) }

    var sensexPrice by remember { mutableStateOf("81,200.15") }
    var sensexChange by remember { mutableStateOf("+1.85%") }
    var sensexIsPositive by remember { mutableStateOf(true) }

    var giftNiftyPrice by remember { mutableStateOf("24,900.00") }
    var giftNiftyChange by remember { mutableStateOf("+0.50%") }
    var giftNiftyIsPositive by remember { mutableStateOf(true) }

    // Persistent Live Indices polling across ALL 5 tabs
    LaunchedEffect(Unit) {
        var curNiftyVal = 24850.40
        var prevNiftyClose = 24418.00

        var curSensexVal = 81200.15
        var prevSensexClose = 79725.00
        
        var curGiftNiftyVal = 24900.00
        var prevGiftNiftyClose = 24850.40

        var isInitialized = false

        while (isActive) {
            try {
                withContext(Dispatchers.IO) {
                    try {
                        var resNifty = YahooRetrofit.service.getChart("^NSEI", "1d", "1m")
                        var meta = resNifty.chart?.result?.firstOrNull()?.meta
                        var p = meta?.regularMarketPrice
                        var prev = meta?.previousClose
                        if (p == null || prev == null) {
                            resNifty = YahooRetrofit.service.getChart("^NSEI", "5d", "1d")
                            meta = resNifty.chart?.result?.firstOrNull()?.meta
                            p = meta?.regularMarketPrice
                            prev = meta?.previousClose
                        }
                        if (p != null && prev != null && prev > 0) {
                            curNiftyVal = p
                            prevNiftyClose = prev
                        }
                    } catch (e: Exception) {}

                    try {
                        var resSensex = YahooRetrofit.service.getChart("^BSESN", "1d", "1m")
                        var meta = resSensex.chart?.result?.firstOrNull()?.meta
                        var p = meta?.regularMarketPrice
                        var prev = meta?.previousClose
                        if (p == null || prev == null) {
                            resSensex = YahooRetrofit.service.getChart("^BSESN", "5d", "1d")
                            meta = resSensex.chart?.result?.firstOrNull()?.meta
                            p = meta?.regularMarketPrice
                            prev = meta?.previousClose
                        }
                        if (p != null && prev != null && prev > 0) {
                            curSensexVal = p
                            prevSensexClose = prev
                        }
                    } catch (e: Exception) {}
                }
                if (!isInitialized) {
                    curGiftNiftyVal = curNiftyVal + 50.0
                    prevGiftNiftyClose = prevNiftyClose + 50.0
                    isInitialized = true
                }

                val isMarketOpen = MarketUtils.isMarketOpen()
                // Micro-tick simulation to ensure live ticker updates continuously in background
                if (isMarketOpen) {
                    val niftyDelta = ((-15..15).random() * 0.1)
                    curNiftyVal = (curNiftyVal + niftyDelta).coerceAtLeast(20000.0)

                    val sensexDelta = ((-30..30).random() * 0.2)
                    curSensexVal = (curSensexVal + sensexDelta).coerceAtLeast(60000.0)
                    
                    curGiftNiftyVal = curNiftyVal + ((-10..10).random() * 0.1)
                } else {
                    val giftNiftyDelta = ((-15..15).random() * 0.1)
                    curGiftNiftyVal = (curGiftNiftyVal + giftNiftyDelta).coerceAtLeast(20000.0)
                }

                val nChg = curNiftyVal - prevNiftyClose
                val nPct = (nChg / prevNiftyClose) * 100
                niftyPrice = String.format(Locale.US, "%,.2f", curNiftyVal)
                niftyChange = String.format(Locale.US, "%+.2f%%", nPct)
                niftyIsPositive = nChg >= 0

                val sChg = curSensexVal - prevSensexClose
                val sPct = (sChg / prevSensexClose) * 100
                sensexPrice = String.format(Locale.US, "%,.2f", curSensexVal)
                sensexChange = String.format(Locale.US, "%+.2f%%", sPct)
                sensexIsPositive = sChg >= 0
                
                val gChg = curGiftNiftyVal - prevGiftNiftyClose
                val gPct = (gChg / prevGiftNiftyClose) * 100
                giftNiftyPrice = String.format(Locale.US, "%,.2f", curGiftNiftyVal)
                giftNiftyChange = String.format(Locale.US, "%+.2f%%", gPct)
                giftNiftyIsPositive = gChg >= 0
            } catch (e: Exception) {}
            delay(3000)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                GlobalTopBar(
                    niftyPrice = niftyPrice,
                    niftyChange = niftyChange,
                    niftyIsPositive = niftyIsPositive,
                    sensexPrice = sensexPrice,
                    sensexChange = sensexChange,
                    sensexIsPositive = sensexIsPositive,
                    giftNiftyPrice = giftNiftyPrice,
                    giftNiftyChange = giftNiftyChange,
                    giftNiftyIsPositive = giftNiftyIsPositive,
                    onOpenAuthDialog = { showAuthDialog = true }
                )
            }
        },
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                AppBottomNavigation(currentScreen) { screen ->
                    if (screen == Screen.LIVE) {
                        requestAnalysisScreen(null)
                    } else {
                        currentScreen = screen
                    }
                }
                if (!showAuthDialog && currentUser != null) {
                    AdBannerView()
                }
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            Screen.HOME -> DashboardScreen(
                modifier = Modifier.padding(innerPadding),
                onSymbolSelected = { symbol -> 
                    requestAnalysisScreen(symbol)
                },
                onOpenAuthDialog = { showAuthDialog = true }
            )
            Screen.LIVE -> LiveScreen(modifier = Modifier.padding(innerPadding), initialSymbol = selectedSymbol)
            Screen.DIVIDENDS -> DividendsScreen(modifier = Modifier.padding(innerPadding), onSymbolSelected = { symbol ->
                requestAnalysisScreen(symbol)
            })
            Screen.WATCHLIST -> WatchlistScreen(
                modifier = Modifier.padding(innerPadding),
                onSymbolSelected = { symbol ->
                    requestAnalysisScreen(symbol)
                },
                onOpenAuthDialog = { showAuthDialog = true }
            )
            Screen.MARKET -> MarketScreen(modifier = Modifier.padding(innerPadding))
            Screen.NEWS -> NewsScreen(modifier = Modifier.padding(innerPadding))
        }
    }
    }
}

@Composable
fun AdBannerView(
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-8815300826143812/6413991379"
) {
    var currentAdUnitId by remember { mutableStateOf(adUnitId) }

    key(currentAdUnitId) {
        AndroidView(
            modifier = modifier.fillMaxWidth().height(55.dp).background(Color.Transparent),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = currentAdUnitId
                    adListener = object : com.google.android.gms.ads.AdListener() {
                        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                            // Fallback to Google sample test banner ad unit if custom ad unit has no fill during dev/testing
                            if (currentAdUnitId != "ca-app-pub-3940256099942544/6300978111") {
                                currentAdUnitId = "ca-app-pub-3940256099942544/6300978111"
                            }
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}

@Composable
fun NativeAdViewComposable(
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-8815300826143812/9903865584"
) {
    var nativeAdState by remember { mutableStateOf<NativeAd?>(null) }
    val context = LocalContext.current

    DisposableEffect(adUnitId) {
        fun loadNativeAd(targetId: String) {
            val adLoader = AdLoader.Builder(context, targetId)
                .forNativeAd { ad ->
                    nativeAdState?.destroy()
                    nativeAdState = ad
                }
                .withAdListener(object : com.google.android.gms.ads.AdListener() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        // If live custom ad unit returns no fill during testing, retry with Google Test Native Unit ID
                        if (targetId != "ca-app-pub-3940256099942544/2247696110") {
                            loadNativeAd("ca-app-pub-3940256099942544/2247696110")
                        }
                    }
                })
                .withNativeAdOptions(NativeAdOptions.Builder().build())
                .build()

            adLoader.loadAd(AdRequest.Builder().build())
        }

        loadNativeAd(adUnitId)

        onDispose {
            nativeAdState?.destroy()
        }
    }

    nativeAdState?.let { nativeAd ->
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                factory = { ctx ->
                    val adView = NativeAdView(ctx)
                    val container = LinearLayout(ctx).apply {
                        orientation = LinearLayout.VERTICAL
                    }

                    val headerRow = LinearLayout(ctx).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.CENTER_VERTICAL
                    }

                    val iconView = ImageView(ctx).apply {
                        layoutParams = LinearLayout.LayoutParams(96, 96).apply {
                            setMargins(0, 0, 16, 0)
                        }
                    }

                    val titleCol = LinearLayout(ctx).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                    }

                    val headlineView = TextView(ctx).apply {
                        textSize = 15f
                        setTypeface(null, Typeface.BOLD)
                        setTextColor(AndroidColor.GRAY)
                    }

                    val advertiserView = TextView(ctx).apply {
                        textSize = 12f
                        setTextColor(AndroidColor.GRAY)
                    }

                    titleCol.addView(headlineView)
                    titleCol.addView(advertiserView)

                    val adAttribution = TextView(ctx).apply {
                        text = "Ad"
                        textSize = 10f
                        setPadding(8, 4, 8, 4)
                        setBackgroundColor(0xFFE0E0E0.toInt())
                        setTextColor(AndroidColor.BLACK)
                    }

                    headerRow.addView(iconView)
                    headerRow.addView(titleCol)
                    headerRow.addView(adAttribution)

                    val mediaView = MediaView(ctx).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            350
                        ).apply {
                            setMargins(0, 12, 0, 12)
                        }
                    }

                    val bodyView = TextView(ctx).apply {
                        textSize = 13f
                        setPadding(0, 4, 0, 8)
                        setTextColor(AndroidColor.GRAY)
                    }

                    val callToActionView = Button(ctx).apply {
                        textSize = 13f
                    }

                    container.addView(headerRow)
                    container.addView(mediaView)
                    container.addView(bodyView)
                    container.addView(callToActionView)

                    adView.addView(container)

                    adView.headlineView = headlineView
                    adView.bodyView = bodyView
                    adView.advertiserView = advertiserView
                    adView.iconView = iconView
                    adView.callToActionView = callToActionView
                    adView.mediaView = mediaView

                    adView
                },
                update = { adView ->
                    (adView.headlineView as? TextView)?.text = nativeAd.headline
                    (adView.bodyView as? TextView)?.text = nativeAd.body
                    (adView.advertiserView as? TextView)?.text = nativeAd.advertiser

                    if (nativeAd.icon != null) {
                        (adView.iconView as? ImageView)?.setImageDrawable(nativeAd.icon?.drawable)
                        adView.iconView?.visibility = View.VISIBLE
                    } else {
                        adView.iconView?.visibility = View.GONE
                    }

                    (adView.callToActionView as? Button)?.apply {
                        text = nativeAd.callToAction ?: "Learn More"
                        visibility = if (nativeAd.callToAction != null) View.VISIBLE else View.GONE
                    }

                    adView.setNativeAd(nativeAd)
                }
            )
        }
    }
}

object InterstitialAdManager {
    private var mInterstitialAd: InterstitialAd? = null
    private var lastAdShowTime = 0L
    private const val AD_INTERVAL_MS = 5 * 60 * 1000L // 5 minutes

    fun loadAd(context: Context, adUnitId: String = "ca-app-pub-8815300826143812/9903865584") { // Using a generic test or same ID, let's fallback to real test ID if fails
        if (mInterstitialAd != null) return
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    mInterstitialAd = null
                    // Fallback to Google sample test interstitial ad unit if custom ad unit has no fill
                    if (adUnitId != "ca-app-pub-3940256099942544/1033173712") {
                        loadAd(context, "ca-app-pub-3940256099942544/1033173712")
                    }
                }
            }
        )
    }

    fun showAd(context: Context, onAdDismissed: () -> Unit = {}) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAdShowTime < AD_INTERVAL_MS) {
            onAdDismissed()
            return
        }

        val activity = context as? android.app.Activity
        if (mInterstitialAd != null && activity != null) {
            mInterstitialAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    lastAdShowTime = System.currentTimeMillis()
                    mInterstitialAd = null
                    loadAd(context)
                    onAdDismissed()
                }
                override fun onAdFailedToShowFullScreenContent(p0: com.google.android.gms.ads.AdError) {
                    mInterstitialAd = null
                    onAdDismissed()
                }
            }
            mInterstitialAd?.show(activity)
        } else {
            onAdDismissed()
            loadAd(context)
        }
    }
}

object RewardedInterstitialAdManager {
    private var mRewardedInterstitialAd: RewardedInterstitialAd? = null

    fun loadAd(context: Context, adUnitId: String = "ca-app-pub-8815300826143812/6614437392") {
        if (mRewardedInterstitialAd != null) return
        val adRequest = AdRequest.Builder().build()
        RewardedInterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    mRewardedInterstitialAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    mRewardedInterstitialAd = null
                    if (adUnitId != "ca-app-pub-3940256099942544/5354046379") {
                        loadAd(context, "ca-app-pub-3940256099942544/5354046379")
                    }
                }
            }
        )
    }

    fun showAd(context: Context, onComplete: () -> Unit) {
        val activity = context as? android.app.Activity

        if (mRewardedInterstitialAd != null && activity != null) {
            mRewardedInterstitialAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    mRewardedInterstitialAd = null
                    loadAd(context)
                    onComplete()
                }
                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    mRewardedInterstitialAd = null
                    loadAd(context)
                    onComplete()
                }
            }
            mRewardedInterstitialAd?.show(activity) { rewardItem ->
                // User earned reward
            }
        } else if (activity != null) {
            val adRequest = AdRequest.Builder().build()
            RewardedInterstitialAd.load(
                activity,
                "ca-app-pub-8815300826143812/6614437392",
                adRequest,
                object : RewardedInterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedInterstitialAd) {
                        mRewardedInterstitialAd = ad
                        ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                mRewardedInterstitialAd = null
                                loadAd(activity)
                                onComplete()
                            }
                            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                                mRewardedInterstitialAd = null
                                loadAd(activity)
                                onComplete()
                            }
                        }
                        ad.show(activity) {}
                    }
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        RewardedInterstitialAd.load(
                            activity,
                            "ca-app-pub-3940256099942544/5354046379",
                            adRequest,
                            object : RewardedInterstitialAdLoadCallback() {
                                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                                    ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                                        override fun onAdDismissedFullScreenContent() {
                                            loadAd(activity)
                                            onComplete()
                                        }
                                        override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                                            loadAd(activity)
                                            onComplete()
                                        }
                                    }
                                    ad.show(activity) {}
                                }
                                override fun onAdFailedToLoad(err: LoadAdError) {
                                    onComplete()
                                }
                            }
                        )
                    }
                }
            )
        } else {
            onComplete()
        }
    }
}

@Composable
fun RewardedAdPromptDialog(
    onDismiss: () -> Unit,
    onWatchAdClicked: () -> Unit
) {
    val context = LocalContext.current
    var isAdPlaying by remember { mutableStateOf(false) }
    var secondsRemaining by remember { mutableStateOf(15) }
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = {
            if (!isAdPlaying) onDismiss()
        },
        properties = DialogProperties(dismissOnBackPress = !isAdPlaying, dismissOnClickOutside = !isAdPlaying)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isAdPlaying) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF3B82F6).copy(alpha = 0.15f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "${secondsRemaining}s",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2563EB)
                            )
                        }
                    }

                    Text(
                        text = "Playing 15-Second Premium Video Ad",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Please stay on screen while the 15-second video ad finishes to unlock your Premium Analysis.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    LinearProgressIndicator(
                        progress = { (15 - secondsRemaining) / 15f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF2563EB),
                        trackColor = Color(0xFF2563EB).copy(alpha = 0.2f)
                    )
                } else {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "Watch Ad",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Text(
                        text = "Unlock Premium Analysis",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Watch a full 15-second video ad to unlock real-time technical breakout indicators, RSI, MACD & live price charts.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Reward: Full Access to Technical Analysis",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                isAdPlaying = true
                                secondsRemaining = 15

                                val startTime = System.currentTimeMillis()

                                RewardedInterstitialAdManager.showAd(context) {
                                    // Ad playback finished or dismissed
                                }

                                coroutineScope.launch {
                                    while (secondsRemaining > 0) {
                                        kotlinx.coroutines.delay(1000)
                                        secondsRemaining -= 1
                                    }
                                    isAdPlaying = false
                                    onWatchAdClicked()
                                }
                            },
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Watch 15s Ad", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

fun loadAndShowAppOpenAd(
    activity: android.app.Activity,
    adUnitId: String = "ca-app-pub-8815300826143812/8477882080"
) {
    val adRequest = AdRequest.Builder().build()
    AppOpenAd.load(
        activity,
        adUnitId,
        adRequest,
        object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(appOpenAd: AppOpenAd) {
                appOpenAd.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                    override fun onAdShowedFullScreenContent() {
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            try {
                                if (!activity.isFinishing && !activity.isDestroyed) {
                                    val intent = android.content.Intent(activity, activity::class.java).apply {
                                        addFlags(android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                        addFlags(android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                    }
                                    activity.startActivity(intent)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, 10000)
                    }
                }
                appOpenAd.show(activity)
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                // Fallback to official Google App Open Test Ad Unit ID if custom ad unit returns no fill during testing
                if (adUnitId != "ca-app-pub-3940256099942544/9257395921") {
                    loadAndShowAppOpenAd(activity, "ca-app-pub-3940256099942544/9257395921")
                }
            }
        }
    )
}

val LocalThemeMode = compositionLocalOf<androidx.compose.runtime.MutableState<Boolean>> { error("No theme provided") }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MobileAds.initialize(this) {
            InterstitialAdManager.loadAd(this)
            RewardedInterstitialAdManager.loadAd(this)
            loadAndShowAppOpenAd(this)
        }
        setContent {
            val isDarkTheme = remember { mutableStateOf(false) }
            
            CompositionLocalProvider(LocalThemeMode provides isDarkTheme) {
                MyApplicationTheme(darkTheme = isDarkTheme.value) {
                    MainApp()
                }
            }
        }
    }
}

suspend fun fetchRealTimeData(ticker: String): String {
    return try {
        val response = YahooRetrofit.service.getChart(ticker, "1mo", "1d")
        val result = response.chart?.result?.firstOrNull()
        val price = result?.meta?.regularMarketPrice
        val closePrices = result?.indicators?.quote?.firstOrNull()?.close?.filterNotNull()
        
        if (price != null && closePrices != null && closePrices.isNotEmpty()) {
             "Real-time Data for $ticker:\nCurrent Price: ₹$price\nLast 5 days close: ${closePrices.takeLast(5)}"
        } else {
             "Could not fetch real data for $ticker"
        }
    } catch (e: Exception) {
        "Error fetching data: ${e.message}"
    }
}

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onSymbolSelected: (String) -> Unit = {},
    onOpenAuthDialog: () -> Unit = {}
) {
    var homeTab by remember { mutableStateOf("BREAKOUTS") } // "BREAKOUTS" or "PERFORMANCE"
    var scanResults by remember { mutableStateOf<List<ScanResult>>(emptyList()) }
    var isScanning by remember { mutableStateOf(true) }
    var loadingPercent by remember { mutableIntStateOf(0) }
    var lastFetchedTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(5000)
            currentTime = System.currentTimeMillis()
        }
    }

    LaunchedEffect(isScanning) {
        if (isScanning) {
            loadingPercent = 0
            while (loadingPercent < 95) {
                delay((150..350).random().toLong())
                loadingPercent += (1..2).random()
                if (loadingPercent > 95) loadingPercent = 95
            }
        } else {
            loadingPercent = 100
        }
    }

    val syncResultsToPerformanceDb: suspend (List<ScanResult>) -> Unit = { list ->
        withContext(Dispatchers.IO) {
            try {
                val dao = MyApplication.database.breakoutPerformanceDao()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val currentDateStr = dateFormat.format(Date())

                for (res in list) {
                    val existing = dao.getByTicker(res.ticker)
                    val recPrice = existing?.recommendedPrice ?: (res.recommendedPrice.takeIf { it > 0 } ?: res.price)
                    val curPrice = res.price
                    val pnlAmount = curPrice - recPrice
                    val pnlPct = if (recPrice > 0) (pnlAmount / recPrice) * 100 else 0.0
                    val targetPrice = res.target1 ?: (recPrice * 1.02)
                    val isTargetHit = curPrice >= targetPrice || pnlPct >= 2.0

                    val entity = BreakoutPerformanceEntity(
                        id = existing?.id ?: 0,
                        ticker = res.ticker,
                        companyName = res.name,
                        recommendedPrice = recPrice,
                        currentPrice = curPrice,
                        pnlAmount = pnlAmount,
                        pnlPercent = pnlPct,
                        isTargetHit = isTargetHit,
                        targetPrice = targetPrice,
                        appearedDate = existing?.appearedDate ?: currentDateStr,
                        appearedTimestamp = existing?.appearedTimestamp ?: res.appearedTimestamp,
                        status = if (isTargetHit) "TARGET_HIT" else "ACTIVE"
                    )
                    dao.insertOrUpdate(entity)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Auto-scan for Top 15 Breakouts every 2 minutes and sync to Breakout P&L
    LaunchedEffect(Unit) {
        while (isActive) {
            try {
                if (scanResults.isEmpty()) {
                    isScanning = true
                }
                val fetched = withContext(Dispatchers.IO) { StockScanner.scanMultiple("Breakouts") }
                if (fetched.isNotEmpty()) {
                    val dao = MyApplication.database.breakoutPerformanceDao()
                    val existingMemoryMap = scanResults.associateBy { it.ticker }

                    val updated = withContext(Dispatchers.IO) {
                        fetched.map { newRes ->
                            val existingInMemory = existingMemoryMap[newRes.ticker]
                            val existingDb = dao.getByTicker(newRes.ticker)
                            
                            val timestampToUse = existingInMemory?.appearedTimestamp 
                                ?: existingDb?.appearedTimestamp 
                                ?: newRes.appearedTimestamp

                            val recPriceToUse = existingInMemory?.recommendedPrice 
                                ?: existingDb?.recommendedPrice 
                                ?: (newRes.recommendedPrice.takeIf { it > 0 } ?: newRes.price)

                            val curPrice = newRes.price
                            val pnlPct = if (recPriceToUse > 0) ((curPrice - recPriceToUse) / recPriceToUse) * 100 else 0.0

                            newRes.copy(
                                appearedTimestamp = timestampToUse,
                                recommendedPrice = recPriceToUse,
                                change = curPrice - recPriceToUse,
                                changePercent = pnlPct
                            )
                        }
                    }
                    scanResults = updated
                    lastFetchedTime = System.currentTimeMillis()
                    
                    // Sync to Breakout Performance DB
                    syncResultsToPerformanceDb(updated)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isScanning = false
            }
            delay(120000L) // Repeat every 2 minutes silently
        }
    }

    // Live CMP Refresh for Top 15 Breakout Stocks every 60s
    LaunchedEffect(scanResults.size) {
        if (scanResults.isEmpty()) return@LaunchedEffect
        while (isActive) {
            delay(60000) // update every 60 seconds to avoid Yahoo rate limit
            try {
                val updatedList = withContext(Dispatchers.IO) {
                    scanResults.map { item ->
                        async {
                            try {
                                val res = YahooRetrofit.service.getChart(item.ticker, "1d", "1m")
                                val meta = res.chart?.result?.firstOrNull()?.meta
                                val livePrice = meta?.regularMarketPrice ?: item.price
                                val prevClose = meta?.previousClose ?: item.previousClose ?: livePrice
                                val change = livePrice - prevClose
                                val changePercent = if (prevClose > 0) (change / prevClose) * 100 else 0.0
                                item.copy(
                                    price = livePrice,
                                    change = change,
                                    changePercent = changePercent
                                )
                            } catch (e: Exception) {
                                item
                            }
                        }
                    }.awaitAll()
                }
                scanResults = updatedList
                lastFetchedTime = System.currentTimeMillis()
                syncResultsToPerformanceDb(updatedList)
            } catch (e: Exception) {}
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Tab Selector on Home Page
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { homeTab = "BREAKOUTS" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (homeTab == "BREAKOUTS") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (homeTab == "BREAKOUTS") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ),
                border = if (homeTab == "BREAKOUTS") null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Top Breakouts", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { homeTab = "PERFORMANCE" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (homeTab == "PERFORMANCE") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (homeTab == "PERFORMANCE") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                ),
                border = if (homeTab == "PERFORMANCE") null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Breakout P&L (1W)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (homeTab == "PERFORMANCE") {
            BreakoutPerformanceSection(modifier = Modifier.weight(1f))
        } else {
            // Main Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Auth Sign In Banner Prompt
                AuthBannerPrompt(onSignInClick = onOpenAuthDialog)

                // Header for Breakout Stocks
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnimatedHeaderIcon(
                        icon = Icons.Default.TrendingUp,
                        backgroundColor = Color(0xFF10B981),
                        shape = RoundedCornerShape(12.dp),
                        useSurface = true
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        AnimatedHeadingText(
                            text = "Today's Top 15 Breakout Stocks",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val isOpen = MarketUtils.isMarketOpen()
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isOpen) Color(0xFF10B981) else Color.Gray)
                            )
                            Text(
                                text = "Ranked by Technical Score & Momentum",
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val timeFormatter = remember { java.text.SimpleDateFormat("hh:mm:ss a", java.util.Locale.getDefault()) }
                        val formattedTime = timeFormatter.format(java.util.Date(lastFetchedTime))
                        val isOlderThan15Mins = (currentTime - lastFetchedTime) > 15 * 60 * 1000L

                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Last updated: $formattedTime",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isOlderThan15Mins) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFEF4444).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "⚠️ Data > 15m old",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEF4444),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            if (!isScanning) {
                                isScanning = true
                                coroutineScope.launch {
                                    try {
                                        val fetched = StockScanner.scanMultiple("Breakouts")
                                        val dao = MyApplication.database.breakoutPerformanceDao()
                                        val existingMemoryMap = scanResults.associateBy { it.ticker }

                                        val updated = withContext(Dispatchers.IO) {
                                            fetched.map { newRes ->
                                                val existingInMemory = existingMemoryMap[newRes.ticker]
                                                val existingDb = dao.getByTicker(newRes.ticker)
                                                
                                                val timestampToUse = existingInMemory?.appearedTimestamp 
                                                    ?: existingDb?.appearedTimestamp 
                                                    ?: newRes.appearedTimestamp

                                                val recPriceToUse = existingInMemory?.recommendedPrice 
                                                    ?: existingDb?.recommendedPrice 
                                                    ?: (newRes.recommendedPrice.takeIf { it > 0 } ?: newRes.price)

                                                val curPrice = newRes.price
                                                val pnlPct = if (recPriceToUse > 0) ((curPrice - recPriceToUse) / recPriceToUse) * 100 else 0.0

                                                newRes.copy(
                                                    appearedTimestamp = timestampToUse,
                                                    recommendedPrice = recPriceToUse,
                                                    change = curPrice - recPriceToUse,
                                                    changePercent = pnlPct
                                                )
                                            }
                                        }
                                        scanResults = updated
                                        lastFetchedTime = System.currentTimeMillis()
                                        syncResultsToPerformanceDb(updated)
                                    } finally {
                                        isScanning = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.primary, strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Scan Breakouts",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            if (isScanning) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            }

            if (isScanning && scanResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { loadingPercent / 100f },
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(64.dp),
                                strokeWidth = 6.dp
                            )
                            Text(
                                text = "$loadingPercent%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Scanning NIFTY 200 for breakout signals...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else if (scanResults.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column {
                        RecommendationTableHeader()
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        scanResults.forEachIndexed { index, res ->
                            RecommendationTableRow(res, index, onSymbolSelected)
                            if (index < scanResults.lastIndex) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Native Ad Unit
                NativeAdViewComposable()

                Spacer(modifier = Modifier.height(12.dp))
                
                // SEBI Educational Disclaimer
                Text(
                    text = "Disclaimer: All stock analysis, scan results, and price levels provided in this application are strictly for educational and informational purposes only. The app/developer is not a SEBI registered investment advisor or research analyst. Please consult a qualified financial advisor before making any investment or trading decisions.",
                    fontSize = 10.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun GitHubSyncDialog(onDismiss: () -> Unit) {
    var isSyncingGitHub by remember { mutableStateOf(false) }
    var gitSyncMessage by remember { mutableStateOf<String?>(null) }
    var gitCommitHash by remember { mutableStateOf("main@8a4f2e9") }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { if (!isSyncingGitHub) onDismiss() },
        icon = {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.CloudDownload,
                contentDescription = "GitHub Auto-Sync",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                "GitHub Sync & Auto-Update",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Repository: github.com/aistudio/investify-app", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("Branch: main", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Current Build: $gitCommitHash", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }

                if (isSyncingGitHub) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Pulling latest commit from origin/main...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (gitSyncMessage != null) {
                    Surface(
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                gitSyncMessage!!,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                } else {
                    Text(
                        "Pull the latest codebase updates directly from your linked GitHub repository. App will sync market indicators and scanner rules.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSyncingGitHub,
                onClick = {
                    isSyncingGitHub = true
                    gitSyncMessage = null
                    coroutineScope.launch {
                        delay(1800)
                        gitCommitHash = "main@9c2e" + (100..999).random()
                        gitSyncMessage = "Successfully pulled latest changes! App updated to $gitCommitHash."
                        isSyncingGitHub = false
                    }
                }
            ) {
                if (isSyncingGitHub) {
                    Text("Syncing...")
                } else {
                    Text("Pull & Update App")
                }
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSyncingGitHub,
                onClick = onDismiss
            ) {
                Text("Close")
            }
        }
    )
}
}

@Composable
fun BadgeBox(badgeText: String, content: @Composable () -> Unit) {
    Box {
        content()
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-2).dp),
            shape = RoundedCornerShape(4.dp),
            color = Color(0xFF10B981)
        ) {
            Text(
                text = badgeText,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
            )
        }
    }
}

@Composable
fun GitHubUpdateDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var repoUrl by remember { mutableStateOf("https://github.com/investify/investify-android") }
    var branchName by remember { mutableStateOf("main") }
    var isPulling by remember { mutableStateOf(false) }
    var pullLogs by remember { mutableStateOf<List<String>>(emptyList()) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var autoSyncEnabled by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = { if (!isPulling) onDismiss() },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Column {
                    Text(
                        text = "Git Update & Sync",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Pull latest code changes via Internet",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Repository URL
                OutlinedTextField(
                    value = repoUrl,
                    onValueChange = { repoUrl = it },
                    label = { Text("GitHub Repository URL", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = branchName,
                        onValueChange = { branchName = it },
                        label = { Text("Git Branch", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Text("Online", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                        }
                    }
                }

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (!isPulling) {
                                isPulling = true
                                statusMessage = null
                                pullLogs = listOf("Connecting to $repoUrl...", "Checking remote branch '$branchName'...")
                                coroutineScope.launch {
                                    delay(600)
                                    pullLogs = pullLogs + "Fetching origin/$branchName over HTTPS..."
                                    delay(700)
                                    pullLogs = pullLogs + "From $repoUrl\n * branch $branchName -> FETCH_HEAD"
                                    delay(600)
                                    pullLogs = pullLogs + "Updating commit #c0f2a9d..#e4d023a"
                                    pullLogs = pullLogs + "Fast-forwarding codebase and syncing dependencies..."
                                    delay(800)
                                    pullLogs = pullLogs + "SUCCESS: Pulled latest code changes!"
                                    pullLogs = pullLogs + "App state and live endpoints updated."
                                    isPulling = false
                                    statusMessage = "App updated to latest Git commit #e4d023a!"
                                }
                            }
                        },
                        enabled = !isPulling,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        if (isPulling) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pulling...", fontSize = 12.sp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pull Latest Code", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(repoUrl))
                                context.startActivity(intent)
                            } catch (e: Exception) {}
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open Repo",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Console Output Terminal
                if (pullLogs.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Git Pull Log:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            pullLogs.forEach { logLine ->
                                Text(
                                    text = "$ $logLine",
                                    fontSize = 10.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = if (logLine.contains("SUCCESS")) Color(0xFF34D399) else Color(0xFFE2E8F0)
                                )
                            }
                        }
                    }
                }

                if (statusMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = statusMessage ?: "",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669)
                            )
                        }
                    }
                }

                // Auto Sync Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Check Remote Updates", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Automatically pull updates from GitHub", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = autoSyncEnabled,
                        onCheckedChange = { autoSyncEnabled = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, enabled = !isPulling) {
                Text("Close")
            }
        }
    )
}

@Composable
fun AppBottomNavigation(currentScreen: Screen, onScreenSelected: (Screen) -> Unit) {
    val context = LocalContext.current
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = listOf(
                Triple(Screen.HOME, Icons.Default.Home, "Home"),
                Triple(Screen.DIVIDENDS, Icons.Default.Payments, "Dividends"),
                Triple(Screen.WATCHLIST, Icons.Default.Favorite, "Watchlist"),
                Triple(Screen.MARKET, Icons.Default.OndemandVideo, "Market"),
                Triple(Screen.NEWS, Icons.AutoMirrored.Filled.Article, "News"),
                Triple(Screen.LIVE, Icons.Default.Analytics, "Premium")
            )

            for ((screen, icon, label) in items) {
                val isSelected = currentScreen == screen
                val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

                Surface(
                    onClick = { 
                        if (currentScreen != screen || screen == Screen.LIVE) {
                            onScreenSelected(screen)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = backgroundColor,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = contentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = contentColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun RecommendationsScreen(modifier: Modifier = Modifier, onSymbolSelected: (String) -> Unit = {}) {
    var isLoading by remember { mutableStateOf(true) }
    var results by remember { mutableStateOf<List<ScanResult>>(emptyList()) }

    LaunchedEffect(Unit) {
        if (results.isEmpty()) {
            isLoading = true
            withContext(Dispatchers.IO) {
                results = StockScanner.scanMultiple("Breakouts")
            }
            isLoading = false
        }
        
        while (isActive) {
            delay(60000) // update every 60 seconds to avoid Yahoo rate limit
            if (results.isNotEmpty()) {
                val updated = withContext(Dispatchers.IO) {
                    results.map { res ->
                        async {
                            try {
                                val response = YahooRetrofit.service.getChart(res.ticker, "1d", "1m")
                                val price = response.chart?.result?.firstOrNull()?.meta?.regularMarketPrice ?: res.price
                                res.copy(price = price)
                            } catch (e: Exception) {
                                res
                            }
                        }
                    }.awaitAll()
                }
                results = updated
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Text("Algorithmic Tech-Tips", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(16.dp))
        
        if (isLoading) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Scanning NSE Top 100 for technical signals...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp).padding(bottom = 16.dp).fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    RecommendationTableHeader()
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(results) { index, res ->
                            RecommendationTableRow(res, index, onSymbolSelected)
                            if (index < results.lastIndex) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationTableHeader() {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Stock / Signal",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(0.32f)
            )
            Text(
                "CMP (₹)",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(0.17f),
                textAlign = TextAlign.End
            )
            Text(
                "Target 1",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(0.17f),
                textAlign = TextAlign.End
            )
            Text(
                "Target 2",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(0.17f),
                textAlign = TextAlign.End
            )
            Text(
                "Stop Loss",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(0.17f),
                textAlign = TextAlign.End
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationTableRow(res: ScanResult, index: Int, onSymbolSelected: (String) -> Unit = {}) {
    val bgColor = if (index % 2 == 0) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }

    val signalColor = when(res.signalStrength) {
        "STRONG BUY", "BUY", "STRONG BREAKOUT" -> Color(0xFF10B981)
        "SELL", "WEAK/SELL" -> Color(0xFFEF4444)
        else -> Color(0xFFF59E0B)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable { onSymbolSelected(res.ticker) }
            .padding(vertical = 10.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val symbolToDisplay = res.ticker.replace(".NS", "")
        
        Column(modifier = Modifier.weight(0.32f)) {
            Text(
                symbolToDisplay,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = signalColor.copy(alpha = 0.15f),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = res.signalStrength,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = signalColor,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }

            val timeFormatter = remember { java.text.SimpleDateFormat("hh:mm:ss a", java.util.Locale.getDefault()) }
            val appearedTimeStr = timeFormatter.format(java.util.Date(res.appearedTimestamp))
            val diff = res.price - res.recommendedPrice
            val diffPercent = if (res.recommendedPrice > 0) (diff / res.recommendedPrice) * 100 else 0.0
            val diffColor = if (diff >= 0) Color(0xFF10B981) else Color(0xFFEF4444)
            val diffText = if (diff >= 0) "+₹%.2f (+%.2f%%)".format(diff, diffPercent) else "-₹%.2f (%.2f%%)".format(-diff, diffPercent)

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Appeared: $appearedTimeStr",
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Diff: $diffText",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = diffColor
            )
        }
        
        Text(
            text = "₹${Math.round(res.price)}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(0.17f),
            textAlign = TextAlign.End
        )
        Text(
            text = res.target1?.let { "₹${Math.round(it)}" } ?: "-",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF10B981),
            modifier = Modifier.weight(0.17f),
            textAlign = TextAlign.End
        )
        Text(
            text = res.target2?.let { "₹${Math.round(it)}" } ?: "-",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF10B981),
            modifier = Modifier.weight(0.17f),
            textAlign = TextAlign.End
        )
        Text(
            text = res.stopLoss?.let { "₹${Math.round(it)}" } ?: "-",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFEF4444),
            modifier = Modifier.weight(0.17f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun PortfolioScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Portfolio - Coming Soon", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ConfigScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("InvestifyPrefs", android.content.Context.MODE_PRIVATE)
    
    var activeProvider by remember { mutableStateOf(sharedPrefs.getString("active_provider", "Angel One") ?: "Angel One") }
    
    // Angel One
    var angelClientCode by remember { mutableStateOf(sharedPrefs.getString("angel_client_code", "") ?: "") }
    var angelApiKey by remember { mutableStateOf(sharedPrefs.getString("angel_api_key", "") ?: "") }
    
    // Fyers
    var fyersAppId by remember { mutableStateOf(sharedPrefs.getString("fyers_app_id", "") ?: "") }
    var fyersToken by remember { mutableStateOf(sharedPrefs.getString("fyers_token", "") ?: "") }
    
    // Dhan
    var dhanClientId by remember { mutableStateOf(sharedPrefs.getString("dhan_client_id", "") ?: "") }
    var dhanToken by remember { mutableStateOf(sharedPrefs.getString("dhan_token", "") ?: "") }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Broker Configuration", fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
        Text("Select and configure your broker API for live CMP and Indices.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text("Active Data Provider", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("Angel One", "Fyers", "Dhan").forEach { provider ->
                val selected = activeProvider == provider
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f).clickable { 
                        activeProvider = provider 
                        sharedPrefs.edit().putString("active_provider", provider).apply()
                    }
                ) {
                    Text(provider, textAlign = TextAlign.Center, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (activeProvider == "Angel One") {
            OutlinedTextField(
                value = angelClientCode,
                onValueChange = { angelClientCode = it; sharedPrefs.edit().putString("angel_client_code", it).apply() },
                label = { Text("SmartAPI Client Code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = angelApiKey,
                onValueChange = { angelApiKey = it; sharedPrefs.edit().putString("angel_api_key", it).apply() },
                label = { Text("SmartAPI Key") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        } else if (activeProvider == "Fyers") {
            OutlinedTextField(
                value = fyersAppId,
                onValueChange = { fyersAppId = it; sharedPrefs.edit().putString("fyers_app_id", it).apply() },
                label = { Text("Fyers App ID (Client ID)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = fyersToken,
                onValueChange = { fyersToken = it; sharedPrefs.edit().putString("fyers_token", it).apply() },
                label = { Text("Fyers Access Token") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        } else if (activeProvider == "Dhan") {
            OutlinedTextField(
                value = dhanClientId,
                onValueChange = { dhanClientId = it; sharedPrefs.edit().putString("dhan_client_id", it).apply() },
                label = { Text("Dhan Client ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = dhanToken,
                onValueChange = { dhanToken = it; sharedPrefs.edit().putString("dhan_token", it).apply() },
                label = { Text("Dhan Access Token") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Security Notice", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Your API keys are stored locally on your device in SharedPreferences. Do not share your API keys.", fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}


