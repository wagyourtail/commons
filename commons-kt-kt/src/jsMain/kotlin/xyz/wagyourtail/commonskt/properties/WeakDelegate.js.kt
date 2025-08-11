package xyz.wagyourtail.commonskt.properties

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

external class WeakRef<T>(target: T) {

    fun deref(): T?

}

actual class WeakDelegate<T> actual constructor(private val refCreator: () -> T) : ReadOnlyProperty<Any?, T> {

    private var ref = WeakRef(refCreator())

    actual override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return ref.deref() ?: refCreator().also { ref = WeakRef(it) }
    }

}
