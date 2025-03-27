package xyz.wagyourtail.commonskt.properties

import java.lang.ref.SoftReference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class SoftDelegate<T>(private val refCreator: () -> T) : ReadOnlyProperty<Any?, T> {

    private var soft = SoftReference<T>(null)

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val ref = soft.get()
        if (ref != null) {
            return ref
        }
        synchronized(this) {
            val ref = soft.get()
            if (ref != null) {
                return ref
            }
            return refCreator().also {
                soft = SoftReference(it)
            }
        }
    }

}