java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    api(project(":commons-core"))
    compileOnly(libs.jetbrains.annotations.j5)
}