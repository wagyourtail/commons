package xyz.wagyourtail.commonskt.utils

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round

val Long.k
    get() = this * 1000L

val Long.m
    get() = this.k * 1000L

val Long.g
    get() = this.m * 1000L

val Long.t
    get() = this.g * 1000L

val Long.kb: Long
    get() = this * 1024L

val Long.mb: Long
    get() = this.kb * 1024L

val Long.gb: Long
    get() = this.mb * 1024L

val Long.tb: Long
    get() = this.gb * 1024L

val Int.k
    get() = this * 1000

val Int.m
    get() = this.k * 1000

val Int.kb: Int
    get() = this * 1024

val Int.mb: Int
    get() = this.kb * 1024

fun Float.roundToMultipleOf(value: Float) = round(this / value) * value

fun Double.roundToMultipleOf(value: Double) = round(this / value) * value

fun Float.ceilToMultipleOf(value: Float) = ceil(this / value) * value

fun Double.ceilToMultipleOf(value: Double) = ceil(this / value) * value

fun Float.floorToMultipleOf(value: Float) = floor(this / value) * value

fun Double.floorToMultipleOf(value: Double) = floor(this / value) * value

fun Int.roundToMultipleOf(value: Int): Int {
    return if (this % value < value / 2) {
        this - this % value
    } else {
        this + value - this % value
    }
}

fun Long.roundToMultipleOf(value: Long): Long {
    return if (this % value < value / 2) {
        this - this % value
    } else {
        this + value - this % value
    }
}

fun Int.ceilToMultipleOf(value: Int): Int {
    return if (this % value == 0) {
        this
    } else {
        this + value - this % value
    }
}

fun Long.ceilToMultipleOf(value: Long): Long {
    return if (this % value == 0L) {
        this
    } else {
        this + value - this % value
    }
}

fun Int.floorToMultipleOf(value: Int): Int {
    return this - this % value
}

fun Long.floorToMultipleOf(value: Long): Long {
    return this - this % value
}

operator fun Number.unaryMinus(): Number {
    return when (this) {
        is Int -> -this
        is Long -> -this
        is Float -> -this
        is Double -> -this
        is Byte -> -this
        is Short -> -this
        else -> throw IllegalArgumentException()
    }
}

