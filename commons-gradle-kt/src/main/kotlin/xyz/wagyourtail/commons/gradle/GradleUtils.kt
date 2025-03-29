package xyz.wagyourtail.commons.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.jvm.toolchain.JavaToolchainService
import java.io.File

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

fun String.withSourceSet(sourceSet: SourceSet) =
    if (sourceSet.name == "main") this else "${sourceSet.name}${this.capitalized()}"
