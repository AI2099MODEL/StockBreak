import re

with open('app/src/main/java/com/example/WatchlistScreen.kt', 'r') as f:
    content = f.read()

# Add viewmodel import if needed
if 'import androidx.lifecycle.viewmodel.compose.viewModel' not in content:
    content = content.replace('import androidx.compose.ui.platform.LocalContext\n', 'import androidx.compose.ui.platform.LocalContext\nimport androidx.lifecycle.viewmodel.compose.viewModel\n')

new_screen = """@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun WatchlistScreen(
    modifier: Modifier = Modifier,
    viewModel: WatchlistViewModel = viewModel(
        factory = WatchlistViewModel.Factory(WatchlistRepository(MyApplication.database.priceAlertDao()))
    )
) {
    val context = LocalContext.current
    
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    val currentPrices by viewModel.currentPrices.collectAsStateWithLifecycle()
    
    var showDialog by remember { mutableStateOf(false) }
    var ticker by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }

    // Start background worker if not already
    LaunchedEffect(Unit) {
        WorkerUtils.schedulePriceAlertWorker(context)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> }
    )
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val marketNews = listOf(
        "Nifty 50 hits new all-time high, crosses 25,000 mark",
        "Sensex surges over 800 points, banking stocks lead the rally",
        "FIIs buy equities worth ₹3,500 crore in the latest session",
        "RBI keeps repo rate unchanged at 6.5%",
        "IT stocks gain momentum ahead of Q1 earnings",
        "Rupee appreciates 15 paise against US dollar"
    ).joinToString("   •   ")

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = "Add Alert")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TopAppBar(
                title = { Text("Watchlist & Alerts", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
            
            // News Ticker
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "LIVE NEWS",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = marketNews,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 14.sp,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                    )
                }
            }
            
            if (alerts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No price alerts set.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    items(alerts) { alert ->
                        val cmp = currentPrices[alert.ticker]
                        val isTargetReached = if (cmp != null) {
                            if (alert.targetPrice > 0) cmp >= alert.targetPrice else cmp <= alert.targetPrice
                        } else false

                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(alert.ticker, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Target: ₹${alert.targetPrice}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(16.dp))
                                        if (cmp != null) {
                                            Text("CMP: ₹$cmp", color = if (isTargetReached) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        } else {
                                            Text("CMP: Loading...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                        }
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = alert.isAlertActive,
                                        onCheckedChange = { active ->
                                            viewModel.updateAlertActiveStatus(alert, active)
                                        }
                                    )
                                    IconButton(onClick = {
                                        viewModel.deleteAlert(alert.id)
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add Price Alert") },
            text = {
                Column {
                    OutlinedTextField(
                        value = ticker,
                        onValueChange = { ticker = it.uppercase() },
                        label = { Text("Symbol (e.g. RELIANCE.NS)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = target,
                        onValueChange = { target = it },
                        label = { Text("Target Price (₹)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val targetPrice = target.toDoubleOrNull()
                    if (ticker.isNotBlank() && targetPrice != null) {
                        viewModel.addAlert(ticker, targetPrice)
                        showDialog = false
                        ticker = ""
                        target = ""
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}"""

pattern = re.compile(r'@OptIn\(ExperimentalMaterial3Api::class.*?\)\s*@Composable\s*fun WatchlistScreen.*?^}', re.MULTILINE | re.DOTALL)
content = pattern.sub(new_screen, content)

with open('app/src/main/java/com/example/WatchlistScreen.kt', 'w') as f:
    f.write(content)
