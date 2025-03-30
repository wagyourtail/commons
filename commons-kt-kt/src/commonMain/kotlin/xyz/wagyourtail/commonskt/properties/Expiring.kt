package xyz.wagyourtail.commonskt.properties

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

expect fun currentTimeMillis(): Long

class Expiring<T>(private val expireAfter: Duration = 1.days, private val refCreator: () -> T) :
    SynchronizedObject(),
    ReadOnlyProperty<Any?, T> {

    private var lastUpdate = 0L
    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (value == null || lastUpdate + expireAfter.inWholeMilliseconds < currentTimeMillis()) {
            synchronized(this) {
                if (value == null || lastUpdate + expireAfter.inWholeMilliseconds < currentTimeMillis()) {
                    value = refCreator()
                    lastUpdate = currentTimeMillis()
                }
            }
        }
        return value!!
    }

}