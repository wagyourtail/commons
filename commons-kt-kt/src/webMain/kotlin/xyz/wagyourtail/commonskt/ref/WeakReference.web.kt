@file:OptIn(ExperimentalWasmJsInterop::class)

package xyz.wagyourtail.commonskt.ref

private external class WeakRef<T : JsAny>(target: T) {
    fun deref(): T?
}

actual class WeakReference<T : Any> actual constructor(referred: T) {
    private var ref: WeakRef<JsReference<T>>? = WeakRef(referred.toJsReference())

    @Suppress("UNCHECKED_CAST")
    actual fun get(): T? {
        return ref?.deref()?.get()
    }

    actual fun clear() {
        ref = null
    }
}
