package xyz.wagyourtail.commonskt.properties

import java.lang.ref.WeakReference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

actual class WeakDelegate<T> actual constructor(private val refCreator: () -> T) : ReadOnlyProperty<Any?, T> {

    private var weak = WeakReference(refCreator())

    actual override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val ref = weak.get()
        if (ref != null) {
            return ref
        }
        synchronized(this) {
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