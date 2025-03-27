import java.net.URI

plugins {
    kotlin("multiplatform") version libs.versions.kotlin.asProvider() apply false
    `java-library`
    `maven-publish`
}

allprojects {
    val kotlin = project.projectDir.name.endsWith("-kt")
    if (!kotlin) {
        apply(plugin = "java-library")

        java {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(8))
            }

            withSourcesJar()
            withJavadocJar()
        }
    } else {
        apply(plugin = "base")
    }
    apply(plugin = "maven-publish")

    group = project.properties["maven_group"] as String
    version =
        if (project.hasProperty("version_snapshot")) project.properties["version"] as String + "-SNAPSHOT" else project.properties["version"] as String

    base {
        archivesName = project.name
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        if (!kotlin) {
            val testImplementation by configurations.getting
            val testCompileOnly by configurations.getting
            val testRuntimeOnly by configurations.getting
            val testAnnotationProcessor by configurations.getting

            compileOnly(rootProject.libs.lombok)
            annotationProcessor(rootProject.libs.lombok)

            testImplementation(rootProject.libs.junit.jupiter)
            testRuntimeOnly(rootProject.libs.junit.platform.launcher)
//            testRuntimeOnly("org.junit.platform:junit-platform-launcher")

            testCompileOnly(rootProject.libs.lombok)
            testAnnotationProcessor(rootProject.libs.lombok)
        }
    }

    if (!kotlin) {
        tasks.jar {
            from(rootProject.file("LICENSE.md"))
        }
    }

    if (!kotlin) {
        tasks.compileTestJava {
            javaCompiler = javaToolchains.compilerFor {
                languageVersion.set(JavaLanguageVersion.of(17))
            }
        }

        tasks.test {
            javaLauncher = javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(17))
            }
        }

        tasks.test {
            useJUnitPlatform()
        }
    }

    publishing {
        repositories {
            maven {
                name = "WagYourMaven"
                url = if (project.hasProperty("version_snapshot")) {
                    URI.create("https://maven.wagyourtail.xyz/snapshots/")
                } else {
                    URI.create("https://maven.wagyourtail.xyz/releases/")
                }
                credentials {
                    username = project.findProperty("mvn.user") as String? ?: System.getenv("USERNAME")
                    password = project.findProperty("mvn.key") as String? ?: System.getenv("TOKEN")
                }
            }
        }
        // kmp does it for us
        if (!kotlin) {
            publications {
                create<MavenPublication>("maven") {
                    groupId = project.group as String
                    artifactId = project.base.archivesName.get()
                    version = project.version as String

                    from(components["java"])
                }
            }
        }
    }

}

evaluationDependsOnChildren()

dependencies {
    compileOnly(libs.jetbrains.annotations)

    api(project(":commons-core"))
}
