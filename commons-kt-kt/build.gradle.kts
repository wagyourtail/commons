plugins {
    kotlin("multiplatform")
    alias(libs.plugins.kotlinx.atomicfu)
}

kotlin {
    jvmToolchain(8)
    jvm {
        compilerOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
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
                api(libs.kotlin.datetime)
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