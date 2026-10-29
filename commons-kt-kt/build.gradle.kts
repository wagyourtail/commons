@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    alias(libs.plugins.kotlinx.atomicfu)
}

description = "kotlin utilities"

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
        commonMain {
            dependencies {
                api(libs.jetbrains.annotations)
                api(libs.kotlin.coroutines)
                api(libs.kotlin.datetime)
                api(libs.kotlin.atomicfu)
                api(libs.kotlin.serialization)
                // optional integration: okio adapters in commonskt.reader only
                // resolve for consumers that have okio on their classpath
                compileOnly(libs.okio)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(libs.kotlin.serialization.json)
            }
        }
        jvmMain {
            dependencies {
                // optional integration: commonskt.compress helpers resolve
                // for consumers that have commons-compress on their classpath
                compileOnly(libs.apache.commons.compress)
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
                // optional integration: commons.kvision components resolve
                // for consumers that have kvision on their classpath
                compileOnly(kvisionLibs.kvision)
                compileOnly(kvisionLibs.kvision.bootstrap)
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
