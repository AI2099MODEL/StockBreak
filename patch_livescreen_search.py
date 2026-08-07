import re

with open('app/src/main/java/com/example/LiveScreen.kt', 'r') as f:
    content = f.read()

search_row_regex = re.compile(r'        Row\(\n            modifier = Modifier\.fillMaxWidth\(\)\.padding\(bottom = 12\.dp\),\n            verticalAlignment = Alignment\.CenterVertically,\n            horizontalArrangement = Arrangement\.spacedBy\(8\.dp\)\n        \) \{\n            var searchQuery by remember \{ mutableStateOf\(\"\"\) \}\n            OutlinedTextField\(\n                value = searchQuery,\n                onValueChange = \{ searchQuery = it\.uppercase\(\) \},\n                modifier = Modifier\.weight\(1f\),\n                placeholder = \{ Text\(\"Add Ticker \(e\.g\. ITC\)\"\) \},\n                singleLine = true,\n                colors = OutlinedTextFieldDefaults\.colors\(\n                    unfocusedContainerColor = Color\.White,\n                    focusedContainerColor = Color\.White\n                \)\n            \)\n            Button\(\n                onClick = \{\n                    if \(searchQuery\.isNotBlank\(\)\) \{\n                        val ticker = if \(searchQuery\.contains\(\"\.\"\)\) searchQuery else \"\$searchQuery\.NS\"\n                        if \(stocks\.none \{ it\.symbol == ticker \}\) \{\n                            stocks\.add\(LiveStock\(ticker, ticker\)\)\n                            activeSymbol = ticker\n                        \}\n                        searchQuery = \"\"\n                    \}\n                \},\n                modifier = Modifier\.height\(56\.dp\),\n                shape = RoundedCornerShape\(12\.dp\)\n            \) \{\n                Text\(\"Add\"\)\n            \}\n        \}\n        \n', re.DOTALL)

content = search_row_regex.sub('', content)

app_header_regex = re.compile(r'        // App Header\n        Row\(\n            modifier = Modifier\.fillMaxWidth\(\)\.padding\(bottom = 16\.dp\),\n            horizontalArrangement = Arrangement\.SpaceBetween,\n            verticalAlignment = Alignment\.CenterVertically\n        \) \{\n            Text\(\"Investify\", fontSize = 22\.sp, fontWeight = FontWeight\.ExtraBold, color = Color\(0xFF0F172A\)\)\n            Surface\(\n                color = Color\(0xFFE6F4EA\),\n                shape = RoundedCornerShape\(8\.dp\)\n            \) \{\n                Row\(\n                    modifier = Modifier\.padding\(horizontal = 10\.dp, vertical = 6\.dp\),\n                    verticalAlignment = Alignment\.CenterVertically,\n                    horizontalArrangement = Arrangement\.spacedBy\(6\.dp\)\n                \) \{\n                    Box\(modifier = Modifier\.size\(8\.dp\)\.clip\(CircleShape\)\.background\(Color\(0xFF137333\)\)\)\n                    Text\(\"Live Free Market API\", fontSize = 13\.sp, fontWeight = FontWeight\.Bold, color = Color\(0xFF137333\)\)\n                \}\n            \}\n        \}')

new_search_row = """        // App Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Investify", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
            Surface(
                color = Color(0xFFE6F4EA),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF137333)))
                    Text("Live Free Market API", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF137333))
                }
            }
        }
        
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            var searchQuery by remember { mutableStateOf("") }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it.uppercase() },
                modifier = Modifier.weight(1f),
                placeholder = { Text("e.g. RELIANCE") },
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
                Icon(androidx.compose.material.icons.Icons.Default.Search, contentDescription = "Search")
            }
        }"""

content = app_header_regex.sub(new_search_row, content)

with open('app/src/main/java/com/example/LiveScreen.kt', 'w') as f:
    f.write(content)

