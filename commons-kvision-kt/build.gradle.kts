plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    alias(kvisionLibs.plugins.kvision)
}

kotlin {
    js {
        browser {
        }
        binaries.executable()
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":commons-kt"))
                api(libs.kotlin.datetime)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        jsMain {
            dependencies {
                api(kvisionLibs.kvision)
                api(kvisionLibs.kvision.bootstrap)
            }
        }
        jsTest {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}
