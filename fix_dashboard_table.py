import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

old_results = """            // Scan Results List
            if (scanResults.isNotEmpty()) {
                scanResults.forEach { res ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    val nameToDisplay = if (res.name != res.ticker && res.name.isNotEmpty()) "${res.name} (${res.ticker.replace(".NS", "")})" else res.ticker.replace(".NS", "")
                                    Text(nameToDisplay, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                    Text(res.strategies, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("₹${"%,.2f".format(res.price)}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                                    
                                    val isBullish = res.change >= 0
                                    val changeColor = if (isBullish) Color(0xFF10B981) else Color(0xFFEF4444)
                                    val changeStr = "${if(isBullish) "+" else ""}₹${"%.2f".format(res.change)} (${"%.2f".format(res.changePercent)}%)"
                                    Text(changeStr, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = changeColor)
                                    
                                    val color = when(res.signalStrength) {
                                        "STRONG BUY", "BUY" -> Color(0xFF10B981)
                                        "SELL", "WEAK/SELL" -> Color(0xFFEF4444)
                                        else -> Color(0xFFF59E0B)
                                    }
                                    
                                    val stars = when(res.signalStrength) {
                                        "STRONG BUY" -> "⭐⭐⭐⭐⭐"
                                        "BUY" -> "⭐⭐⭐⭐"
                                        "MILD BUY" -> "⭐⭐⭐"
                                        "HOLD" -> "⭐⭐"
                                        "SELL", "WEAK/SELL" -> "⭐"
                                        else -> "⭐⭐"
                                    }
                                    
                                    Text(stars, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Targets", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text("₹${res.target1?.let { Math.round(it) } ?: "N/A"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
                                        Text("₹${res.target2?.let { Math.round(it) } ?: "N/A"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Stop Loss", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                    Text("₹${res.stopLoss?.let { Math.round(it) } ?: "N/A"}", fontSize = 12.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
                                }
                            }
                            
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    res.reasons, 
                                    fontSize = 11.sp, 
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }"""

new_results = """            // Scan Results List
            if (scanResults.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(vertical = 12.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Share", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(0.26f))
                            Text("CMP", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(0.18f), textAlign = TextAlign.End)
                            Text("T1", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(0.18f), textAlign = TextAlign.End)
                            Text("T2", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(0.18f), textAlign = TextAlign.End)
                            Text("SL", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(0.20f), textAlign = TextAlign.End)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        
                        scanResults.forEachIndexed { index, res ->
                            RecommendationTableRow(res, index)
                            if (index < scanResults.lastIndex) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            }
                        }
                    }
                }
            }"""

content = content.replace(old_results, new_results)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

