plugins {
    kotlin("jvm")
    kotlin("plugin.lombok")
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
    api(project(":commons-asm"))
    api(gradleApi())

    api(libs.asm)
    api(libs.asm.tree)
    api(libs.asm.commons)
}