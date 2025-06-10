package xyz.wagyourtail.commons.gradle

import org.gradle.api.Project
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import javax.inject.Inject
import kotlin.jvm.java

abstract class GradleExtension @Inject constructor(@get:Internal val project: Project) {

    @JvmOverloads
    fun autoName(baseName: String = project.rootProject.name, projectName: (Project) -> String = { it.name }) {
        project.base.apply {
            archivesName.set(
                if (project == project.rootProject) {
                    baseName
                } else {
                    buildString {
                        append(project.name)
                        var current: Project? = project.parent
                        while (current != null) {
                            append(0, "-")
                            if (current == project.rootProject) {
                                append(0, baseName)
                            } else {
                                append(0, projectName(current))
                            }
                            current = current.parent
                        }
                    }
                }
            )
        }
    }

    @JvmOverloads
    fun autoSnapshot(version: String, defaultSnapshot: Boolean = false) {
        project.version = if (defaultSnapshot) {
            if (project.hasProperty("version_snapshot")) {
                "$version-SNAPSHOT"
            } else {
                version
            }
        } else {
            if (project.hasProperty("version_release")) {
                version
            } else {
                "$version-SNAPSHOT"
            }
        }
    }

    @JvmOverloads
    fun autoToolchain(mainVersion: Int, testVersion: Int = mainVersion) {
        project.java.apply {
            toolchain {
                it.languageVersion.set(JavaLanguageVersion.of(mainVersion))
            }
        }
        if (mainVersion != testVersion) {
            project.tasks.named("compileTestJava", JavaCompile::class.java) { task ->
                task.javaCompiler.set(project.javaToolchains.compilerFor {
                    it.languageVersion.set(JavaLanguageVersion.of(testVersion))
                })
            }

            project.tasks.named("test", Test::class.java) { task ->
                task.javaLauncher.set(project.javaToolchains.launcherFor {
                    it.languageVersion.set(JavaLanguageVersion.of(testVersion))
                })
            }
        }
    }

}