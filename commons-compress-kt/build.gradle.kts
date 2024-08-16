plugins {
    val kotlinVersion: String by System.getProperties()
    kotlin("jvm") version kotlinVersion
}

kotlin {
    jvmToolchain(8)
}

dependencies {
    api(libs.appache.commons.compress)
}