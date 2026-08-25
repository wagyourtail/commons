package xyz.wagyourtail.commonskt.properties

import kotlin.reflect.KProperty

actual class WeakDelegate<T> actual constructor(refCreator: () -> T) :
    kotlin.properties.ReadOnlyProperty<Any?, T> {
    actual override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        throw UnsupportedOperationException("WASM-WASI does not support weak references.")
    }
}
