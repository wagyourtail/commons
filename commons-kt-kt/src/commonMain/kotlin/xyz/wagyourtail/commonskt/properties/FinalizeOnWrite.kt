package xyz.wagyourtail.commonskt.properties

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class FinalizeOnWrite<T>(value: T) :
    ReadWriteProperty<Any?, T> {

    var finalized = false

    var value: Any? = value

    private val lock = SynchronizedObject()

    constructor(prop: ReadWriteProperty<Any?, T>) : this(prop as T)

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (value is ReadWriteProperty<*, *>) {
            return (value as ReadWriteProperty<Any?, T>).getValue(thisRef, property)
        }
        return value as T
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        synchronized(lock) {
            if (!finalized) {
                finalized = true
            } else {
                throw IllegalStateException("Cannot set finalized property")
            }
        }
        if (value is ReadWriteProperty<*, *>) {
            (value as ReadWriteProperty<Any?, T>).setValue(thisRef, property, value)
        } else {
            this.value = value
        }
    }

}