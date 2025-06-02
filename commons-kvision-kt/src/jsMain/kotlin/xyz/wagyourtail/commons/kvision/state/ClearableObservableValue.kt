package io.github.anifin.components.improved

import io.kvision.state.ObservableState
import io.kvision.state.ObservableValue

open class ClearableObservableValue<T>(initial: T) : ObservableValue<T>(initial) {

    open fun clearSubscribers() {
        observers.clear()
    }

}

inline fun <T, U> Pair<ObservableState<T>, ObservableState<U>>.subscribe(crossinline onUpdate: (T, U) -> Unit) {
    this.first.subscribe { onUpdate(it, this.second.getState()) }
    this.second.subscribe { onUpdate(this.first.getState(), it) }
}