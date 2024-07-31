package xyz.wagyourtail.commons.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.jvm.toolchain.JavaToolchainService
import java.io.File

val Project.sourceSets
    get() = extensions.findByType(SourceSetContainer::class.java)!!

val Project.javaToolchains
    get() = extensions.findByType(JavaToolchainService::class.java)!!

/**
 * ignores the version and classifier, because gradle's dumb and deprecated
 * [Configuration.files] without a proper replacement.
 */
fun Configuration.getFiles(dep: Dependency, filter: (File) -> Boolean): FileCollection {
    resolve()
    return incoming.artifactView { view ->
        when (dep) {
            is ModuleDependency -> {
                view.componentFilter {
                    when (it) {
                        is ModuleComponentIdentifier -> {
                            it.group == dep.group && it.module == dep.name
                        }
                        is ComponentArtifactIdentifier -> {
                            false
                        }
                        else -> {
                            println("Unknown component type: ${it.javaClass}")
                            false
                        }
                    }
                }
            }
            is FileCollectionDependency -> {
                view.componentFilter { comp ->
                    when (comp) {
                        is ModuleComponentIdentifier -> {
                            false
                        }
                        is ComponentIdentifier -> {
                            dep.files.any { it.name == comp.displayName }
                        }
                        else -> {
                            println("Unknown component type: ${comp.javaClass}")
                            false
                        }
                    }
                }
            }
            else -> {
                throw IllegalArgumentException("Unknown dependency type: ${dep.javaClass}")
            }
        }
    }.files.filter(filter)
}

fun Configuration.getFiles(dep: Dependency, extension: String = "jar"): FileCollection {
    return getFiles(dep) { it.extension == extension }
}


