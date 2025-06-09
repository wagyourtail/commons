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

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to "commons-gradle",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "wagyourtail.xyz",
            "Implementation-Vendor-Id" to "xyz.wagyourtail",
            "Implementation-Vendor-Url" to "https://github.com/wagyourtail",
            "Implementation-Url" to "https://github.com/wagyourtail/commons"
        )
    }
}