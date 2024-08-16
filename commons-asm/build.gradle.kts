
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(7))
    }
}

dependencies {
    compileOnly(libs.jetbrains.annotations.j5)

    api(project(":commons-core"))

    api(libs.asm)
    api(libs.asm.commons)
    api(libs.asm.util)
    api(libs.asm.analysis)
    api(libs.asm.tree)
}