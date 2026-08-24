package xyz.wagyourtail.commonskt.properties

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.WeakReference
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@OptIn(ExperimentalNativeApi::class)
actual class WeakDelegate<T> actual constructor(private val refCreator: () -> T) : SynchronizedObject(), ReadOnlyProperty<Any?, T> {
	private var weak = WeakReference(refCreator() as Any)

	actual override fun getValue(thisRef: Any?, property: KProperty<*>): T {
		val ref = weak.get()
		if (ref != null) {
			return ref as T
		}
		synchronized(this) {
			val ref = weak.get()
			if (ref != null) {
				return ref as T
			}
			return refCreator().also {
				weak = WeakReference(it as Any)
			}
		}
	}
}
