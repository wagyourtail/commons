package xyz.wagyourtail.commonskt.ref


actual class WeakReference<T : Any> actual constructor(value: T) {
    var value: T? = value

    companion object {
        var allow: Boolean = false
    }

    init {
        if (!allow)
            throw UnsupportedOperationException("Can't initialize WeakReference, unsupported platform")
    }

    actual fun get(): T? {
        return value
    }

    actual fun clear() {
        value = null
    }
}
