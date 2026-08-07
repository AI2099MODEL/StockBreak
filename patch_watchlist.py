import re

with open('app/src/main/java/com/example/WatchlistScreen.kt', 'r') as f:
    content = f.read()

replacement = """@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun WatchlistScreen(
    modifier: Modifier = Modifier,
    onSymbolSelected: (String) -> Unit = {},
    viewModel: WatchlistViewModel = viewModel(
        factory = WatchlistViewModel.Factory(WatchlistRepository(MyApplication.database.priceAlertDao()))
    )
) {"""

content = re.sub(r'@OptIn\(ExperimentalMaterial3Api::class,\s*androidx\.compose\.foundation\.ExperimentalFoundationApi::class\)\s*@Composable\s*fun WatchlistScreen\(\s*modifier: Modifier = Modifier,\s*viewModel: WatchlistViewModel = viewModel\(\s*factory = WatchlistViewModel\.Factory\(WatchlistRepository\(MyApplication\.database\.priceAlertDao\(\)\)\)\s*\)\s*\)\s*\{', replacement, content)

# Make Card clickable
card_target = """Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),"""
card_replacement = """Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onSymbolSelected(alert.ticker) },"""
                            
content = content.replace(card_target, card_replacement)

with open('app/src/main/java/com/example/WatchlistScreen.kt', 'w') as f:
    f.write(content)
