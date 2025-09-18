package xyz.wagyourtail.commonskt.collection.iterator

import xyz.wagyourtail.commonskt.utils.iterable
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.experimental.ExperimentalTypeInference

@OptIn(ExperimentalTypeInference::class)
fun <T> mutableIterator(@BuilderInference block: suspend MutableSequenceScope<T>.() -> Unit): MutableIterator<T> {
    val iterator = SequenceBuilderMutableIterator<T>()
    iterator.nextStep = block.createCoroutine(iterator, iterator) as Continuation<Boolean>?
    return iterator
}

abstract class MutableSequenceScope<T> internal constructor() {

    abstract suspend fun yield(value: T): Boolean

    abstract suspend fun yieldAll(values: Iterator<T>): Iterable<T>

    suspend fun yieldAll(values: Iterable<T>): Iterable<T> {
        if (values is Collection && values.isEmpty()) return iterable {}
        return yieldAll(values.iterator())
    }

    suspend fun yieldAll(values: Sequence<T>): Iterable<T> {
        return yieldAll(values.iterator())
    }

}

enum class State {
    NOT_READY,
    READY,
    DONE,
    FAILED
}

private class SequenceBuilderMutableIterator<T> : MutableSequenceScope<T>(), MutableIterator<T>, Continuation<Unit> {
    private var state = State.NOT_READY
    private var nextValue: T? = null
    private var remove: Boolean = false
    var nextStep: Continuation<Boolean>? = null

    override fun hasNext(): Boolean {
        while (true) {
            when (state) {
                State.NOT_READY -> {}
                State.DONE -> return false
                State.READY -> return true
                else -> throw exceptionalState()
            }

            state = State.FAILED
            val step = nextStep!!
            nextStep = null
            step.resume(remove)
            remove = false
        }
    }

    override fun next(): T {
        when (state) {
            State.NOT_READY -> if (!hasNext()) throw NoSuchElementException() else return next()
            State.READY -> {
                state = State.NOT_READY
                val result = nextValue
                nextValue = null
                return result!!
            }
            else -> throw exceptionalState()
        }
    }

    private fun exceptionalState(): Throwable = when (state) {
        State.DONE -> NoSuchElementException()
        State.FAILED -> IllegalStateException("Iterator has failed.")
        else -> IllegalStateException("Unexpected state of the iterator: $state")
    }

    override suspend fun yield(value: T): Boolean {
        nextValue = value
        state = State.READY
        return suspendCoroutine {
            nextStep = it
        }
    }

    override suspend fun yieldAll(values: Iterator<T>): Iterable<T> {
        val out = mutableListOf<T>()
        for (v in values) {
            if (yield(v)) {
                out.add(v)
            }
        }
        return out
    }

    override val context: CoroutineContext
        get() = EmptyCoroutineContext

    override fun resumeWith(result: Result<Unit>) {
        result.getOrThrow()
        state = State.DONE
    }

    override fun remove() {
        if (nextStep == null) throw NullPointerException()
        if (remove) throw IllegalStateException()
        remove = true
    }


}