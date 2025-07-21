package xyz.wagyourtail.commons.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.bundling.Jar
import org.gradle.process.ExecOperations
import javax.inject.Inject
import kotlin.jvm.java

abstract class AutoVersionConfig @Inject constructor(@get:Internal val project: Project) {

    @get:Inject
    internal abstract val execOperations: ExecOperations

    abstract val snapshot: Property<Boolean>

    abstract val version: Property<String>

    abstract val implementationVersion: Property<String>

    init {
        snapshot.finalizeValueOnRead()
        version.finalizeValueOnRead()
        implementationVersion.finalizeValueOnRead()
    }

    fun fromGit(mainBranchName: String? = null) {
        val describe = project.provider {
            execOperations.getStdout {
                it.commandLine("git", "describe", "--always", "--tags", "--first-parent")
            }
        }

        this.snapshot.set(project.provider {
            val currentBranch = execOperations.getStdout {
                it.commandLine("git", "branch", "--show-current")
            }

            if (mainBranchName == null && (currentBranch == "main" || currentBranch == "master")) {
                false
            } else if (currentBranch == mainBranchName) {
                false
            } else {
                true
            }
        })

        version.set(project.provider {
            if (describe.get().contains("-")) {
                if (snapshot.get()) {
                    "${describe.get().substringBefore("-")}-SNAPSHOT"
                } else {
                    describe.get().substringBeforeLast("-").replace("-", ".")
                }
            } else {
                if (snapshot.get()) {
                    "$describe"
                } else {
                    "$describe.0"
                }
            }
        })

        implementationVersion.set(project.provider {
            if (snapshot.get()) {
                describe.get().replaceFirst("-", ".")
            } else {
                version.get()
            }
        })
    }

    fun snapshotProperty(snapshot: Boolean = project.hasProperty("version_snapshot")) {
        this.snapshot.set(snapshot)
    }

    fun releaseProperty() {
        snapshot.set(!project.hasProperty("version_release"))
    }

    fun versionProperty(version: String = project.findProperty("version") as String) {
        this.version.set(version)

        this.implementationVersion.set(version)
    }

    fun gitHashInImplementationVersion(onlyIfSnapshot: Boolean = true) {
        implementationVersion.set(project.provider {
            if (!onlyIfSnapshot || snapshot.get()) {
                val gitHash = execOperations.getStdout {
                    it.commandLine("git", "rev-parse", "--short", "HEAD")
                }
                if (gitHash.isNotEmpty()) {
                    "${version.get()}-$gitHash"
                } else {
                    version.get()
                }
            } else {
                version.get()
            }
        })
    }

    fun apply(project: Project) {

        project.version = version.get() + (if (snapshot.get()) "-SNAPSHOT" else "")

        project.afterEvaluate {
            project.tasks.withType(Jar::class.java).configureEach { task ->
                task.manifest { mf ->
                    mf.attributes["Implementation-Version"] =
                        implementationVersion.get() + (if (snapshot.get()) "-SNAPSHOT" else "")
                }
            }
        }
    }

}