import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

lines = content.split('\n')
start_idx = -1
end_idx = -1

for i, line in enumerate(lines):
    if 'filteredResults.forEach { res ->' in line:
        start_idx = i
        break

for i in range(start_idx, len(lines)):
    if 'Text("🧠 ${res.reasons}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)' in lines[i]:
        end_idx = i + 2 # include closing braces
        break

replacement = """                filteredResults.forEach { res ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(res.ticker.replace(".NS", ""), fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                                    Text(res.strategies.take(15), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("₹${res.price}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                                    
                                    val color = when(res.signalStrength) {
                                        "STRONG BUY", "BUY" -> Color(0xFF10B981)
                                        "SELL", "WEAK/SELL" -> Color(0xFFEF4444)
                                        else -> Color(0xFFF59E0B)
                                    }
                                    
                                    Text(res.signalStrength, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color, modifier = Modifier.padding(top = 2.dp))
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = {},
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                ) {
                                    Text("Buy", fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = {},
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                                ) {
                                    Text("Sell", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }"""

if start_idx != -1 and end_idx != -1:
    lines = lines[:start_idx] + [replacement] + lines[end_idx+1:]
    
with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write('\n'.join(lines))
