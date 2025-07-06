package xyz.wagyourtail.commons.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import xyz.wagyourtail.commonskt.properties.FinalizeOnRead

class GradleMain : Plugin<Project> {
    val version by FinalizeOnRead(GradleMain::class.java.`package`.implementationVersion ?: "unknown")

    override fun apply(project: Project) {
        project.commonsLogger.lifecycle("Loaded commons-gradle $version")
        project.extensions.create("commons", GradleExtension::class.java, project)
    }

}
