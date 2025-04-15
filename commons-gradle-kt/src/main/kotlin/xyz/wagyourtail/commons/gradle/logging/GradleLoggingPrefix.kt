package xyz.wagyourtail.commons.gradle.logging

import xyz.wagyourtail.commons.core.logger.Logger
import xyz.wagyourtail.commons.core.logger.prefix.DefaultLoggingPrefix

class GradleLoggingPrefix(
    val projectPath: String,
    loggerName: String = getCallingClass().simpleName,
    includeTime: Boolean = false,
    includeLevel: Boolean = false,
    includeClassName: Boolean = false,
    includeThreadName: Boolean = false
) : DefaultLoggingPrefix(
    loggerName,
    includeTime,
    includeLevel,
    includeClassName,
    includeThreadName
) {

    override fun getPrefix(level: Logger.Level?): String {
        val prefix = super.getPrefix(level)
        return "$prefix [$projectPath]"
    }

}