java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(7))
    }
}

dependencies {
    api(project(":commons-core"))
    compileOnly(libs.jetbrains.annotations.j5)
}