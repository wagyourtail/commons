plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

for (directory in file(".").listFiles() ?: emptyArray()) {
    if (directory.isDirectory) {
        if (directory.resolve("build.gradle.kts").exists()) {
            include(directory.name)
        }
    }
}

rootProject.name = "commons"

