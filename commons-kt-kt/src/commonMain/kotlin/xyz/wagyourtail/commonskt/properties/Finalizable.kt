package xyz.wagyourtail.commonskt.properties

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class Finalizable<T>(
    value: T,
    val finalized: () -> Boolean,
    val sync: SynchronizedObject = SynchronizedObject(),
) :
    ReadWriteProperty<Any?, T> {

    private var finalizedValue: Boolean = false
        get() {
            if (field) {
                return true
            } else {
                synchronized(sync) {
                    field = finalized()
                }
            }
            return field
        }

    var value: Any? = value

    constructor(prop: ReadWriteProperty<Any?, T>, finalized: () -> Boolean, sync: SynchronizedObject) : this(prop as T, finalized, sync)

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (value is ReadWriteProperty<*, *>) {
            return (value as ReadWriteProperty<Any?, T>).getValue(thisRef, property)
        }
        return value as T
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        synchronized(sync) {
            if (finalizedValue) {
                throw IllegalStateException("Cannot set finalized property")
            }

            if (this.value is ReadWriteProperty<*, *>) {
                (this.value as ReadWriteProperty<Any?, T>).setValue(thisRef, property, value)
            } else {
                this.value = value
            }
        }
    }

    fun setValueIntl(value: ReadWriteProperty<Any?, T>) {
        if (finalizedValue) {
            throw IllegalStateException("Cannot set finalized property")
        }
        this.value = value
    }

}