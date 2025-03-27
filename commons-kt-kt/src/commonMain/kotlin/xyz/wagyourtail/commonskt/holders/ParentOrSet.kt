package xyz.wagyourtail.commonskt.holders

@Suppress("UNCHECKED_CAST")
class ParentOrSet<T> private constructor() {
    private object EMPTY

    private var value: Any? = EMPTY
    private var parent: ParentOrSet<T>? = null

    fun get(): T {
        return if (value == EMPTY) {
            parent!!.get()
        } else {
            value as T
        }
    }

    fun set(value: T) {
        this.value = value
    }

    constructor(parentOrSet: ParentOrSet<T>): this() {
        parent = parentOrSet
    }

    constructor(value: T): this() {
        this.value = value
    }
}