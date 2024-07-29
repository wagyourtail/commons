plugins {
    val kotlinVersion: String by System.getProperties()
    kotlin("jvm") version kotlinVersion
}

kotlin {
    jvmToolchain(8)
}

dependencies {
    compileOnly("org.apache.commons:commons-compress:1.26.2")
}