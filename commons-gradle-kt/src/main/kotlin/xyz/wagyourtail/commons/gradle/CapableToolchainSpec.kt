package xyz.wagyourtail.commons.gradle

import org.gradle.api.provider.Property
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainSpec
import org.gradle.jvm.toolchain.JvmImplementation
import org.gradle.jvm.toolchain.JvmVendorSpec

/**
 * Spec for [CapableToolchainService] queries.
 *
 * Unlike [JavaToolchainSpec], the requested language version does not have to
 * be an exact toolchain match — [supports] and [inRange] select the best
 * *locally detected* JDK that satisfies the constraint, falling back to a
 * provisioned toolchain only when no detected installation qualifies.
 *
 * Semantics per tool:
 *  - `compilerFor`: [supports] means "javac can emit this bytecode target"
 *    (e.g. `--release 7` works on JDK 9-19), so any detected JDK able to
 *    compile to the target may be selected.
 *  - `launcherFor` / `javadocToolFor`: [supports] means the JVM/tool major
 *    version is at least the given version.
 *
 * [languageVersion] remains an exact pin — when set it behaves exactly like
 * [JavaToolchainSpec.getLanguageVersion] (and wins over [supports]/[inRange]).
 */
abstract class CapableToolchainSpec {

    /** exact toolchain language version; wins over [supports]/[inRange] when set */
    abstract val languageVersion: Property<JavaLanguageVersion>

    /** bytecode/language target the selected tool must support */
    abstract val supports: Property<JavaLanguageVersion>

    /** minimum toolchain major version, inclusive */
    abstract val minVersion: Property<JavaLanguageVersion>

    /** maximum toolchain major version, inclusive */
    abstract val maxVersion: Property<JavaLanguageVersion>

    /** forwarded to the resolved [JavaToolchainSpec] and used to filter detected candidates */
    abstract val vendor: Property<JvmVendorSpec>

    /** forwarded to the resolved [JavaToolchainSpec] and used to filter detected candidates */
    abstract val implementation: Property<JvmImplementation>

    fun supports(version: Int) {
        supports.set(JavaLanguageVersion.of(version))
    }

    fun supports(version: JavaLanguageVersion) {
        supports.set(version)
    }

    /** restrict candidates to toolchain majors in `[min] .. [max]`, inclusive */
    @JvmOverloads
    fun inRange(min: Int, max: Int = Int.MAX_VALUE) {
        minVersion.set(JavaLanguageVersion.of(min))
        maxVersion.set(JavaLanguageVersion.of(max))
    }

    fun inRange(min: JavaLanguageVersion, max: JavaLanguageVersion) {
        minVersion.set(min)
        maxVersion.set(max)
    }

}
