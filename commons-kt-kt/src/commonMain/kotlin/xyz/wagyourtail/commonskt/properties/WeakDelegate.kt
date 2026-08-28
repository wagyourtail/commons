package xyz.wagyourtail.commonskt.properties

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import xyz.wagyourtail.commonskt.ref.WeakReference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class WeakDelegate<T: Any>(val refCreator: () -> T) : ReadOnlyProperty<Any?, T> {

    private var weak = WeakReference(refCreator())
    private var lock = SynchronizedObject()

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val ref = weak.get()
        if (ref != null) {
            return ref
        }
        synchronized(lock) {
            val ref = weak.get()
            if (ref != null) {
                return ref
            }
            return refCreator().also {
                weak = WeakReference(it)
            }
        }
    }

}
