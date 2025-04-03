package xyz.wagyourtail.commonskt.utils

inline fun <R, T : R> T.iif(condition: Boolean, block: (T) -> R): R {
    return if (condition) {
        block(this)
    } else {
        this
    }
}

inline fun <R, T: R> T.iif(condition: Boolean, block: (T) -> R, elseBlock: (T) -> R): R {
    return if (condition) {
        block(this)
    } else {
        elseBlock(this)
    }
}
