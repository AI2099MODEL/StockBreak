import re

with open('app/src/main/java/com/example/LiveScreen.kt', 'r') as f:
    content = f.read()

search_code = """        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            var searchQuery by remember { mutableStateOf("") }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it.uppercase() },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Add Ticker (e.g. ITC)") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )
            Button(
                onClick = {
                    if (searchQuery.isNotBlank()) {
                        val ticker = if (searchQuery.contains(".")) searchQuery else "$searchQuery.NS"
                        if (stocks.none { it.symbol == ticker }) {
                            stocks.add(LiveStock(ticker, ticker))
                            activeSymbol = ticker
                        }
                        searchQuery = ""
                    }
                },
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add")
            }
        }
        
        Text("MARKETS WATCHLIST", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), letterSpacing = 0.5.sp, modifier = Modifier.padding(bottom = 8.dp))"""

content = content.replace('        Text("MARKETS WATCHLIST", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), letterSpacing = 0.5.sp, modifier = Modifier.padding(bottom = 8.dp))', search_code)

with open('app/src/main/java/com/example/LiveScreen.kt', 'w') as f:
    f.write(content)
