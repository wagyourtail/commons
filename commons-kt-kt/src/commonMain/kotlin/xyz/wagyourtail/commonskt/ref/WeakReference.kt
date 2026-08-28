package xyz.wagyourtail.commonskt.ref

expect class WeakReference<T: Any>(value: T) {
    fun get(): T?

    fun clear()
}