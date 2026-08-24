@file:OptIn(ExperimentalWasmJsInterop::class)

package xyz.wagyourtail.commonskt.properties

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

// JavaScript "external" class generic upper bounds must not extend from Kotlin "Any"
// This limitation applies to this platform's WeakDelegate by extension
private external class WeakRef<T : JsAny>(target: T) {
    fun deref(): T?
}

@Suppress("UNCHECKED_CAST")
actual class WeakDelegate<T> actual constructor(
    private val refCreator: () -> T
) : ReadOnlyProperty<Any?, T> {
    private var ref = WeakRef(refCreator() as JsAny)

    actual override fun getValue(
        thisRef: Any?,
        property: KProperty<*>
    ): T {
        return (ref.deref() ?: refCreator().also { ref = WeakRef(it as JsAny) }) as T
    }
}
