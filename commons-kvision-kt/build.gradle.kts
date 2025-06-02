plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    alias(kvisionLibs.plugins.kvision)
}

kotlin {
    js(IR) {
        browser {
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":commons-kt"))
                api(kvisionLibs.kvision.server.ktor.koin)
                api(libs.kotlin.datetime)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jsMain by getting {
            dependencies {
                api(kvisionLibs.kvision)
                api(kvisionLibs.kvision.bootstrap)
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}



