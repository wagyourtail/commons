package xyz.wagyourtail.commonskt.ref

expect class WeakReference<T: Any>(referred: T) {
    fun get(): T?

    fun clear()
}