package xyz.wagyourtail.commonskt.properties

import kotlin.js.Date

actual fun currentTimeMillis(): Long {
    return Date.now().toLong()
}