package xyz.wagyourtail.commons.gradle

import org.gradle.api.*
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.internal.artifacts.configurations.Configurations
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.jvm.toolchain.JavaToolchainService
import xyz.wagyourtail.commonskt.maven.MavenCoords
import java.io.File
import kotlin.jvm.java
import kotlin.reflect.KClass

val Project.sourceSets
    get() = extensions.getByType(SourceSetContainer::class.java)

val Project.javaToolchains
    get() = extensions.getByType(JavaToolchainService::class.java)

val Project.base
    get() = extensions.getByType(BasePluginExtension::class.java)

val Project.java
    get() = extensions.findByType(JavaPluginExtension::class.java)

val Project.publishing
    get() = extensions.findByType(PublishingExtension::class.java)

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
    return files.filter { filter(it) }.toSet()
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
    } catch (_: UnknownTaskException) {
        register(name)
    }.also {
        it.configure(action)
    }
}

fun <S: T, T: Any> PolymorphicDomainObjectContainer<T>.maybeRegister(name: String, classType: KClass<S>, action: S.() -> Unit = {}): NamedDomainObjectProvider<S> {
    return try {
        named(name, classType.java) as NamedDomainObjectProvider<S>
    } catch (_: UnknownTaskException) {
        register(name, classType.java)
    }.also {
        it.configure(action)
    } as NamedDomainObjectProvider<S>
}

fun <S: T, T: Any> PolymorphicDomainObjectContainer<T>.maybeRegister(name: String, classType: Class<S>, action: S.() -> Unit = {}): NamedDomainObjectProvider<S> {
    return try {
        named(name, classType) as NamedDomainObjectProvider<S>
    } catch (_: UnknownTaskException) {
        register(name, classType)
    }.also {
        it.configure(action)
    } as NamedDomainObjectProvider<S>
}

inline fun <reified S: T, T> PolymorphicDomainObjectContainer<T>.maybeRegister(name: String, noinline action: S.() -> Unit = {}): NamedDomainObjectProvider<S> {
    return try {
        named(name, S::class.java) as NamedDomainObjectProvider<S>
    } catch (_: UnknownTaskException) {
        register(name, S::class.java)
    }.also {
        it.configure(action)
    }
}

val isIdeaSync: Boolean
    get() = System.getProperty("idea.sync.active", "false").toBoolean()

infix fun SourceSet.includesFrom(other: SourceSet) {
    compileClasspath += other.output + other.compileClasspath
    runtimeClasspath += other.output + other.runtimeClasspath
}

infix fun SourceSet.extendsDependenciesFrom(other: SourceSet) {
    compileClasspath += other.compileClasspath
    runtimeClasspath += other.runtimeClasspath
}