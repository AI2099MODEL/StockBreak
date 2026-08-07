import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

replacement = """            // Filters Stack
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                // Categories
                @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { title ->
                        val selected = selectedCategory == title
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) Color(0xFF2ECA8B) else Color(0xFFFCFDF2),
                            contentColor = if (selected) Color.White else Color(0xFF475569),
                            modifier = Modifier.clickable { selectedCategory = title }
                        ) {
                            Text(
                                text = title,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // Price Filter
                @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    priceFilters.forEach { filter ->
                        val selected = priceFilter == filter
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) Color(0xFFE5F5E0) else Color(0xFFF6FAF0),
                            contentColor = if (selected) Color(0xFF0F172A) else Color(0xFF475569),
                            modifier = Modifier.clickable { priceFilter = filter }
                        ) {
                            Text(
                                text = filter,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // Rating Filter
                @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ratingFilters.forEach { filter ->
                        val selected = ratingFilter == filter
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) Color(0xFFE5F5E0) else Color(0xFFF6FAF0),
                            contentColor = if (selected) Color(0xFF0F172A) else Color(0xFF475569),
                            modifier = Modifier.clickable { ratingFilter = filter }
                        ) {
                            Text(
                                text = filter,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }"""

start_str = "            // Categories\n            LazyRow("
content_part1 = content.split(start_str)[0]

end_str = """            Button(
                onClick = {"""

content_part2 = "\n" + end_str + content.split(end_str)[1]

content = content_part1 + replacement + content_part2

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
