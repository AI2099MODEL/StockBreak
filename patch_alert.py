import re

with open('app/src/main/java/com/example/LiveScreen.kt', 'r') as f:
    content = f.read()

# Add new fields to LiveStock
old_livestock = """data class LiveStock(
    val symbol: String,
    val name: String,
    var price: Double = 0.0,
    var change: Double = 0.0,
    val history: MutableList<Double> = mutableListOf(),
    var isBullish: Boolean = true
)"""
new_livestock = """data class LiveStock(
    val symbol: String,
    val name: String,
    var price: Double = 0.0,
    var change: Double = 0.0,
    val history: MutableList<Double> = mutableListOf(),
    var isBullish: Boolean = true,
    var targetPrice: Double? = null,
    var isTargetTriggered: Boolean = false
)"""
content = content.replace(old_livestock, new_livestock)

# Add state variables in LiveScreen
old_states = """    var activeScanResult by remember { mutableStateOf<ScanResult?>(null) }
    var isScanningActive by remember { mutableStateOf(false) }"""
new_states = """    var activeScanResult by remember { mutableStateOf<ScanResult?>(null) }
    var isScanningActive by remember { mutableStateOf(false) }
    
    var showAlertDialog by remember { mutableStateOf<LiveStock?>(null) }
    var triggeredAlert by remember { mutableStateOf<LiveStock?>(null) }"""
content = content.replace(old_states, new_states)

# Replace the stocks[i] assignment
old_stock_update = """                    stocks[i] = stock.copy(
                        price = price,
                        change = price - previousClose,
                        history = closes.takeLast(60).toMutableList(), // last 60 minutes
                        isBullish = price >= previousClose
                    )"""

new_stock_update = """                    val updatedStock = stock.copy(
                        price = price,
                        change = price - previousClose,
                        history = closes.takeLast(60).toMutableList(), // last 60 minutes
                        isBullish = price >= previousClose
                    )
                    
                    if (updatedStock.targetPrice != null && !updatedStock.isTargetTriggered) {
                        val target = updatedStock.targetPrice!!
                        val crossedUp = stock.price < target && price >= target
                        val crossedDown = stock.price > target && price <= target
                        if (stock.price != 0.0 && (crossedUp || crossedDown)) {
                            updatedStock.isTargetTriggered = true
                            triggeredAlert = updatedStock
                        }
                    }
                    stocks[i] = updatedStock"""
content = content.replace(old_stock_update, new_stock_update)

# Add UI for setting the alert
old_ui_row = """                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Text(activeStock.symbol.replace(".NS", ""), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                        if (isLoading && activeStock.price == 0.0) {"""
new_ui_row = """                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(activeStock.symbol.replace(".NS", ""), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                            IconButton(onClick = { showAlertDialog = activeStock }) {
                                Icon(
                                    imageVector = if (activeStock.targetPrice != null) Icons.Default.NotificationsActive else Icons.Default.Notifications, 
                                    contentDescription = "Set Alert",
                                    tint = if (activeStock.targetPrice != null) Color(0xFF3B82F6) else Color(0xFF94A3B8)
                                )
                            }
                        }
                        if (isLoading && activeStock.price == 0.0) {"""
content = content.replace(old_ui_row, new_ui_row)


# Add UI code for the Alert Dialogs at the end of LiveScreen function, just before its closing brace.
# Let's find the closing brace of LiveScreen. It's before `@Composable\nfun borderStroke()`
old_end_livescreen = """            }
        }
    }
}

@Composable
fun borderStroke()"""

new_end_livescreen = """            }
        }
    }
    
    if (showAlertDialog != null) {
        var targetInput by remember(showAlertDialog) { mutableStateOf(showAlertDialog?.targetPrice?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
        AlertDialog(
            onDismissRequest = { showAlertDialog = null },
            title = { Text("Set Price Alert for ${showAlertDialog?.symbol?.replace(".NS", "")}") },
            text = {
                OutlinedTextField(
                    value = targetInput,
                    onValueChange = { targetInput = it },
                    label = { Text("Target Price (₹)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val newTarget = targetInput.toDoubleOrNull()
                    val idx = stocks.indexOfFirst { it.symbol == showAlertDialog!!.symbol }
                    if (idx >= 0) {
                        stocks[idx] = stocks[idx].copy(targetPrice = newTarget, isTargetTriggered = false)
                    }
                    showAlertDialog = null
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAlertDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (triggeredAlert != null) {
        val stock = triggeredAlert!!
        AlertDialog(
            onDismissRequest = { triggeredAlert = null },
            title = { Text("Price Alert Triggered! 🚨") },
            text = { Text("${stock.symbol.replace(".NS", "")} has reached your target price of ₹${stock.targetPrice}!\n\nCurrent Price: ₹${"%,.2f".format(stock.price)}") },
            confirmButton = {
                Button(onClick = { triggeredAlert = null }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun borderStroke()"""
content = content.replace(old_end_livescreen, new_end_livescreen)

with open('app/src/main/java/com/example/LiveScreen.kt', 'w') as f:
    f.write(content)

