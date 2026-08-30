package xyz.wagyourtail.commonskt.ref

import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
actual class WeakReference<T: Any>(private val ref: kotlin.native.ref.WeakReference<T>) {

    actual constructor(value: T) : this(kotlin.native.ref.WeakReference(value))

    actual fun get(): T? {
        return ref.get()
    }

    actual fun clear() {
        ref.clear()
    }

}