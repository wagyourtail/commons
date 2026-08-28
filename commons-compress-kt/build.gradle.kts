plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(8)
}

description = "a few extension functions for using apache commons compress in kotlin"

dependencies {
    api(libs.apache.commons.compress)
}