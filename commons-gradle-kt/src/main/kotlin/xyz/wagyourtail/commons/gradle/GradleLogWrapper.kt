package xyz.wagyourtail.commons.gradle

import org.gradle.api.logging.LogLevel
import xyz.wagyourtail.commons.core.logger.Logger

class GradleLogWrapper(val prefix: MessageSupplier, val logger: org.gradle.api.logging.Logger) : Logger() {

    override fun subLogger(targetClass: Class<*>): Logger {
        return GradleLogWrapper(messageSupplierOf("$prefix/${targetClass.simpleName}"), logger)
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
        logger.log(lv, "[$prefix] $message")
    }

    override fun log(level: Level, message: String, throwable: Throwable) {
        val lv = mapLevel(level) ?: return
        logger.log(lv, "[$prefix] $message", throwable)
    }

}
