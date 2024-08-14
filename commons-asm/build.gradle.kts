
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(7))
    }
}

dependencies {
    compileOnly("org.jetbrains:annotations-java5:24.1.0")

    api(project(":commons-core"))

    api("org.ow2.asm:asm:9.7")
    api("org.ow2.asm:asm-commons:9.7")
    api("org.ow2.asm:asm-util:9.7")
    api("org.ow2.asm:asm-analysis:9.7")
    api("org.ow2.asm:asm-tree:9.7")
}