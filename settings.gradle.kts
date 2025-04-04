plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

for (directory in file(".").listFiles() ?: emptyArray()) {
    if (directory.isDirectory) {
        if (directory.resolve("build.gradle.kts").exists()) {
            include(directory.name)
            project(":${directory.name}").name = directory.name.removeSuffix("-kt")

            val gradle = directory.resolve("gradle")
            if (gradle.isDirectory) {
                for (file in gradle.listFiles() ?: emptyArray()) {
                    if (file.name.endsWith(".versions.toml")) {
                        dependencyResolutionManagement {
                            versionCatalogs {
                                create(file.name.removeSuffix(".versions.toml") + "Libs") {
                                    from(files(file))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

rootProject.name = "commons"

