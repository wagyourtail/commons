package xyz.wagyourtail.commons.kvision.variables

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class RetainedDelegate<T>(val reader: () -> T, val writer: (T) -> Unit) : ReadWriteProperty<Any?, T> {

    companion object {

        inline fun <reified T> fromLocalStorage(key: String, crossinline defaultValue: () -> T) =
            RetainedDelegate(
                { readLocalStorageValue(key) ?: defaultValue() },
                { writeLocalStorageValue(key, it) }
            )

        inline fun <reified T> fromParam(key: String, crossinline defaultValue: () -> T) =
            RetainedDelegate(
                { readParamValue(key) ?: defaultValue() },
                { writeParamValue(key, it) }
            )

    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return reader()
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        writer(value)
    }

}