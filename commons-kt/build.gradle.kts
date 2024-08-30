import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    kotlin("multiplatform") version libs.versions.kotlin.asProvider()
    idea
}

kotlin {
    jvmToolchain(8)
    jvm {
        withJava()
    }
    js {
        browser {
            useCommonJs()
        }
        nodejs {
            useCommonJs()
        }
        binaries.executable()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.jetbrains.annotations.kmp)
                api(libs.kotlin.coroutines)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }
        val jsMain by getting {
            dependencies {
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}