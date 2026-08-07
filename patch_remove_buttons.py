import re

with open('app/src/main/java/com/example/LiveScreen.kt', 'r') as f:
    content = f.read()

target = """            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Buy ${activeStock.symbol.replace(".NS", "")}", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Sell", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }"""

if target in content:
    content = content.replace(target, "")
    with open('app/src/main/java/com/example/LiveScreen.kt', 'w') as f:
        f.write(content)
    print("Success")
else:
    print("Target not found")
