package xyz.wagyourtail.commons.gradle

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logging
import xyz.wagyourtail.commons.core.logger.Logger
import xyz.wagyourtail.commons.core.logger.prefix.DefaultLoggingPrefix.getCallingClass
import xyz.wagyourtail.commons.core.logger.prefix.LoggingPrefix
import xyz.wagyourtail.commons.gradle.logging.GradleLoggingPrefix

val Task.commonsLogger
    get() = GradleLogWrapper(this.path.substringBeforeLast(":").ifEmpty { ":" }, this.name, this.logger)

val Project.commonsLogger
    get() = GradleLogWrapper(this.path, this.name, this.logger)

class GradleLogWrapper(val prefix: LoggingPrefix, val logger: org.gradle.api.logging.Logger) : Logger() {

    constructor(path: String, name: String?, logger: org.gradle.api.logging.Logger) : this(
        GradleLoggingPrefix(path, name ?: getCallingClass().simpleName),
        logger
    )

    override fun subLogger(subloggerName: String): Logger {
        return GradleLogWrapper(prefix.subSupplier(subloggerName), logger)
    }

    fun mapLevel(level: Level): LogLevel? {
        return when (level) {
            Level.ALL -> LogLevel.DEBUG
            Level.TRACE -> LogLevel.DEBUG
            Level.DEBUG -> LogLevel.DEBUG
            Level.INFO -> LogLevel.INFO
            Level.LIFECYCLE -> LogLevel.LIFECYCLE
            Level.WARNING -> LogLevel.WARN
            Level.ERROR -> LogLevel.ERROR
            Level.OFF -> null
        }
    }

    override fun isLevel(level: Level): Boolean {
        val lv = mapLevel(level) ?: return false
        return logger.isEnabled(lv)
    }

    override fun log(level: Level, message: String) {
        val lv = mapLevel(level) ?: return
        logger.log(lv, "${prefix.getPrefix(level)}$message")
    }

    override fun log(level: Level, message: String, throwable: Throwable) {
        val lv = mapLevel(level) ?: return
        logger.log(lv, "${prefix.getPrefix(level)}$message", throwable)
    }

    inline fun trace(message: () -> String) {
        if (!isLevel(Level.TRACE)) return
        log(Level.TRACE, message())
    }

    inline fun debug(message: () -> String) {
        if (!isLevel(Level.DEBUG)) return
        log(Level.DEBUG, message())
    }

    inline fun info(message: () -> String) {
        if (!isLevel(Level.INFO)) return
        log(Level.INFO, message())
    }

    inline fun lifecycle(message: () -> String) {
        if (!isLevel(Level.LIFECYCLE)) return
        log(Level.LIFECYCLE, message())
    }

    inline fun warning(message: () -> String) {
        if (!isLevel(Level.WARNING)) return
        log(Level.WARNING, message())
    }

    inline fun error(message: () -> String) {
        if (!isLevel(Level.ERROR)) return
        log(Level.ERROR, message())
    }

}
