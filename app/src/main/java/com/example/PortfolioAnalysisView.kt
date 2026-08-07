package com.example

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioAnalysisView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var inputMode by remember { mutableStateOf(0) } // 0: Upload File, 1: Paste Text, 2: Sample Portfolios
    var portfolioText by remember { mutableStateOf("") }
    var uploadedFileName by remember { mutableStateOf<String?>(null) }
    var uploadedFileSize by remember { mutableStateOf<String?>(null) }

    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<PortfolioAnalysisResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }



    // File Picker for CSV, XLS, XLSX, PDF, TXT, JSON
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val name = getFileNameFromUri(context, uri) ?: "portfolio_file"
                val text = readTextFromUri(context, uri)
                uploadedFileName = name
                uploadedFileSize = "${text.length} characters"
                portfolioText = text
                inputMode = 0
            } catch (e: Exception) {
                errorMessage = "Failed to read file: ${e.message}"
            }
        }
    }

    // Sample Portfolios
    val samplePortfolios = listOf(
        "1. Core Bluechip Indian Portfolio" to """
            RELIANCE.NS - 50 shares @ ₹2,850 (20%)
            HDFCBANK.NS - 100 shares @ ₹1,520 (22%)
            TATAMOTORS.NS - 150 shares @ ₹980 (18%)
            TCS.NS - 30 shares @ ₹3,950 (15%)
            SBIN.NS - 120 shares @ ₹820 (13%)
            ICICIBANK.NS - 80 shares @ ₹1,150 (12%)
        """.trimIndent(),
        "2. Growth Tech & EV Focus" to """
            NVDA - 15 shares @ $125 (25%)
            TATAMOTORS.NS - 200 shares @ ₹980 (20%)
            ZOMATO.NS - 500 shares @ ₹220 (18%)
            PERSISTENT.NS - 25 shares @ ₹4,100 (15%)
            TATAELXSI.NS - 20 shares @ ₹7,200 (12%)
            PAYTM.NS - 100 shares @ ₹450 (10%)
        """.trimIndent(),
        "3. Dividend & Infrastructure" to """
            L&T.NS - 40 shares @ ₹3,600 (22%)
            NTPC.NS - 300 shares @ ₹390 (20%)
            COALINDIA.NS - 250 shares @ ₹480 (18%)
            ITC.NS - 200 shares @ ₹470 (15%)
            BPCL.NS - 300 shares @ ₹310 (13%)
            IRFC.NS - 500 shares @ ₹175 (12%)
        """.trimIndent()
    )

    fun runPortfolioAnalysis() {
        if (portfolioText.isBlank()) {
            errorMessage = "Please upload a file, paste portfolio text, or pick a sample portfolio!"
            return
        }
        errorMessage = null
        isAnalyzing = true
        coroutineScope.launch {
            try {
                val res = GeminiPortfolioAnalyzer.analyzePortfolio(portfolioText, uploadedFileName)
                analysisResult = res
            } catch (e: Exception) {
                errorMessage = "Analysis error: ${e.message}"
            } finally {
                isAnalyzing = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // PORTFOLIO UPLOAD & INPUT WORKSPACE
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(6.dp)
                                .size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "AI Portfolio Scanner & Rebalancer",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Upload CSV, XLS, PDF, TXT or select sample portfolio",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Input Mode Tab Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = inputMode == 0,
                        onClick = { inputMode = 0 },
                        label = { Text("Upload File", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        leadingIcon = { Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        modifier = Modifier.height(32.dp)
                    )
                    FilterChip(
                        selected = inputMode == 1,
                        onClick = { inputMode = 1 },
                        label = { Text("Paste Text", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        leadingIcon = { Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        modifier = Modifier.height(32.dp)
                    )
                    FilterChip(
                        selected = inputMode == 2,
                        onClick = { inputMode = 2 },
                        label = { Text("Samples", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp)) },
                        modifier = Modifier.height(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mode 0: Upload File
                if (inputMode == 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(14.dp)
                            )
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(14.dp)
                            )
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (uploadedFileName != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(uploadedFileName ?: "", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(uploadedFileSize ?: "", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = {
                                    uploadedFileName = null
                                    uploadedFileSize = null
                                    portfolioText = ""
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear file", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Upload",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Supported Formats: CSV, XLS, XLSX, PDF, TXT, JSON", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { filePickerLauncher.launch("*/*") },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Choose Portfolio File", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Mode 1: Paste Text
                if (inputMode == 1) {
                    OutlinedTextField(
                        value = portfolioText,
                        onValueChange = { portfolioText = it },
                        placeholder = { Text("Paste your holdings e.g.\nRELIANCE - 50 shares @ 2800\nHDFCBANK - 100 shares @ 1500\nTATAMOTORS - 150 shares @ 980") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                // Mode 2: Sample Portfolios
                if (inputMode == 2) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Pick a pre-configured sample portfolio to test:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        samplePortfolios.forEach { (title, text) ->
                            val isSelected = portfolioText == text
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.5.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        portfolioText = text
                                        uploadedFileName = title
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${text.lines().size} Stock Holdings Included",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primary
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color.White,
                                                modifier = Modifier.padding(4.dp).size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Full Holdings Visible Preview Card
                        if (portfolioText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF0F172A), // High contrast dark slate container for sample text
                                border = BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                            Text(
                                                text = "FULL HOLDINGS PREVIEW (${uploadedFileName ?: "Sample Portfolio"})",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF38BDF8)
                                            )
                                        }
                                        TextButton(
                                            onClick = { inputMode = 1 },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                            modifier = Modifier.height(26.dp)
                                        ) {
                                            Text("Edit in Text Mode", fontSize = 10.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    HorizontalDivider(color = Color(0xFF1E293B))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = portfolioText,
                                        fontSize = 11.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        color = Color(0xFFF8FAFC),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage ?: "", fontSize = 11.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Submit Action Button
                Button(
                    onClick = {
                        runPortfolioAnalysis()
                    },
                    enabled = !isAnalyzing && portfolioText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Deep Analysis Ongoing...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyze Portfolio & Get Recommendations", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // Bottom SEBI / Financial Disclaimer Box for Portfolio Analysis
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "Disclaimer", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Column {
                            Text("FINANCIAL & SEBI DISCLAIMER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "All AI-generated portfolio insights, health scores, and holding recommendations are strictly for educational and analytical purposes. StockBreak is not a SEBI-registered investment advisor. Please consult a certified financial advisor before executing trades.",
                                fontSize = 10.sp,
                                lineHeight = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // ANALYSIS RESULTS DISPLAY
        analysisResult?.let { res ->
            // Overall Portfolio Health Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Portfolio Health Score", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("${res.overallHealthScore}", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = getScoreColor(res.overallHealthScore))
                                Text("/100", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp, start = 2.dp))
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = getRiskColor(res.riskRating).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, getRiskColor(res.riskRating))
                        ) {
                            Text(
                                text = res.riskRating,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = getRiskColor(res.riskRating),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Asset Allocation Summary Box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.DonutSmall, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Column {
                                Text("Asset & Sector Allocation:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(res.assetAllocationSummary, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }

            // Strengths & Risks Breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Strengths Card
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFF86EFAC))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF166534), modifier = Modifier.size(16.dp))
                            Text("Top Strengths", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        res.topStrengths.forEach { item ->
                            Text("• $item", fontSize = 10.sp, color = Color(0xFF15803D), lineHeight = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }

                // Risks & Gaps Card
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFECACA))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.ReportProblem, contentDescription = null, tint = Color(0xFF991B1B), modifier = Modifier.size(16.dp))
                            Text("Gaps & Concentration", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        res.keyRisksAndGaps.forEach { item ->
                            Text("• $item", fontSize = 10.sp, color = Color(0xFFB91C1C), lineHeight = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }

            // Holding-by-Holding AI Recommendations Table
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(Icons.Default.FormatListNumbered, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Text("Holding Recommendations", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    res.holdingRecommendations.forEachIndexed { index, item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.assetName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text("Weight: ${item.allocationPct}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = getActionBgColor(item.action)
                                    ) {
                                        Text(
                                            text = item.action,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = getActionTextColor(item.action),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(item.reasoning, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 15.sp)
                            }
                        }
                    }
                }
            }

            // Strategic Action Plan
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 10.dp)
                    ) {
                        Icon(Icons.Default.AltRoute, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Text("Rebalancing Action Plan", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    res.strategicActionPlan.forEachIndexed { idx, step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("${idx + 1}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Text(step, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 16.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // FOOTER SEBI DISCLAIMER BOX
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Disclaimer", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Text(
                        text = res.disclaimerText,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// Helpers
private fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    result = it.getString(index)
                }
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result
}

private fun readTextFromUri(context: Context, uri: Uri): String {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        inputStream?.bufferedReader()?.use { it.readText() } ?: ""
    } catch (e: Exception) {
        "Error reading file stream: ${e.message}"
    }
}

private fun getScoreColor(score: Int): Color {
    return when {
        score >= 80 -> Color(0xFF10B981)
        score >= 60 -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }
}

private fun getRiskColor(risk: String): Color {
    val upper = risk.uppercase()
    return when {
        upper.contains("CONSERVATIVE") || upper.contains("LOW") -> Color(0xFF10B981)
        upper.contains("MODERATE") || upper.contains("BALANCED") -> Color(0xFF3B82F6)
        else -> Color(0xFFF59E0B)
    }
}

private fun getActionBgColor(action: String): Color {
    val upper = action.uppercase()
    return when {
        upper.contains("BUY") || upper.contains("ACCUMULATE") -> Color(0xFFDCFCE7)
        upper.contains("EXIT") || upper.contains("SELL") -> Color(0xFFFEE2E2)
        upper.contains("TRIM") || upper.contains("REDUCE") -> Color(0xFFFEF3C7)
        upper.contains("REBALANCE") -> Color(0xFFF3E8FF)
        else -> Color(0xFFDBEAFE)
    }
}

private fun getActionTextColor(action: String): Color {
    val upper = action.uppercase()
    return when {
        upper.contains("BUY") || upper.contains("ACCUMULATE") -> Color(0xFF15803D)
        upper.contains("EXIT") || upper.contains("SELL") -> Color(0xFFB91C1C)
        upper.contains("TRIM") || upper.contains("REDUCE") -> Color(0xFFB45309)
        upper.contains("REBALANCE") -> Color(0xFF6B21A8)
        else -> Color(0xFF1D4ED8)
    }
}
