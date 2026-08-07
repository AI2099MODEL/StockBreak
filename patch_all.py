import os

# Update PriceAlertWorker.kt
with open('app/src/main/java/com/example/PriceAlertWorker.kt', 'r') as f:
    worker_content = f.read()

worker_content = worker_content.replace('alert.targetPrice', 'alert.priceTarget')
worker_content = worker_content.replace('alert.copy(isAlertActive = false)', 'alert.copy(isTriggered = true, isAlertActive = false)')
worker_content = worker_content.replace(
    'if (Math.abs(currentPrice - alert.priceTarget) / alert.priceTarget < 0.005 || currentPrice >= alert.priceTarget) {',
    'if (!alert.isTriggered && (Math.abs(currentPrice - alert.priceTarget) / alert.priceTarget < 0.005 || currentPrice >= alert.priceTarget)) {'
)
with open('app/src/main/java/com/example/PriceAlertWorker.kt', 'w') as f:
    f.write(worker_content)

# Update WatchlistScreen.kt
with open('app/src/main/java/com/example/WatchlistScreen.kt', 'r') as f:
    screen_content = f.read()

screen_content = screen_content.replace('alert.targetPrice', 'alert.priceTarget')
with open('app/src/main/java/com/example/WatchlistScreen.kt', 'w') as f:
    f.write(screen_content)

# Update WatchlistViewModel.kt
with open('app/src/main/java/com/example/WatchlistViewModel.kt', 'r') as f:
    vm_content = f.read()

vm_content = vm_content.replace('targetPrice = targetPrice', 'priceTarget = targetPrice')
with open('app/src/main/java/com/example/WatchlistViewModel.kt', 'w') as f:
    f.write(vm_content)
