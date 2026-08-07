import re

with open('app/src/main/java/com/example/MarketScreen.kt', 'r') as f:
    content = f.read()

# Add LocalContext import if not present
if 'import androidx.compose.ui.platform.LocalContext' not in content:
    content = content.replace('import androidx.compose.ui.platform.LocalContext\n', '')
    content = content.replace('import androidx.compose.ui.Modifier', 'import androidx.compose.ui.Modifier\nimport androidx.compose.ui.platform.LocalContext\nimport androidx.lifecycle.compose.LocalLifecycleOwner\n')

# Find the YouTubePlayer Composable and replace it
new_player = """@Composable
fun YouTubePlayer(videoId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    AndroidView(
        modifier = modifier,
        factory = {
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                webChromeClient = WebChromeClient()
                webViewClient = WebViewClient()
            }
        },
        update = { webView ->
            val html = \"\"\"
                <!DOCTYPE html>
                <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                        <style>
                            body { margin: 0; padding: 0; background-color: #000; }
                            iframe { border: none; width: 100vw; height: 100vh; }
                        </style>
                    </head>
                    <body>
                        <iframe 
                            src="https://www.youtube.com/embed/${videoId}?autoplay=1&controls=1&modestbranding=1&rel=0&playsinline=1" 
                            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                            allowfullscreen>
                        </iframe>
                    </body>
                </html>
            \"\"\".trimIndent()
            webView.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "utf-8", null)
        }
    )
}"""

pattern = re.compile(r'@Composable\s*fun YouTubePlayer.*?^}', re.MULTILINE | re.DOTALL)
content = pattern.sub(new_player, content)

with open('app/src/main/java/com/example/MarketScreen.kt', 'w') as f:
    f.write(content)
