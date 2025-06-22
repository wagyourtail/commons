package xyz.wagyourtail.commonskt.position

import kotlin.jvm.JvmName
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

fun max(a: Pos2, b: Pos2): Pos2 {
    return Pos2(max(a.x, b.x), max(a.y, b.y))
}

fun min(a: Pos2, b: Pos2): Pos2 {
    return Pos2(min(a.x, b.x), min(a.y, b.y))
}

fun abs(pos: Pos2): Pos2 {
    return Pos2(abs(pos.x), abs(pos.y))
}

fun max(a: Pos2D, b: Pos2D): Pos2D {
    return Pos2D(max(a.x, b.x), max(a.y, b.y))
}

fun min(a: Pos2D, b: Pos2D): Pos2D {
    return Pos2D(min(a.x, b.x), min(a.y, b.y))
}

fun abs(pos: Pos2D): Pos2D {
    return Pos2D(abs(pos.x), abs(pos.y))
}

fun max(a: Pos3, b: Pos3): Pos3 {
    return Pos3(max(a.x, b.x), max(a.y, b.y), max(a.z, b.z))
}

fun min(a: Pos3, b: Pos3): Pos3 {
    return Pos3(max(a.x, b.x), max(a.y, b.y), max(a.z, b.z))
}

fun abs(pos: Pos3): Pos3 {
    return Pos3(abs(pos.x), abs(pos.y), abs(pos.z))
}

fun max(a: Pos3D, b: Pos3D): Pos3D {
    return Pos3D(max(a.x, b.x), max(a.y, b.y), max(a.z, b.z))
}

fun min(a: Pos3D, b: Pos3D): Pos3D {
    return Pos3D(min(a.x, b.x), min(a.y, b.y), min(a.z, b.z))
}

fun abs(pos: Pos3D): Pos3D {
    return Pos3D(abs(pos.x), abs(pos.y), abs(pos.z))
}

@JvmName("maxPos2")
fun Iterable<Pos2>.max(): Pos2 {
    return reduce { acc, pos2 -> max(acc, pos2) }
}

@JvmName("minPos2")
fun Iterable<Pos2>.min(): Pos2 {
    return reduce { acc, pos2 -> min(acc, pos2) }
}

@JvmName("maxPos2D")
fun Iterable<Pos2D>.max(): Pos2D {
    return reduce { acc, pos2D -> max(acc, pos2D) }
}

@JvmName("minPos2D")
fun Iterable<Pos2D>.min(): Pos2D {
    return reduce { acc, pos2D -> min(acc, pos2D) }
}

@JvmName("maxPos3")
fun Iterable<Pos3>.max(): Pos3 {
    return reduce { acc, pos3 -> max(acc, pos3) }
}

@JvmName("minPos3")
fun Iterable<Pos3>.min(): Pos3 {
    return reduce { acc, pos3 -> min(acc, pos3) }
}

@JvmName("maxPos3D")
fun Iterable<Pos3D>.max(): Pos3D {
    return reduce { acc, pos3D -> max(acc, pos3D) }
}

@JvmName("minPos3D")
fun Iterable<Pos3D>.min(): Pos3D {
    return reduce { acc, pos3D -> min(acc, pos3D) }
}

fun max(vararg a: Pos2): Pos2 {
    return a.reduce { a, b -> max(a, b) }
}

fun min(vararg a: Pos2): Pos2 {
    return a.reduce { a, b -> min(a, b) }
}

fun max(vararg a: Pos2D): Pos2D {
    return a.reduce { a, b -> max(a, b) }
}

fun min(vararg a: Pos2D): Pos2D {
    return a.reduce { a, b -> min(a, b) }
}

fun max(vararg a: Pos3): Pos3 {
    return a.reduce { a, b -> max(a, b) }
}

fun min(vararg a: Pos3): Pos3 {
    return a.reduce { a, b -> min(a, b) }
}

fun max(vararg a: Pos3D): Pos3D {
    return a.reduce { a, b -> max(a, b) }
}

fun min(vararg a: Pos3D): Pos3D {
    return a.reduce { a, b -> min(a, b) }
}
