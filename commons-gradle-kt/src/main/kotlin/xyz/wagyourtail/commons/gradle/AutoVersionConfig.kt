package xyz.wagyourtail.commons.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.provider.Provider
import javax.inject.Inject

abstract class AutoVersionConfig @Inject constructor(val project: Project) {

    abstract val snapshot: Property<Boolean>

    abstract val version: Property<String>

    abstract val implementationVersion: Property<String>

    init {
        snapshot.finalizeValueOnRead()
        version.finalizeValueOnRead()
        implementationVersion.finalizeValueOnRead()
    }

    /** CC-safe git stdout — providers.exec defers the external
     *  process into tracked provider evaluation. */
    private fun gitOut(vararg args: String): Provider<String> =
        project.providers.exec { it.commandLine("git", *args) }
            .standardOutput.asText.map(String::trim)

    fun fromGit(mainBranchName: String? = null) {
        val describe = gitOut("describe", "--always", "--tags", "--first-parent")
        val currentBranch = gitOut("branch", "--show-current")

        this.snapshot.set(project.provider {
            val branch = currentBranch.get()
            if (mainBranchName == null && (branch == "main" || branch == "master")) {
                false
            } else if (branch == mainBranchName) {
                false
            } else {
                true
            }
        })

        version.set(project.provider {
            if (describe.get().contains("-")) {
                if (snapshot.get()) {
                    describe.get().substringBefore("-")
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

    fun versionProperty(version: String = project.property("version") as String) {
        this.version.set(version)
        this.implementationVersion.set(version)
    }

    fun gitHashInImplementationVersion(onlyIfSnapshot: Boolean = true) {
        val gitHash = gitOut("rev-parse", "--short", "HEAD")
        implementationVersion.set(project.provider {
            if (!onlyIfSnapshot || snapshot.get()) {
                val hash = gitHash.get()
                if (hash.isNotEmpty()) {
                    "${version.get()}-$hash"
                } else {
                    version.get()
                }
            } else {
                version.get()

            }
        })
    }

    private fun String.snapshotSuffix() = if (!snapshot.get() || endsWith("-SNAPSHOT")) {
        this
    } else {
        "$this-SNAPSHOT"
    }


    fun apply(project: Project, subprojects: Boolean = true) {
        project.version = this.version.get().snapshotSuffix()

        applyImplementationVersion(project)
        if (subprojects) {
            project.subprojects {
                it.version = project.version

                applyImplementationVersion(it)
            }
        }
    }

    fun applyImplementationVersion(project: Project) {
        project.afterEvaluate {
            project.tasks.withType(Jar::class.java).configureEach { task ->
                task.manifest { mf ->
                    mf.attributes["Implementation-Version"] = implementationVersion.get().snapshotSuffix()
                }
            }
        }
    }

}