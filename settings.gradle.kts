plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include("commons-gradle")
include("commons-java7")

include("commons-kt")
include("commons-compress-kt")

rootProject.name = "commons"

