import re

with open('app/src/main/java/com/example/AppDatabase.kt', 'r') as f:
    content = f.read()

target = """@Entity(tableName = "price_alerts")
data class PriceAlert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ticker: String,
    val name: String = "",
    val targetPrice: Double,
    val isAlertActive: Boolean = true
)"""

replacement = """@Entity(tableName = "price_alerts")
data class PriceAlert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val ticker: String,
    val name: String = "",
    val priceTarget: Double,
    val isTriggered: Boolean = false,
    val isAlertActive: Boolean = true
)"""

content = content.replace(target, replacement)

with open('app/src/main/java/com/example/AppDatabase.kt', 'w') as f:
    f.write(content)
