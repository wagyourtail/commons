@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    alias(libs.plugins.kotlinx.atomicfu)
}

description = "some parsers based on CharReader.kt"


kotlin {
    jvmToolchain(8)
    jvm {
        compilerOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    js {
        useCommonJs()
        browser {
        }
        nodejs()
        binaries.executable()
    }
    wasmJs {
        browser {
        }
        nodejs()
        binaries.executable()
    }
    wasmWasi {
        nodejs()
        binaries.executable()
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":commons-kt"))
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        jvmMain {
            dependencies {
            }
        }
        jvmTest {
            dependencies {
                implementation(kotlin("test"))
//                implementation(kotlin("test-junit"))
                implementation(libs.junit.jupiter.params)
                implementation(libs.junit.platform.launcher)
            }
        }
        jsMain {
            dependencies {
            }
        }
        jsTest {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

tasks.named("jvmTest", Test::class) {
    useJUnitPlatform()
}
