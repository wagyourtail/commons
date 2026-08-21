@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsSubTargetDsl
import org.jetbrains.kotlin.gradle.targets.js.dsl.KotlinJsTargetDsl

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    alias(libs.plugins.kotlinx.atomicfu)
}

kotlin {
    jvmToolchain(8)
    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }

    jvm {
        compilerOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xexpect-actual-classes")
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

    linuxX64()
    linuxArm64()

    sourceSets {
        getByName("commonMain") {
            dependencies {
                api(libs.jetbrains.annotations)
                api(libs.kotlin.coroutines)
                api(libs.kotlin.datetime)
                api(libs.kotlin.atomicfu)
                api(libs.kotlin.serialization)
            }
        }
        getByName("commonTest") {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(libs.kotlin.serialization.json)
            }
        }
        getByName("jvmMain") {
            dependencies {
            }
        }
        getByName("jvmTest") {
            dependencies {
                implementation(kotlin("test"))
//                implementation(kotlin("test-junit"))
                implementation(libs.junit.jupiter.params)
                implementation(libs.junit.platform.launcher)
            }
        }
        getByName("jsMain") {
            dependencies {
            }
        }
        getByName("jsTest") {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

val jvmTest by tasks.getting(Test::class) {
    useJUnitPlatform()
}
