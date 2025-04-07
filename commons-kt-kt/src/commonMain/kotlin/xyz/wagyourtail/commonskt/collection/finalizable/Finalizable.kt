package xyz.wagyourtail.commonskt.collection.finalizable

interface Finalizable {

    val finalized: Boolean

    fun finalize()

}