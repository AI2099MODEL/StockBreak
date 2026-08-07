import re

with open('app/src/main/java/com/example/LiveScreen.kt', 'r') as f:
    content = f.read()

replacement = """    val stocks = remember { 
        mutableStateListOf<LiveStock>(
            LiveStock("RELIANCE.NS", "RELIANCE.NS"),
            LiveStock("TCS.NS", "TCS.NS"),
            LiveStock("HDFCBANK.NS", "HDFCBANK.NS"),
            LiveStock("INFY.NS", "INFY.NS")
        ) 
    }
    var activeSymbol by remember { mutableStateOf("RELIANCE.NS") }"""

content = re.sub(r'val stocks = remember\s*\{\s*mutableStateListOf<LiveStock>\(\)\s*\}\s*var activeSymbol by remember\s*\{\s*mutableStateOf\(""\)\s*\}', replacement, content)

with open('app/src/main/java/com/example/LiveScreen.kt', 'w') as f:
    f.write(content)
