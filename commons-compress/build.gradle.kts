
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    api(project(":commons-core"))

    api(libs.appache.commons.compress)
    implementation(libs.xz)
    implementation(libs.zstd)

    api(libs.tika.core)
    api(libs.tika.parsers)

    compileOnly(libs.jetbrains.annotations)
}
