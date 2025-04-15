package xyz.wagyourtail.commons.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.PolymorphicDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.internal.AbstractTask
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.jvm.toolchain.JavaToolchainService
import xyz.wagyourtail.commonskt.maven.MavenCoords
import java.io.File
import kotlin.jvm.java

val Project.sourceSets
    get() = extensions.findByType(SourceSetContainer::class.java)!!

val Project.javaToolchains
    get() = extensions.findByType(JavaToolchainService::class.java)!!

fun Configuration.getFiles(dep: Dependency, filter: (File) -> Boolean): Set<File> {
    resolve()
    val files = mutableSetOf<File>()
    when (dep) {
        is ModuleDependency -> {
            val resolvedArtifacts = resolvedConfiguration.resolvedArtifacts
            for (artifact in dep.artifacts) {
                val group = dep.group
                val name = dep.name
                val version = dep.version
                val classifier = artifact.classifier
                val ext = artifact.extension
                for (resolvedArtifact in resolvedArtifacts) {
                    if (resolvedArtifact.moduleVersion.id.group == group &&
                        resolvedArtifact.moduleVersion.id.name == name &&
                        resolvedArtifact.moduleVersion.id.version == version &&
                        resolvedArtifact.classifier == classifier &&
                        resolvedArtifact.extension == ext
                    ) {
                        files.add(resolvedArtifact.file)
                    }
                }
            }
        }
        is FileCollectionDependency -> {
            for (file in dep.files) {
                files.add(file)
            }
        }
        else -> {
            throw IllegalArgumentException("Unsupported dependency type: ${dep::class.java}")
        }
    }
    return files.filter {filter(it) }.toSet()
}

fun Configuration.getFiles(dep: Dependency, extension: String = "jar"): Set<File> {
    return getFiles(dep) { it.extension == extension }
}

@Deprecated(message = "use sourceSet.getTaskName() instead", replaceWith = ReplaceWith("sourceSet.getTaskName(null, this)"))
fun String.withSourceSet(sourceSet: SourceSet) =
    if (sourceSet.name == "main") this else "${sourceSet.name}${this.capitalized()}"


fun ResolvedArtifactResult.getCoords(): MavenCoords {
    val owner = this.variant.owner

    var location = if (owner is ModuleComponentIdentifier) {
        MavenCoords(owner.group, owner.module, owner.version)
    } else {
        null
    }

    val capabilityLocations = this.variant.capabilities.map {
        MavenCoords(it.group, it.name, it.version)
    }

    if (!capabilityLocations.isEmpty() && (location == null || !capabilityLocations.contains(location))) {
        location = capabilityLocations[0]
    }

    if (location == null) {
        error("unknown dependency type ${this.variant.owner}")
    }

    val classifierPrefix = "${location.artifact}-${location.version}-"

    if (this.file.name.startsWith(classifierPrefix)) {
        location = MavenCoords(
            location.group!!,
            location.artifact,
            location.version,
            this.file.nameWithoutExtension.substring(classifierPrefix.length),
            this.file.extension
        )
    } else {
        location = MavenCoords(
            location.group!!,
            location.artifact,
            location.version,
            null,
            this.file.extension
        )
    }

    return location
}

fun <T> NamedDomainObjectContainer<T>.maybeRegister(name: String, action: T.() -> Unit = {}): NamedDomainObjectProvider<T> {
    return try {
        named(name) as NamedDomainObjectProvider<T>
    } catch (ex: UnknownTaskException) {
        register(name)
    }.also {
        it.configure(action)
    }
}

inline fun <reified S: T, T> PolymorphicDomainObjectContainer<T>.maybeRegister(name: String, noinline action: S.() -> Unit = {}): NamedDomainObjectProvider<S> {
    return try {
        named(name, S::class.java) as NamedDomainObjectProvider<S>
    } catch (ex: UnknownTaskException) {
        register(name, S::class.java)
    }.also {
        it.configure(action)
    }
}

val isIdeaSync: Boolean
    get() = System.getProperty("idea.sync.active", "false").toBoolean()
