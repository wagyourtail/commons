java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(7))
        vendor.set(JvmVendorSpec.AZUL)
    }
}

description = "Core utilities"

dependencies {
    compileOnly(libs.jetbrains.annotations.j5)
    testImplementation(libs.asm)
    testImplementation(libs.asm.tree)
}