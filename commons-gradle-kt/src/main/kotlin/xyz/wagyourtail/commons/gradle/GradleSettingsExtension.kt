package xyz.wagyourtail.commons.gradle

import groovy.lang.Closure
import groovy.lang.DelegatesTo
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings
import org.gradle.api.tasks.Internal
import java.io.File
import javax.inject.Inject

abstract class GradleSettingsExtension @Inject constructor(@get:Internal val settings: Settings) {

    val buildSrc: File = settings.rootDir.resolve("buildSrc")

    @JvmOverloads
    fun autoSubprojects(rootDir: File = settings.rootDir, configProject: ProjectDescriptor.() -> Unit = {}) {
        for (directory in rootDir.listFiles() ?: emptyArray()) {
            if (directory.isDirectory) {
                if (directory.equals(buildSrc)) continue
                val groovy = directory.resolve("build.gradle").exists()
                val kts = directory.resolve("build.gradle.kts").exists()
                if (groovy || kts) {
                    settings.include(directory.relativeTo(settings.rootDir).path.replace(File.separator, ":"))
                    settings.project(directory).apply(configProject)

                    val gradle = directory.resolve("gradle")
                    if (gradle.isDirectory) {
                        for (file in gradle.listFiles() ?: emptyArray()) {
                            if (file.name.endsWith(".versions.toml")) {
                                settings.dependencyResolutionManagement {
                                    it.versionCatalogs {
                                        it.create(file.name.removeSuffix(".versions.toml") + "Libs") {
                                            it.from(settings.layout.rootDirectory.files(file.absoluteFile))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    autoSubprojects(directory)
                }
            }
        }
    }

    fun autoSubprojects(
        rootDir: File = settings.rootDir,
        @DelegatesTo(
            value = ProjectDescriptor::class,
            strategy = Closure.DELEGATE_FIRST
        )
        configProject: Closure<*>
    ) {
        autoSubprojects(rootDir) {
            configProject.delegate = this
            configProject.resolveStrategy = Closure.DELEGATE_FIRST
            configProject.call()
        }
    }

}