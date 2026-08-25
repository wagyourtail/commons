package xyz.wagyourtail.commons.gradle

import groovy.lang.Closure
import groovy.lang.DelegatesTo
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import org.gradle.api.initialization.ProjectDescriptor
import org.gradle.api.initialization.Settings
import org.gradle.api.tasks.Internal
import xyz.wagyourtail.commonskt.utils.removeSuffix
import java.io.File
import javax.inject.Inject

abstract class GradleSettingsExtension @Inject constructor(@get:Internal val settings: Settings) {

    val buildSrc: File = settings.rootDir.resolve("buildSrc")

    /**
     * auto-adds subprojects by searching for `build.gradle` and `build.gradle.kts`
     */
    @JvmOverloads
    fun autoSubprojects(
        rootDir: File = settings.layout.rootDirectory.asFile,
        configProject: ProjectDescriptor.() -> Unit = { }
    ) {
        for (directory in rootDir.listFiles() ?: emptyArray()) {
            if (directory.isDirectory) {
                if (directory.equals(buildSrc)) continue
                val groovy = directory.resolve("build.gradle").exists()
                val kts = directory.resolve("build.gradle.kts").exists()
                if (groovy || kts) {
                    settings.include(
                        directory.relativeTo(settings.layout.rootDirectory.asFile).path.replace(
                            File.separator,
                            ":"
                        )
                    )
                    settings.project(directory).apply {
                        autoVersionConfig(this)
                        this.configProject()
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

    /**
     * adds non-standard version catalogs
     *
     * @param project the target project
     * @param name the name closure to produce the name of the catalog, defaults to the first part of the file name + "Libs"
     *
     * @since 1.0.6
     */
    @JvmOverloads
    fun autoVersionConfig(
        project: ProjectDescriptor = settings.rootProject,
        name: ProjectDescriptor.(File) -> String = { it.name.removeSuffix(".versions.toml") + "Libs" }
    ) {
        val projectDir = project.projectDir
        val gradle = projectDir.resolve("gradle")
        if (gradle.isDirectory) {
            for (file in gradle.listFiles() ?: emptyArray()) {
                // skip default libs.versions.toml in root project
                if (project == settings.rootProject && file.name.equals("libs.versions.toml")) continue
                if (file.name.endsWith(".versions.toml")) {
                    settings.dependencyResolutionManagement {
                        it.versionCatalogs {
                            it.create(project.name(file)) {
                                it.from(settings.layout.rootDirectory.files(file.absoluteFile))
                            }
                        }
                    }
                }
            }
        }
    }

    @JvmOverloads
    fun autoVersionConfig(
        project: ProjectDescriptor = settings.rootProject,
        @DelegatesTo(
            value = ProjectDescriptor::class,
            strategy = Closure.DELEGATE_FIRST
        )
        @ClosureParams(
            value = SimpleType::class,
            options = ["java.io.File"]
        )
        configProject: Closure<*>
    ) {
        configProject.delegate = project
        configProject.resolveStrategy = Closure.DELEGATE_FIRST
        configProject.call()
    }

}