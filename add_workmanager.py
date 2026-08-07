import re

with open('gradle/libs.versions.toml', 'r') as f:
    content = f.read()

if 'workRuntimeKtx =' not in content:
    content = content.replace('[versions]\n', '[versions]\nworkRuntimeKtx = "2.10.0"\n')
    
if 'androidx-work-runtime-ktx' not in content:
    content = content.replace('[libraries]\n', '[libraries]\nandroidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "workRuntimeKtx" }\n')

with open('gradle/libs.versions.toml', 'w') as f:
    f.write(content)

with open('app/build.gradle.kts', 'r') as f:
    build_content = f.read()

if 'libs.androidx.work.runtime.ktx' not in build_content:
    build_content = build_content.replace('dependencies {\n', 'dependencies {\n  implementation(libs.androidx.work.runtime.ktx)\n')

with open('app/build.gradle.kts', 'w') as f:
    f.write(build_content)

