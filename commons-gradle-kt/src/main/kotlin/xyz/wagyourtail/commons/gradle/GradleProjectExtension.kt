package xyz.wagyourtail.commons.gradle

import groovy.lang.Closure
import groovy.lang.DelegatesTo
import org.gradle.api.Project
import org.gradle.api.component.SoftwareComponentFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.process.ExecOperations
import xyz.wagyourtail.commonskt.string.NameType
import xyz.wagyourtail.commonskt.string.convertNameType
import javax.inject.Inject
import javax.inject.Provider
import kotlin.jvm.java

abstract class GradleProjectExtension @Inject constructor(@get:Internal val project: Project) {

    @get:Inject
    abstract val execOperations: ExecOperations

    @get:Inject
    abstract val softwareComponentFactory: SoftwareComponentFactory

    fun autoGroup(group: String = project.findProperty("maven_group") as String, includeSubprojects: Boolean = true) {
        project.group = group
        if (includeSubprojects) project.subprojects { it.group = group }
    }

    @JvmOverloads
    fun autoName(baseName: String = project.rootProject.name.convertNameType(NameType.PASCAL_CASE, NameType.KEBAB_CASE), includeSubprojects: Boolean = true, projectName: (Project) -> String = { it.name }) {
        project.base.archivesName.set(project.provider {
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
        })

        if (includeSubprojects) {
            val nameCache = mutableMapOf<String, Property<String>>()
            nameCache[project.path] = project.base.archivesName
            project.subprojects {
                project.base.archivesName.set(project.provider {
                    buildString {
                        append(project.name)
                        var current: Project? = project.parent
                        while (current != null) {
                            append(0, "-")
                            if (current.path in nameCache) {
                                append(0, nameCache.getValue(current.path).get())
                                break
                            } else if (current == project.rootProject) {
                                append(0, baseName)
                            } else {
                                append(0, projectName(current))
                            }
                            current = current.parent
                        }
                    }
                })
                nameCache[it.path] = it.base.archivesName
            }
        }
    }

    @JvmOverloads
    fun autoVersion(includeSubprojects: Boolean = true, builder: AutoVersionConfig.() -> Unit) {
        val autoVersion = project.objects.newInstance(AutoVersionConfig::class.java, project)

        autoVersion.builder()

        autoVersion.apply(project)
        if (includeSubprojects) project.subprojects(autoVersion::apply)

    }

    @JvmOverloads
    fun autoVersion(
        includeSubprojects: Boolean = true,
        @DelegatesTo(
            value = AutoVersionConfig::class,
            strategy = Closure.DELEGATE_FIRST
        )
        builder: Closure<*>
    ) {
        autoVersion(includeSubprojects) {
            builder.delegate = this
            builder.resolveStrategy = Closure.DELEGATE_FIRST
            builder.call()
        }
    }

    /**
     * create a tag with 2 parts, ie `1.0` and this will automatically produce `1.0.0` where the last part is the number of commits since the last tag.
     * snapshot will be set if the current branch is not the main branch, it will produce `1.0-SNAPSHOT` with an implementation version of the full git describe
     */
    @JvmOverloads
    fun autoVersionFromGit(mainBranchName: String? = null) {
        autoVersion {
            fromGit(mainBranchName)
        }
    }

    /**
     * create a tag with 2 parts, ie `1.0` and this will automatically produce `1.0.0` where the last part is the number of commits since the last tag.
     * if snapshot is true, it will produce `1.0-SNAPSHOT` with an implementation version of the full git describe
     */
    fun autoVersionFromGit(snapshot: Boolean) {
        autoVersion {
            fromGit()
            snapshotProperty(snapshot)
        }
    }

    /**
     * sets the version of the project, automatically adding the `-SNAPSHOT` if the proper gradle property is set,
     * and adding the git shorthash to the implementation version
     */
    @JvmOverloads
    fun autoVersion(version: String = project.findProperty("version") as String, defaultSnapshot: Boolean = false) {
        val isSnapshot = if (defaultSnapshot) !project.hasProperty("version_release") else project.hasProperty("version_snapshot")
        autoVersion {
            versionProperty(version)
            snapshotProperty(isSnapshot)
            gitHashInImplementationVersion()
        }
    }

    @JvmOverloads
    fun autoToolchain(mainVersion: Int, testVersion: Int = mainVersion) {
        project.java!!.apply {
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