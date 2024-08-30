plugins {
    kotlin("jvm") version libs.versions.kotlin.asProvider()
}

kotlin {
    jvmToolchain(8)
}

dependencies {
    api(libs.appache.commons.compress)
}