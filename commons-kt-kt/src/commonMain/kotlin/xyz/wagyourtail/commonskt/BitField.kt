package xyz.wagyourtail.commonskt

import kotlin.jvm.JvmInline
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class BitField {

    var field: Long = 0

    fun hasFlag(flag: Long): Boolean {
        return (field and flag) == flag
    }

    fun setFlag(flag: Long, value: Boolean) {
        field = if (value) {
            field or flag
        } else {
            field and flag.inv()
        }
    }

    fun entry(flag: Long, enable: Boolean = false): Entry {
        return Entry(flag).also { if (enable) setFlag(flag, true) }
    }

    @JvmInline
    value class Entry(val flag: Long) : ReadWriteProperty<BitField, Boolean> {
        override operator fun getValue(thisRef: BitField, property: KProperty<*>): Boolean {
            return thisRef.hasFlag(flag)
        }

        override operator fun setValue(thisRef: BitField, property: KProperty<*>, value: Boolean) {
            thisRef.setFlag(flag, value)
        }
    }

}