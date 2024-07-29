import java.net.URI

plugins {
    `java-library`
    `maven-publish`
}

allprojects {
    apply(plugin = "java")
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
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.jar {
        from(rootProject.file("LICENSE.md"))
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
        if (project.name != "commons-kt") {
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

sourceSets.main.configure {
    compileClasspath += project(":commons-java7").sourceSets["shared"].output
    runtimeClasspath += project(":commons-java7").sourceSets["shared"].output
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.1.0")
}

tasks.jar.configure {
    from(sourceSets["main"].output, project(":commons-java7").sourceSets["shared"].output)
}

val sourcesJar by tasks.getting(Jar::class) {
    from(sourceSets["main"].allSource, project(":commons-java7").sourceSets["shared"].allSource)
}

val javadoc by tasks.getting(Javadoc::class) {
    source += sourceSets["main"].allSource
    source += project(":commons-java7").sourceSets["shared"].allSource
    classpath +=  project(":commons-java7").sourceSets["shared"].runtimeClasspath
}