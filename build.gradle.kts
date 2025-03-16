import java.net.URI

plugins {
    kotlin("multiplatform") version libs.versions.kotlin.asProvider() apply false
    `java-library`
    `maven-publish`
}

allprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    group = project.properties["maven_group"] as String
    version = if (project.hasProperty("version_snapshot")) project.properties["version"] as String + "-SNAPSHOT" else project.properties["version"] as String

    base {
        archivesName = project.name
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(8))
        }
        if (project.name != "commons-kt") {
            withSourcesJar()
            withJavadocJar()
        }
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.jar {
        from(rootProject.file("LICENSE.md"))
    }

    if (project.name != "commons-kt") {
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
    }

    tasks.test {
        useJUnitPlatform()
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
        if (project.name != "commons-kt" && project.name != "commons-gradle" && project.name != "commons-kvision") {
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
    compileOnly("org.jetbrains:annotations:24.1.0")

    api(project(":commons-core"))
}
