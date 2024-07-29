
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(7))
    }
}

val shared by sourceSets.creating {}


sourceSets.main.configure {
    compileClasspath += shared.output
    runtimeClasspath += shared.output
}

dependencies {
    compileOnly("org.jetbrains:annotations-java5:24.1.0")
}

tasks.jar.configure {
    from(sourceSets["main"].output, sourceSets["shared"].output)
}
