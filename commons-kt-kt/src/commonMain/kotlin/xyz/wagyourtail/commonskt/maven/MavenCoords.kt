package xyz.wagyourtail.commonskt.maven

import kotlin.jvm.JvmInline

@JvmInline
value class MavenCoords(val value: String) {

    constructor(group: String = "", artifact: String, version: String? = null, classifier: String? = null, extension: String? = null): this(buildString {
        append(group)
        append(':')
        append(artifact)
        if (version != null) {
            append(':')
            append(version)
        }
        if (classifier != null) {
            if (version == null) throw IllegalArgumentException("Cannot have classifier without version")
            append(':')
            append(classifier)
        }
        if (extension != null) {
            append('@')
            append(extension)
        }
    })

    val parts: List<String>
        get() = value.substringBeforeLast('@').split(":", limit = 4)

    val group: String?
        // ["", "artifact"] = null
        // ["@extension", "artifact@extension"] = null
        // ["group:artifact" ... "group:artifact:version:classifier" ...] = "group"
        // ["group:artifact@extension" ... "group:artifact:version:classifier@extension" ...] = "group"
        get() = if (parts.size < 2) null else parts[0]

    val artifact: String
        // ["", "@extension"] = ""
        // ["artifact", "artifact@extension"] = "artifact"
        // ["group:artifact" ... "group:artifact:version:classifier" ...] = "artifact"
        // ["group:artifact@extension" ... "group:artifact:version:classifier@extension" ...] = "artifact"
        get() = if (parts.size < 2) value else parts[1]

    val version: String?
        // ["" ... "group:artifact" ...] = null
        // ["@extension" ... "group:artifact@extension" ...] = null
        // ["group:artifact:version", "group:artifact:version:classifier" ...] = "version"
        // ["group:artifact:version@extension", "group:artifact:version:classifier" ...] = "version"
        get() = parts.getOrNull(2)

    val classifier: String?
        // ["" ... "group:artifact:version" ...] = null
        // ["@extension" ... "group:artifact:version@extension" ...] = null
        // ["group:artifact:version:classifier" ...] = "classifier"
        // ["group:artifact:version:classifier@extension" ...] = "classifier"
        get() = parts.getOrNull(3)

    val extension: String
        // ["" ... "group:artifact:version:classifier" ...] = "jar"
        // "@extension" ... "group:artifact:version:classifier@extension" = "extension"
        get() = value.substringAfterLast('@', "jar")


    val fileName: String
        // [("group:artifact")] = "artifact-unspecified.jar"
        // [("group:artifact:version")] = "artifact-version.jar"
        // [("group:artifact") { classifier = "classifier" }] = "artifact-unspecified-classifier.jar"
        // [("group:artifact:version:classifier")] = "artifact-version-classifier.jar"
        // [("group:artifact@extension")] = "artifact-unspecified.extension"
        // [("group:artifact:version@extension")] = "artifact-version.extension"
        // [("group:artifact@extension") { classifier = "classifier" }] = "artifact-unspecified-classifier.extension"
        // [("group:artifact:version:classifier@extension")] = "artifact-version-classifier.extension"
        get() = buildString {
            append(artifact)
            append('-')
            if (version == null) {
                append("unspecified")
            } else {
                append(version)
            }
            if (classifier != null) {
                append('-')
                append(classifier)
            }
            append('.')
            append(extension)
        }

    override fun toString() = value

}
