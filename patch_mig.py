import re

with open('app/src/main/java/com/example/MyApplication.kt', 'r') as f:
    content = f.read()

target = """        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "price-alert-db"
        ).build()"""

replacement = """        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "price-alert-db"
        ).fallbackToDestructiveMigration().build()"""

content = content.replace(target, replacement)
with open('app/src/main/java/com/example/MyApplication.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/AppDatabase.kt', 'r') as f:
    db_content = f.read()

db_content = db_content.replace('version = 1', 'version = 2')
with open('app/src/main/java/com/example/AppDatabase.kt', 'w') as f:
    f.write(db_content)
