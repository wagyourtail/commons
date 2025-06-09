package xyz.wagyourtail.commonskt.data

abstract class LazyData<T, E: LazyData.Content<*>>(unparsed: T) {

    val data by lazy {
        parseData()
    }

    abstract fun parseData(): E


    interface Content<E> {

        val entries: MutableSet<E>

    }

}
