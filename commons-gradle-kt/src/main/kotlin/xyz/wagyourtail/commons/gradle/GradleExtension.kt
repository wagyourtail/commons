package xyz.wagyourtail.commons.gradle

import org.gradle.api.Project
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.process.ExecOperations
import xyz.wagyourtail.commonskt.string.NameType
import xyz.wagyourtail.commonskt.string.convertNameType
import xyz.wagyourtail.commonskt.utils.firstAndMaybeLast
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import kotlin.jvm.java

abstract class GradleExtension @Inject constructor(@get:Internal val project: Project) {

    @get:Inject
    abstract val execOperations: ExecOperations

    fun autoGroup(group: String = project.findProperty("maven_group") as String) {
        project.group = group
    }

    @JvmOverloads
    fun autoName(baseName: String = project.rootProject.name.convertNameType(NameType.PASCAL_CASE, NameType.KEBAB_CASE), projectName: (Project) -> String = { it.name }) {
        project.base.archivesName.set(
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

    @JvmOverloads
    fun autoVersion(version: String = project.findProperty("version") as String, defaultSnapshot: Boolean = false) {
        val isSnapshot = if (defaultSnapshot) project.hasProperty("version_snapshot") else !project.hasProperty("version_release")

        val versionList = buildList {
            add(version)
            if (isSnapshot) {
                val stdout = ByteArrayOutputStream()

                execOperations.exec {
                    it.commandLine("git", "rev-parse", "--short", "HEAD")
                    it.standardOutput = stdout
                }.rethrowFailure().assertNormalExitValue()

                val gitHash = stdout.toString().trim()
                if (gitHash.isNotEmpty()) {
                    add(gitHash)
                }

                add("SNAPSHOT")
            }
        }

        project.version = versionList.firstAndMaybeLast().joinToString("-")

        project.afterEvaluate {
            project.tasks.withType(Jar::class.java).forEach {
                it.manifest { mf ->
                    mf.attributes["Implementation-Version"] = versionList.joinToString("-")
                }
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