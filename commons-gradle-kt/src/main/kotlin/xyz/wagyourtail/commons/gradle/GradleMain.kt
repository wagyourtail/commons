package xyz.wagyourtail.commons.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.PluginAware
import xyz.wagyourtail.commonskt.properties.FinalizeOnRead

class GradleMain : Plugin<PluginAware> {
    val version by FinalizeOnRead(GradleMain::class.java.`package`.implementationVersion ?: "unknown")
    val logger = Logging.getLogger(GradleMain::class.java)

    init {
        logger.lifecycle("Loaded commons-gradle $version")
    }

    override fun apply(target: PluginAware) {
        when (target) {
            is Project -> apply(target)
            is Settings -> apply(target)
            else -> throw IllegalArgumentException("Cannot apply plugin to $target")
        }
    }

    fun apply(project: Project) {
        project.extensions.create("commons", GradleProjectExtension::class.java, project)
    }

    fun apply(settings: Settings) {
        settings.extensions.create("commons", GradleSettingsExtension::class.java, settings)
    }

}
