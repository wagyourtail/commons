package xyz.wagyourtail.commons.kvision.variables

import io.github.anifin.components.improved.ClearableObservableValue

class RetainedObservableValue<T> @PublishedApi internal constructor(initial: T, val writer: (T) -> Unit) : ClearableObservableValue<T>(initial) {

    init {
        subscribe(writer)
    }

    companion object {

        inline fun <reified T> fromLocalStorage(key: String, initialValue: () -> T) =
            RetainedObservableValue(readLocalStorageValue(key) ?: initialValue()) { writeLocalStorageValue(key, it) }

        inline fun <reified T> fromParams(key: String, initialValue: () -> T) =
            RetainedObservableValue(readParamValue(key) ?: initialValue()) { writeParamValue(key, it) }

        fun <T> custom(reader: () -> T, writer: (T) -> Unit) =
            RetainedObservableValue(reader(), writer)

    }

    override fun clearSubscribers() {
        super.clearSubscribers()
        subscribe(writer)
    }

}