package xyz.wagyourtail.commonskt.utils

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round

val Long.kb: Long
    get() = this * 1024L

val Long.mb: Long
    get() = this.kb * 1024L

val Long.gb: Long
    get() = this.mb * 1024L

val Long.tb: Long
    get() = this.gb * 1024L


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