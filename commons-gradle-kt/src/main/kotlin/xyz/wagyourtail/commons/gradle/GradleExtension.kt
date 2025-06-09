package xyz.wagyourtail.commons.gradle

import org.gradle.api.Project
import org.gradle.api.tasks.Internal
import javax.inject.Inject

abstract class GradleExtension @Inject constructor(@get:Internal val project: Project) {

    @JvmOverloads
    fun autoName(baseName: String = project.rootProject.name) {
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
                                append(0, current.name)
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

}