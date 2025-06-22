package xyz.wagyourtail.commonskt.properties

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


class InternallyNullable<T>(
    initialValue: T? = null,
    val get: (internal: T?) -> T,
    val set: (internal: T?, newValue: T) -> T? = { _, newValue -> newValue }
) : ReadWriteProperty<Any?, T> {

    var value: T? = initialValue

    private val lock = SynchronizedObject()

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        synchronized(lock) {
            val value = get(value)
            this.value = value
            return value
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        synchronized(lock) {
            this.value = set(this.value, value)
        }
    }

}