with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

chart_code = """
@Composable
fun MiniPriceChart(prices: List<Double>, modifier: Modifier = Modifier, lineColor: Color) {
    if (prices.isEmpty()) return
    val pts = prices.takeLast(60) // Last 60 periods
    val maxPrice = pts.maxOrNull() ?: 1.0
    val minPrice = pts.minOrNull() ?: 0.0
    
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val priceRange = maxPrice - minPrice
        val stepX = width / (pts.size - 1).coerceAtLeast(1)
        
        val path = androidx.compose.ui.graphics.Path()
        pts.forEachIndexed { index, price ->
            val x = index * stepX
            val y = height - if (priceRange == 0.0) height / 2 else ((price - minPrice) / priceRange) * height
            if (index == 0) {
                path.moveTo(x, y.toFloat())
            } else {
                path.lineTo(x, y.toFloat())
            }
        }
        drawPath(
            path = path,
            color = lineColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
        )
    }
}
"""

replacement = """
            Spacer(modifier = Modifier.height(16.dp))
            
            if (res.historicalPrices.isNotEmpty()) {
                MiniPriceChart(
                    prices = res.historicalPrices,
                    modifier = Modifier.fillMaxWidth().height(100.dp).padding(vertical = 8.dp),
                    lineColor = color
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Text("Targets & Stop Loss"
"""

content = content.replace('Text("Targets & Stop Loss"', replacement)

content = content.replace("@Composable\nfun TimeframeAnalysisCard", chart_code + "\n@Composable\nfun TimeframeAnalysisCard")

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
