plugins {
    val kotlinVersion: String by System.getProperties()
    kotlin("jvm") version kotlinVersion
    `java-gradle-plugin`
}

kotlin {
    jvmToolchain(8)
}

gradlePlugin {
    plugins {
        create("simplePlugin") {
            id = "xyz.wagyourtail.commons-gradle"
            description = project.description
            implementationClass = "xyz.wagyourtail.commons.gradle.GradleMain"
            version = project.version as String
        }
    }
}

dependencies {
    api(project(":commons-kt"))
    api(gradleApi())

    api(libs.asm)
    api(libs.asm.tree)
    api(libs.asm.commons)
}