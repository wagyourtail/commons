package xyz.wagyourtail.commons.gradle

import groovy.lang.Closure
import groovy.lang.DelegatesTo
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Internal
import org.gradle.internal.jvm.inspection.JavaInstallationCapability
import org.gradle.internal.jvm.inspection.JavaInstallationRegistry
import org.gradle.jvm.toolchain.JavaCompiler
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.jvm.toolchain.JavaToolchainSpec
import org.gradle.jvm.toolchain.JavadocTool
import org.gradle.jvm.toolchain.JvmImplementation
import org.gradle.api.JavaVersion
import java.io.File
import javax.inject.Inject

/**
 * Wrapper around [JavaToolchainService] whose queries select the best
 * *locally detected* JDK satisfying a [CapableToolchainSpec], instead of
 * requiring an exact `languageVersion` match.
 *
 * This lets builds ask for "any javac that can still emit Java 7"
 * (`compilerFor { supports(7) }`) or "any launcher in 17..22"
 * (`launcherFor { inRange(17, 22) }`) and reuse newer already-installed
 * JVMs — a matching detected installation is always preferred over
 * downloading a toolchain. Provisioning (foojay etc.) only kicks in when
 * nothing detected qualifies, via a single exact-version fallback spec.
 *
 * Detection reuses Gradle's own installation registry
 * (`org.gradle.internal.jvm.inspection.JavaInstallationRegistry`, internal
 * API); if that lookup fails the service degrades to considering only the
 * JVM running Gradle.
 */
