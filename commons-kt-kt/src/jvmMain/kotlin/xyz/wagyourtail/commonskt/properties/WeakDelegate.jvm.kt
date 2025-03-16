package xyz.wagyourtail.commonskt.properties

import java.lang.ref.WeakReference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

actual class WeakDelegate<T> actual constructor(private val refCreator: () -> T) : ReadOnlyProperty<Any?, T> {

    private var ref = WeakReference(refCreator())

    actual override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return ref.get() ?: refCreator().also {
            ref = WeakReference(it)
        }
    }

}