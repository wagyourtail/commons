package xyz.wagyourtail.commonskt.properties

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Combining<T>(private var value: T, val combiner: (T, T) -> T): SynchronizedObject(), ReadWriteProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        synchronized(this) {
            this.value = combiner(this.value, value)
        }
    }

}
