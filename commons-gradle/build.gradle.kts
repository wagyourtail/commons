plugins {
    val kotlinVersion: String by System.getProperties()
    kotlin("jvm") version kotlinVersion
}

kotlin {
    jvmToolchain(8)
}

dependencies {
    api(project(":commons-kt"))
    api(gradleApi())

    api(libs.asm)
    api(libs.asm.tree)
    api(libs.asm.commons)
}