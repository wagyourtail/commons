plugins {
    alias(libs.plugins.jvm.downgrader)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

dependencies {
    api(project(":commons-core"))

    api(libs.apache.commons.compress)
    implementation(libs.xz)
    implementation(libs.zstd)

    api(libs.tika.core)
    api(libs.tika.parsers)

    compileOnly(libs.jetbrains.annotations)
}

jvmdg.defaultTask.configure {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

jvmdg.defaultShadeTask.configure {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

publishing {
    publications {
        getByName<MavenPublication>("maven") {

            artifact(jvmdg.defaultShadeTask)

        }
    }
}