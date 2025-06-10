package xyz.wagyourtail.commonskt.data

abstract class LazyData<T, E: LazyData.Content<*>>(unparsed: T) {

    val content by lazy {
        parseContent(unparsed)
    }

    abstract fun parseContent(unparsed: T): E


    interface Content<E> {

        val entries: MutableSet<E>

    }

}
