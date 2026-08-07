import re

with open('app/src/main/java/com/example/MarketScreen.kt', 'r') as f:
    content = f.read()

# Filter logic
filter_code = """
    // Helper function to check if time is within 4 hours
    fun isWithin4Hours(timeAgo: String): Boolean {
        val lower = timeAgo.lowercase()
        if (lower.contains("min") || lower.contains("sec")) return true
        if (lower.contains("hour")) {
            val num = lower.filter { it.isDigit() }.toIntOrNull() ?: return false
            return num <= 4
        }
        return false
    }

    LaunchedEffect(selectedFilter) {
        while (true) {
            val filtered = if (selectedFilter == "All") allVideos else allVideos.filter { 
                it.category.contains(selectedFilter, ignoreCase = true) || 
                it.channel.contains(selectedFilter, ignoreCase = true) ||
                it.title.contains(selectedFilter, ignoreCase = true)
            }
            
            val recentVideos = filtered.filter { isWithin4Hours(it.timeAgo) }
            videos = recentVideos.shuffled().take(5)
            delay(30 * 60 * 1000L) // 30 minutes
        }
    }
"""

# Replace the LaunchedEffect block and add the helper
pattern = re.compile(r'LaunchedEffect\(selectedFilter\)\s*\{.*?delay\(30 \* 60 \* 1000L\) // 30 minutes\s*\}\s*\}', re.MULTILINE | re.DOTALL)
content = pattern.sub(filter_code.strip(), content)

with open('app/src/main/java/com/example/MarketScreen.kt', 'w') as f:
    f.write(content)
