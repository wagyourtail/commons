import java.net.URI

plugins {
    kotlin("multiplatform") version libs.versions.kotlin.asProvider() apply false
    kotlin("plugin.lombok") version libs.versions.kotlin.asProvider() apply false
    kotlin("plugin.serialization") version libs.versions.kotlin.asProvider() apply false
    `java-library`
    `maven-publish`
}

allprojects {
    val kotlin = project.projectDir.name.endsWith("-kt")

    apply(plugin = "xyz.wagyourtail.commons-gradle")
    if (!kotlin) {
        apply(plugin = "java-library")

        commons.autoToolchain(8, 17)

        java {
            withSourcesJar()
            withJavadocJar()
        }
    } else {
        apply(plugin = "base")
    }
    apply(plugin = "maven-publish")

    group = project.properties["maven_group"] as String
    commons.autoVersion()

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
