import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

replacements = [
    ('Color(0xFF2ECA8B)', 'MaterialTheme.colorScheme.primary'),
    ('Color(0xFFFCFDF2)', 'MaterialTheme.colorScheme.surfaceVariant'),
    ('Color(0xFF475569)', 'MaterialTheme.colorScheme.onSurfaceVariant'),
    ('Color.White', 'MaterialTheme.colorScheme.surface'),
    ('Color(0xFFF8FAFC)', 'MaterialTheme.colorScheme.background'),
    ('Color(0xFF0F172A)', 'MaterialTheme.colorScheme.onBackground'),
    ('Color(0xFF3B82F6)', 'MaterialTheme.colorScheme.primary'),
    ('Color(0xFF64748B)', 'MaterialTheme.colorScheme.onSurfaceVariant'),
    ('Color(0xFFE2E8F0)', 'MaterialTheme.colorScheme.outlineVariant'),
    ('Color(0xFFE0E7FF)', 'MaterialTheme.colorScheme.primaryContainer'),
    ('Color(0xFF3730A3)', 'MaterialTheme.colorScheme.onPrimaryContainer'),
    ('Color(0xFFC7D2FE)', 'MaterialTheme.colorScheme.outlineVariant'),
    ('Color(0xFFF0FDF4)', 'Color(0xFF10B981).copy(alpha = 0.1f)'),
    ('Color(0xFFFEF2F2)', 'Color(0xFFEF4444).copy(alpha = 0.1f)'),
    ('Color(0xFFFFFBEB)', 'Color(0xFFF59E0B).copy(alpha = 0.1f)'),
    ('Color(0xFF059669)', 'Color(0xFF10B981)'),
    ('Color(0xFFDC2626)', 'Color(0xFFEF4444)'),
]

for old, new in replacements:
    content = content.replace(old, new)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
