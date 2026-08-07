import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

replacement = """                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Targets", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                    Text("₹${res.target1 ?: "N/A"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Stop Loss", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                    Text("₹${res.stopLoss ?: "N/A"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }"""

content = content.replace("                            Spacer(modifier = Modifier.height(12.dp))\n                            \n                        }\n                    }", replacement)

# Replace the text of signal strength with stars
# Let's map STRONG BUY to 5 stars, BUY to 4, MILD BUY to 3, HOLD to 2, SELL to 1
star_code = """                                    val stars = when(res.signalStrength) {
                                        "STRONG BUY" -> "⭐⭐⭐⭐⭐"
                                        "BUY" -> "⭐⭐⭐⭐"
                                        "MILD BUY" -> "⭐⭐⭐"
                                        "HOLD" -> "⭐⭐"
                                        "SELL", "WEAK/SELL" -> "⭐"
                                        else -> "⭐⭐"
                                    }
                                    
                                    Text(stars, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))"""

content = content.replace("""                                    Text(res.signalStrength, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color, modifier = Modifier.padding(top = 2.dp))""", star_code)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
