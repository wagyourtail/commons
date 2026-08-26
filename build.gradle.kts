import java.net.URI

plugins {
    kotlin("multiplatform") version libs.versions.kotlin.asProvider() apply false
    kotlin("plugin.lombok") version libs.versions.kotlin.asProvider() apply false
    kotlin("plugin.serialization") version libs.versions.kotlin.asProvider() apply false
    alias(libs.plugins.commons)
    alias(libs.plugins.lombok)
    `java-library`
    `maven-publish`
}

commons.autoGroup()
commons.autoVersion()

allprojects {
    val kotlin = project.projectDir.name.endsWith("-kt")

    @Suppress("AvoidApplyPluginMethod")
    run {
        apply(plugin = "xyz.wagyourtail.commons-gradle")
        if (!kotlin) {
            apply(plugin = "java-library")
            apply(plugin = "io.freefair.lombok")

            commons.autoToolchain(8, 17)

            java {
                withSourcesJar()
                withJavadocJar()
            }
        } else {
            apply(plugin = "base")
        }
        apply(plugin = "maven-publish")
    }

    base {
        archivesName = project.name
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        if (!kotlin) {
            val testImplementation = configurations.getByName("testImplementation")
            val testRuntimeOnly = configurations.getByName("testRuntimeOnly")

            testImplementation(rootProject.libs.junit.jupiter)
            testRuntimeOnly(rootProject.libs.junit.platform.launcher)
//            testRuntimeOnly("org.junit.platform:junit-platform-launcher")
        }
    }

    tasks.withType<Jar> {
        from(rootProject.file("LICENSE.md"))

        manifest {
            attributes(
                "Implementation-Vendor" to "wagyourtail.xyz",
                "Implementation-Vendor-Id" to "xyz.wagyourtail",
                "Implementation-Vendor-Url" to "https://github.com/wagyourtail",
                "Implementation-Url" to "https://github.com/wagyourtail/commons"
            )
        }
    }

    if (!kotlin) {
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
