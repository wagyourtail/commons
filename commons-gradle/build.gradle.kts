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

    api("org.ow2.asm:asm:9.7")
    api("org.ow2.asm:asm-tree:9.7")
    api("org.ow2.asm:asm-commons:9.7")
}