import re

with open('app/src/main/java/com/example/LiveScreen.kt', 'r') as f:
    content = f.read()

replacements = [
    ('Color(0xFFF1F5F9)', 'MaterialTheme.colorScheme.background'),
    ('Color(0xFF0F172A)', 'MaterialTheme.colorScheme.onBackground'),
    ('Color(0xFFE6F4EA)', 'MaterialTheme.colorScheme.primaryContainer'),
    ('Color(0xFF137333)', 'MaterialTheme.colorScheme.onPrimaryContainer'),
    ('Color(0xFF64748B)', 'MaterialTheme.colorScheme.onSurfaceVariant'),
    ('Color(0xFF94A3B8)', 'MaterialTheme.colorScheme.outline'),
    ('Color(0xFF3B82F6)', 'MaterialTheme.colorScheme.primary'),
    ('Color(0xFFF0F6FF)', 'MaterialTheme.colorScheme.primaryContainer'),
    ('Color.White', 'MaterialTheme.colorScheme.surface'),
    ('Color(0xFFE2E8F0)', 'MaterialTheme.colorScheme.outlineVariant'),
    ('Color(0xFFFAFAFA)', 'MaterialTheme.colorScheme.surface'),
    ('Color(0xFF10B981)', 'Color(0xFF10B981)'), # Semantic color
]

for old, new in replacements:
    content = content.replace(old, new)

with open('app/src/main/java/com/example/LiveScreen.kt', 'w') as f:
    f.write(content)
