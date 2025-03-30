package xyz.wagyourtail.commonskt.properties

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class CombiningDelegate<T>(private var value: T, val combiner: (T, T) -> T): ReadWriteProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = combiner(this.value, value)
    }

}