
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(7))
    }
}

dependencies {
    api(project(":commons-core"))
    compileOnly("org.jetbrains:annotations-java5:24.1.0")
}