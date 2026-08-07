import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# We need to add percentage profit and day increase
old_block = """                                Column(horizontalAlignment = Alignment.End) {
                                    Text("₹${res.price}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                                    
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
                                }"""

new_block = """                                Column(horizontalAlignment = Alignment.End) {
                                    Text("₹${res.price}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                                    
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
                                }"""

content = content.replace(old_block, new_block)

old_block2 = """                                Column {
                                    Text("Targets", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                    Text("₹${res.target1?.let { Math.round(it) } ?: "N/A"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Stop Loss", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                    Text("₹${res.stopLoss?.let { Math.round(it) } ?: "N/A"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                                }"""

new_block2 = """                                Column {
                                    Text("Targets", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text("₹${res.target1?.let { Math.round(it) } ?: "N/A"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
                                        val profitPct = res.target1?.let { ((it - res.price) / res.price) * 100 }
                                        if (profitPct != null) {
                                            Text("+${"%.1f".format(profitPct)}%", fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Stop Loss", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                    Text("₹${res.stopLoss?.let { Math.round(it) } ?: "N/A"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                                }"""

content = content.replace(old_block2, new_block2)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

