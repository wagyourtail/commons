package xyz.wagyourtail.commonskt.properties

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

expect class WeakDelegate<T>(refCreator: () -> T) : ReadOnlyProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T

}