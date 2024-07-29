
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(7))
    }
}

val shared by sourceSets.creating {
    compileClasspath += sourceSets["main"].compileClasspath
    runtimeClasspath += sourceSets["main"].runtimeClasspath
}


sourceSets.main.configure {
    compileClasspath += shared.output
    runtimeClasspath += shared.output
}

dependencies {
    compileOnly("org.jetbrains:annotations-java5:24.1.0")
    compileOnly("org.ow2.asm:asm:9.7")
    compileOnly("org.ow2.asm:asm-commons:9.7")
    compileOnly("org.ow2.asm:asm-util:9.7")
    compileOnly("org.ow2.asm:asm-analysis:9.7")
    compileOnly("org.ow2.asm:asm-tree:9.7")
}

tasks.jar.configure {
    from(sourceSets["main"].output, sourceSets["shared"].output)
}

val sourcesJar by tasks.getting(Jar::class) {
    from(sourceSets["main"].allSource, sourceSets["shared"].allSource)
    archiveClassifier.set("sources")
}

val javadoc by tasks.getting(Javadoc::class) {
    source = sourceSets["main"].allSource
    source += sourceSets["shared"].allSource
}