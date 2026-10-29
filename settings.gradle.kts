pluginManagement {
    repositories {
        mavenLocal()
        maven("https://maven.wagyourtail.xyz/snapshots/")
        maven("https://maven.wagyourtail.xyz/releases/")
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    // self-hosted plugin: keep this pin at "${version}-SNAPSHOT" matching
    // gradle.properties — a lagging pin resolves a stale plugin jar (the
    // 1.0.4-SNAPSHOT pin resolved July-2025 code with inverted snapshot
    // semantics and shipped 1.0.4/1.0.5 as SNAPSHOTs into releases).
    // after bumping `version`, `publishToMavenLocal` covers the gap until
    // the next snapshot publish lands the artifact.
    id("xyz.wagyourtail.commons-gradle") version "1.0.5-SNAPSHOT"
}

//buildscript {
//    repositories {
//        mavenCentral()
//    }
//    dependencies {
//        classpath("org.ow2.asm:asm:9.10.1")
//    }
//}

commons.autoSubprojects {
    name = projectDir.name.removeSuffix("-kt")
}

//for (directory in file(".").listFiles() ?: emptyArray()) {
//    if (directory.isDirectory) {
//        if (directory.resolve("build.gradle.kts").exists()) {
//            include(directory.name)
//            project(":${directory.name}").name = directory.name.removeSuffix("-kt")
//
//            val gradle = directory.resolve("gradle")
//            if (gradle.isDirectory) {
//                for (file in gradle.listFiles() ?: emptyArray()) {
//                    if (file.name.endsWith(".versions.toml")) {
//                        dependencyResolutionManagement {
//                            versionCatalogs {
//                                create(file.name.removeSuffix(".versions.toml") + "Libs") {
//                                    from(files(file))
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

rootProject.name = "commons"
