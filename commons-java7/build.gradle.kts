java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(7))
    }
}

description = "Java 7 specific utils, for when you want to support Java 7 for some stupid reason"

dependencies {
    api(project(":commons-core"))
    compileOnly(libs.jetbrains.annotations.j5)
}