@file:OptIn(ExperimentalWasmJsInterop::class)

package xyz.wagyourtail.commonskt.ref

private external class WeakRef<T : JsAny>(target: T) {
    fun deref(): T?
}

actual class WeakReference<T : Any> actual constructor(value: T) {
    private val ref = WeakRef(value as JsAny)

    @Suppress("UNCHECKED_CAST")
    actual fun get(): T? {
        return ref.deref() as T?
    }

    actual fun clear() {
        ref.deref()
    }
}
