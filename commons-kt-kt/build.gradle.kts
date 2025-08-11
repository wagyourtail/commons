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
                api(libs.jetbrains.annotations)
                api(libs.kotlin.coroutines)
                api(libs.kotlin.datetime)
                api(libs.kotlin.atomicfu)
                api(libs.kotlin.serialization)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation(libs.kotlin.serialization.json)
            }
        }
        val jvmMain by getting {
            dependencies {
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
//                implementation(kotlin("test-junit"))
                implementation(libs.junit.jupiter.params)
                implementation(libs.junit.platform.launcher)
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

val jvmTest by tasks.getting(Test::class) {
    useJUnitPlatform()
}