abstract class CapableToolchainService @Inject constructor(
    @get:Internal
    val project: Project
) {

    @get:Inject
    abstract val objects: ObjectFactory

    @get:Inject
    abstract val providers: ProviderFactory

    @get:Inject
    abstract val toolchains: JavaToolchainService

    private val logger = project.logger

    /** create a [CapableToolchainSpec] and configure it with [action] */
    fun spec(action: Action<CapableToolchainSpec>): CapableToolchainSpec =
        objects.newInstance(CapableToolchainSpec::class.java).also { action.execute(it) }

    fun spec(
        @DelegatesTo(
            value = CapableToolchainSpec::class,
            strategy = Closure.DELEGATE_FIRST
        )
        action: Closure<*>
    ): CapableToolchainSpec = spec {
        action.delegate = it
        action.resolveStrategy = Closure.DELEGATE_FIRST
        action.call()
    }

    /**
     * Obtain a [JavaCompiler] satisfying [spec] — see [CapableToolchainSpec]
     * for how `supports`/`inRange`/`languageVersion` interact.
     */
    fun compilerFor(action: Action<CapableToolchainSpec>): Provider<JavaCompiler> =
        compilerFor(spec(action))

    fun compilerFor(
        @DelegatesTo(
            value = CapableToolchainSpec::class,
            strategy = Closure.DELEGATE_FIRST
        )
        action: Closure<*>
    ): Provider<JavaCompiler> = compilerFor(spec(action))

    fun compilerFor(spec: CapableToolchainSpec): Provider<JavaCompiler> =
        resolvedSpec(spec, JavaInstallationCapability.JAVA_COMPILER)
            .flatMap { toolchains.compilerFor(it) }

    /**
     * Obtain a [JavaLauncher] satisfying [spec]. For launchers, `supports(n)`
     * means the JVM major is at least `n` (it can run `n` bytecode).
     */
    fun launcherFor(action: Action<CapableToolchainSpec>): Provider<JavaLauncher> =
        launcherFor(spec(action))

    fun launcherFor(
        @DelegatesTo(
            value = CapableToolchainSpec::class,
            strategy = Closure.DELEGATE_FIRST
        )
        action: Closure<*>
    ): Provider<JavaLauncher> = launcherFor(spec(action))

    fun launcherFor(spec: CapableToolchainSpec): Provider<JavaLauncher> =
        resolvedSpec(spec, null)
            .flatMap { toolchains.launcherFor(it) }

    fun javadocToolFor(action: Action<CapableToolchainSpec>): Provider<JavadocTool> =
        javadocToolFor(spec(action))

    fun javadocToolFor(
        @DelegatesTo(
            value = CapableToolchainSpec::class,
            strategy = Closure.DELEGATE_FIRST
        )
        action: Closure<*>
    ): Provider<JavadocTool> = javadocToolFor(spec(action))

    fun javadocToolFor(spec: CapableToolchainSpec): Provider<JavadocTool> =
        resolvedSpec(spec, JavaInstallationCapability.JAVADOC_TOOL)
            .flatMap { toolchains.javadocToolFor(it) }

    /**
     * The toolchain major version that would be picked for [spec] and [tool].
     * Absent when the spec carries no version constraints at all.
     */
    fun pickedLanguageVersion(spec: CapableToolchainSpec): Provider<Int> =
        providers.provider { pickVersion(spec, JavaInstallationCapability.JAVA_COMPILER) }

    /**
     * The `javac --release` value matching [spec]'s `supports` target, when
     * the picked toolchain actually supports `--release` (JDK 9+).
     *
     * The provider is absent when nothing should be passed — either `supports`
     * is unset or the picked JDK is 8 or older (use `sourceCompatibility`/
     * `targetCompatibility` there; `--release` doesn't exist on JDK 8).
     * Wire it on a [org.gradle.api.tasks.compile.JavaCompile] like:
     *
     * ```
     * val spec = capableToolchains.spec { supports(7) }
     * tasks.withType<JavaCompile>().configureEach {
     *     javaCompiler.set(capableToolchains.compilerFor(spec))
     *     options.release.set(capableToolchains.releaseOptionFor(spec))
     * }
     * ```
     */
    fun releaseOptionFor(spec: CapableToolchainSpec): Provider<Int> =
        providers.provider { pickVersion(spec, JavaInstallationCapability.JAVA_COMPILER) }
            .flatMap { major ->
                if (major >= 9) spec.supports.map { it.asInt() }
                else providers.provider<Int> { null }
            }

    // --------------------------------------------------------------------

    /** lazily resolved spec — detection runs at provider query time */
    private fun resolvedSpec(
        spec: CapableToolchainSpec,
        tool: JavaInstallationCapability?
    ): Provider<Action<JavaToolchainSpec>> = providers.provider {
        val picked = pickVersion(spec, tool)
        Action<JavaToolchainSpec> {
            picked?.let { v -> it.languageVersion.set(JavaLanguageVersion.of(v)) }
            spec.vendor.orNull?.let { v -> it.vendor.set(v) }
            spec.implementation.orNull?.let { v -> it.implementation.set(v) }
        }
    }

    /**
     * Choose the toolchain major satisfying [spec] from detected
     * installations. Returns `null` only when the spec has no version
     * constraints — meaning "any toolchain" (Gradle picks its preferred,
     * normally the JVM running the build). Otherwise the result is either a
     * detected major (guaranteed local hit) or a fallback major handed to
     * the normal exact-match/provisioning path.
     */
    private fun pickVersion(spec: CapableToolchainSpec, tool: JavaInstallationCapability?): Int? {
        spec.languageVersion.orNull?.let { return it.asInt() }

        val supports = spec.supports.orNull?.asInt()
        val min = spec.minVersion.orNull?.asInt()
        val max = spec.maxVersion.orNull?.asInt()
        if (supports == null && min == null && max == null) return null

        val compiler = tool == JavaInstallationCapability.JAVA_COMPILER
        val candidates = installations().filter { inst ->
            (tool == null || tool in inst.capabilities) &&
                (min == null || inst.major >= min) &&
                (max == null || inst.major <= max) &&
                (supports == null ||
                    if (compiler) javacCanEmit(inst.major, supports) else inst.major >= supports) &&
                (spec.vendor.orNull?.matches(inst.vendor ?: "") ?: true) &&
                implementationMatches(spec.implementation.orNull, inst)
        }

        // the JVM running Gradle first — zero toolchain switching
        candidates.firstOrNull { it.isCurrent }?.let { return it.major }

        if (compiler && supports != null) {
            // prefer the newest javac with real --release support for the
            // target; then the exact target; then anything else capable
            // (e.g. JDK 8 via -source/-target)
            return candidates.filter { it.major >= 9 }.maxByOrNull { it.major }?.major
                ?: candidates.firstOrNull { it.major == supports }?.major
                ?: candidates.minByOrNull { it.major }?.major
                ?: supports
        }

        // launchers/javadoc/plain ranges: lowest qualifying major
        return candidates.minByOrNull { it.major }?.major
            ?: supports ?: min
    }

    private fun implementationMatches(impl: JvmImplementation?, inst: DetectedJvm): Boolean = when {
        impl == null -> true
        impl == JvmImplementation.VENDOR_SPECIFIC ->
            JavaInstallationCapability.J9_VIRTUAL_MACHINE !in inst.capabilities
        impl == JvmImplementation.J9 ->
            JavaInstallationCapability.J9_VIRTUAL_MACHINE in inst.capabilities ||
                (inst.jvmName?.contains("J9", ignoreCase = true) == true) ||
                (inst.jvmName?.contains("OpenJ9", ignoreCase = true) == true)
        else -> true
    }

    // --------------------------------------------------------------------
    // detection (internal Gradle API — degrade gracefully)

    private data class DetectedJvm(
        val major: Int,
        val home: File,
        val vendor: String?,
        val jvmName: String?,
        val capabilities: Set<JavaInstallationCapability>,
        val isCurrent: Boolean
    )

    private val detectedInstallations: List<DetectedJvm> by lazy { detectInstallations() }

    private fun installations(): List<DetectedJvm> = detectedInstallations

    private fun detectInstallations(): List<DetectedJvm> {
        val currentHome = File(System.getProperty("java.home")).canonicalFile
        val currentHomeAlt =
            if (currentHome.name == "jre") currentHome.parentFile?.canonicalFile else null

        fun isCurrent(home: File): Boolean {
            val c = home.canonicalFile
            return c == currentHome || c == currentHomeAlt
        }

        val detected = try {
            (project as ProjectInternal).services
                .get(JavaInstallationRegistry::class.java)
                .toolchains()
                .filter { it.metadata.isValidInstallation }
                .map {
                    DetectedJvm(
                        major = it.metadata.javaMajorVersion,
                        home = it.metadata.javaHome.toFile(),
                        vendor = it.metadata.vendor.rawVendor,
                        jvmName = it.metadata.jvmName,
                        capabilities = it.metadata.capabilities,
                        isCurrent = isCurrent(it.metadata.javaHome.toFile())
                    )
                }
        } catch (e: Throwable) {
            logger.warn(
                "[commons-gradle] could not enumerate detected java installations " +
                    "(${e::class.java.simpleName}: ${e.message}); " +
                    "only the JVM running Gradle will be considered"
            )
            emptyList()
        }

        // make sure the JVM running Gradle is always a candidate, even when
        // the registry lookup failed or skipped it
        val current = DetectedJvm(
            major = JavaVersion.current().majorVersion.toInt(),
            home = currentHome,
            vendor = System.getProperty("java.vendor"),
            jvmName = System.getProperty("java.vm.name"),
            capabilities = JavaInstallationCapability.JDK_CAPABILITIES,
            isCurrent = true
        )
        return if (detected.any { it.isCurrent }) detected else detected + current
    }

    companion object {
        /**
         * Whether javac of major [toolchainMajor] can emit bytecode/language
         * level [target] — via `--release` on JDK 9+, via `-source`/`-target`
         * on JDK ≤ 8.
         *
         * `javac --release` floors: 6 on JDK 9-11, 7 on JDK 12-19, 8 on
         * JDK 20+. Approximation — floors rise as javac retires old targets.
         */
        @JvmStatic
        fun javacCanEmit(toolchainMajor: Int, target: Int): Boolean {
            if (toolchainMajor < target) return false
            return when {
                toolchainMajor <= 8 -> true
                toolchainMajor <= 11 -> target >= 6
                toolchainMajor <= 19 -> target >= 7
                else -> target >= 8
            }
        }
    }

}
