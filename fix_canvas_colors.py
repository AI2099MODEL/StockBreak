import re

with open('app/src/main/java/com/example/LiveScreen.kt', 'r') as f:
    content = f.read()

# For gridColor
old_grid = """        // Draw Grid Lines
        val gridColor = MaterialTheme.colorScheme.background"""
new_grid = """        // Draw Grid Lines
        val gridColor = gridColorValue"""

# For drawCircle
old_circle = """        drawCircle(
            color = MaterialTheme.colorScheme.surface,
            radius = 12f,
            center = androidx.compose.ui.geometry.Offset(lastX, lastY),
            style = Stroke(width = 3f)
        )"""
new_circle = """        drawCircle(
            color = surfaceColorValue,
            radius = 12f,
            center = androidx.compose.ui.geometry.Offset(lastX, lastY),
            style = Stroke(width = 3f)
        )"""

content = content.replace(old_grid, new_grid)
content = content.replace(old_circle, new_circle)

# Now add the values before Canvas
old_canvas = "    Canvas(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface)) {"
new_canvas = "    val gridColorValue = MaterialTheme.colorScheme.background\n    val surfaceColorValue = MaterialTheme.colorScheme.surface\n    Canvas(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface)) {"
content = content.replace(old_canvas, new_canvas)

with open('app/src/main/java/com/example/LiveScreen.kt', 'w') as f:
    f.write(content)

