import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

config_screen_code = """
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
"""

content = re.sub(r'@Composable\nfun ConfigScreen\(modifier: Modifier = Modifier\) \{\n    Box\(modifier = modifier\.fillMaxSize\(\), contentAlignment = Alignment\.Center\) \{\n        Text\("Config - Coming Soon".*?\n    \}\n\}', config_screen_code, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
