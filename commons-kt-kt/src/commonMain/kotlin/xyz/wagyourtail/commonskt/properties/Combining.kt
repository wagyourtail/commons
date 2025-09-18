package xyz.wagyourtail.commonskt.properties

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Combining<T>(private var value: T, val combiner: (T, T) -> T) : ReadWriteProperty<Any?, T> {

    private val lock = SynchronizedObject()

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        synchronized(lock) {
            this.value = combiner(this.value, value)
        }
    }

}
