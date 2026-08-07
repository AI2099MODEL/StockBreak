with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

if 'libs.coil.compose' not in content:
    content = content.replace('dependencies {\n', 'dependencies {\n  implementation(libs.coil.compose)\n')
    
    with open('app/build.gradle.kts', 'w') as f:
        f.write(content)
