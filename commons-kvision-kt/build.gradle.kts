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
        getByName("commonMain") {
            dependencies {
                api(project(":commons-kt"))
                api(libs.kotlin.datetime)
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        getByName("jsMain") {
            dependencies {
                api(kvisionLibs.kvision)
                api(kvisionLibs.kvision.bootstrap)
            }
        }
        getByName("jsTest") {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}
