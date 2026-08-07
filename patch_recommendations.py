import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# Replace Enum
content = content.replace("HOME, CHARTS, PORTFOLIO, LIVE, CONFIG", "HOME, RECOMMENDATIONS, PORTFOLIO, LIVE, CONFIG")

# Replace in MainApp
content = content.replace("Screen.CHARTS -> ChartsScreen(modifier = Modifier.padding(innerPadding))", "Screen.RECOMMENDATIONS -> RecommendationsScreen(modifier = Modifier.padding(innerPadding))")

# Replace in AppBottomNavigation
content = content.replace(
    'NavigationBarItem(selected = currentScreen == Screen.CHARTS, onClick = { onScreenSelected(Screen.CHARTS) }, icon = { Icon(Icons.Default.Insights, contentDescription = "Charts") }, label = { Text("Charts") })',
    'NavigationBarItem(selected = currentScreen == Screen.RECOMMENDATIONS, onClick = { onScreenSelected(Screen.RECOMMENDATIONS) }, icon = { Icon(Icons.Default.Recommend, contentDescription = "Tips") }, label = { Text("Tips") })'
)

# Remove ChartsScreen completely and replace with RecommendationsScreen
charts_regex = re.compile(r'@Composable\nfun ChartsScreen.*?fun PortfolioScreen', re.DOTALL)

recommendations_screen = """data class Recommendation(
    val ticker: String,
    val vendor: String,
    val type: String, // BUY, SELL, HOLD
    val target: Double,
    val stopLoss: Double,
    val date: String,
    val rationale: String
)

@Composable
fun RecommendationsScreen(modifier: Modifier = Modifier) {
    val recommendations = remember {
        listOf(
            Recommendation("RELIANCE", "MoneyControl", "BUY", 1400.0, 1200.0, "Today", "Strong quarterly results expected with telecom growth."),
            Recommendation("TCS", "Motilal Oswal", "HOLD", 2500.0, 2300.0, "Yesterday", "Valuations are stretched, wait for dip before fresh entry."),
            Recommendation("HDFCBANK", "ICICI Direct", "BUY", 1800.0, 1600.0, "Today", "Credit growth remains robust, net interest margins expanding."),
            Recommendation("INFY", "Prabhudas Lilladher", "SELL", 1300.0, 1450.0, "2 days ago", "Margin pressure likely to continue in near term."),
            Recommendation("ITC", "MoneyControl", "BUY", 500.0, 440.0, "Today", "Defensive play with good dividend yield and FMCG recovery."),
            Recommendation("SBIN", "Sharekhan", "BUY", 900.0, 810.0, "Yesterday", "Asset quality improving consistently, credit cycle turning favorable.")
        )
    }

    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF1F5F9))) {
        Text("Expert Recommendations", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), modifier = Modifier.padding(16.dp))
        
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recommendations) { rec ->
                RecommendationCard(rec)
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun RecommendationCard(rec: Recommendation) {
    val typeColor = when(rec.type) {
        "BUY" -> Color(0xFF10B981)
        "SELL" -> Color(0xFFEF4444)
        else -> Color(0xFFF59E0B)
    }
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = typeColor.copy(alpha = 0.15f)) {
                        Text(rec.type, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = typeColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                    Text(rec.ticker, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                }
                Text(rec.date, fontSize = 12.sp, color = Color(0xFF64748B))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text("Source: ${rec.vendor}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3B82F6))
            Text(rec.rationale, fontSize = 14.sp, color = Color(0xFF475569), modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Target", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    Text("₹${Math.round(rec.target)}", fontSize = 14.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Stop Loss", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    Text("₹${Math.round(rec.stopLoss)}", fontSize = 14.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PortfolioScreen"""

content = charts_regex.sub(recommendations_screen, content)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)

