package xyz.wagyourtail.commonskt.properties

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

// https://stackoverflow.com/questions/47947841/kotlin-var-lazy-init :)
class LazyMutable<T>(initializer: () -> T) :
    SynchronizedObject(),
    ReadWriteProperty<Any?, T> {

    private var initializer: (() -> T)? = initializer

    @Suppress("ClassName")
    private object UNINITIALIZED_VALUE

    private var prop: Any? = UNINITIALIZED_VALUE

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (prop === UNINITIALIZED_VALUE) {
            synchronized(this) {
                if (prop === UNINITIALIZED_VALUE) {
                    prop = initializer!!()
                    initializer = null
                }
            }
        }
        return prop as T
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        prop = value
    }

}