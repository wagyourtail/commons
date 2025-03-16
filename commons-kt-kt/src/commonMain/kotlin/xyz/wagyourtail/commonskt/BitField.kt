package xyz.wagyourtail.commonskt

import kotlin.jvm.JvmInline
import kotlin.reflect.KProperty

interface BitField {

    var field: Int

    fun hasFlag(flag: Int): Boolean {
        return (field and flag) == flag
    }

    fun setFlag(flag: Int, value: Boolean) {
        field = if (value) {
            field or flag
        } else {
            field and flag.inv()
        }
    }

    @JvmInline
    value class Entry(val flag: Int) {
        operator fun getValue(thisRef: BitField, property: KProperty<*>): Boolean {
            return thisRef.hasFlag(flag)
        }

        operator fun setValue(thisRef: BitField, property: KProperty<*>, value: Boolean) {
            thisRef.setFlag(flag, value)
        }
    }

}