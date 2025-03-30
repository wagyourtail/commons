package xyz.wagyourtail.commonskt.properties

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class CombiningDelegate<T>(private var value: T, val combiner: (T, T) -> T): SynchronizedObject(), ReadWriteProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        synchronized(this) {
            this.value = combiner(this.value, value)
        }
    }

}

fun <T> combining(value: T, combiner: (T, T) -> T): ReadWriteProperty<Any?, T> {
    return CombiningDelegate(value, combiner)
}
