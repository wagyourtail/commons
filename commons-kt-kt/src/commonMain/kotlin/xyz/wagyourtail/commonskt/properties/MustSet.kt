package xyz.wagyourtail.commonskt.properties

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class MustSet<T> :
    SynchronizedObject(),
    ReadWriteProperty<Any?, T> {

    @Suppress("ClassName")
    private object UNINITIALIZED_VALUE

    private var prop: Any? = UNINITIALIZED_VALUE

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (prop == UNINITIALIZED_VALUE) {
            synchronized(this) {
                if (prop == UNINITIALIZED_VALUE) {
                    throw IllegalStateException("Property ${property.name} must be set before use")
                }
            }
        }
        return prop as T
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        prop = value
    }
}