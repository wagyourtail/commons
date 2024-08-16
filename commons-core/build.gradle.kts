java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(7))
    }
}

dependencies {
    compileOnly(libs.jetbrains.annotations.j5)
}