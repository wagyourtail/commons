package xyz.wagyourtail.commonskt.holders

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class ParentOrSet<T> private constructor() : ReadWriteProperty<Any, T> {
    private object EMPTY

    private var value: Any? = EMPTY
    private var parent: (() -> T)? = null

    fun get(): T {
        return if (value == EMPTY) {
            parent!!.invoke()
        } else {
            value as T
        }
    }

    fun set(value: T) {
        this.value = value
    }

    constructor(parentOrSet: ParentOrSet<T>) : this() {
        parent = parentOrSet::get
    }

    /**
     * @since 1.0.5
     */
    constructor(parent: () -> T) : this() {
        this.parent = parent
    }

    constructor(value: T) : this() {
        this.value = value
    }

    operator fun invoke() = get()

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return get()
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        set(value)
    }
}
