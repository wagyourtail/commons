plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(8)
}

dependencies {
    api(libs.appache.commons.compress)
}