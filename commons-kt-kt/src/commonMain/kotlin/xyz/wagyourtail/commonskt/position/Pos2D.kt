package xyz.wagyourtail.commonskt.position

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round
import kotlin.math.sqrt

open class Pos2D(val x: Double, val y: Double) {
    companion object {
        val ZERO = Pos2D(0.0, 0.0)
    }

    constructor(x: Number, y: Number) : this(x.toDouble(), y.toDouble())

    operator fun component1() = x
    operator fun component2() = y

    open operator fun unaryMinus(): Pos2D {
        return Pos2D(-x, -y)
    }

    operator fun plus(other: Pos2D): Pos2D {
        return Pos2D(x + other.x, y + other.y)
    }

    operator fun plus(other: Pos2): Pos2D {
        return Pos2D(x + other.x, y + other.y)
    }

    fun plus(x: Double, y: Double): Pos2D {
        return Pos2D(this.x + x, this.y + y)
    }

    open operator fun plus(scalar: Double): Pos2D {
        return Pos2D(this.x + scalar, this.y + scalar)
    }

    operator fun minus(other: Pos2D): Pos2D {
        return Pos2D(x - other.x, y - other.y)
    }

    operator fun minus(other: Pos2): Pos2D {
        return Pos2D(x - other.x, y - other.y)
    }

    fun minus(x: Double, y: Double): Pos2D {
        return Pos2D(this.x - x, this.y - y)
    }

    open operator fun minus(scalar: Double): Pos2D {
        return Pos2D(this.x - scalar, this.y - scalar)
    }

    operator fun times(other: Pos2D): Pos2D {
        return Pos2D(x * other.x, y * other.y)
    }

    operator fun times(other: Pos2): Pos2D {
        return Pos2D(x * other.x, y * other.y)
    }

    fun times(x: Double, y: Double): Pos2D {
        return Pos2D(this.x * x, this.y * y)
    }

    open operator fun times(scalar: Double): Pos2D {
        return Pos2D(this.x * scalar, this.y * scalar)
    }

    operator fun div(other: Pos2D): Pos2D {
        return Pos2D(x / other.x, y / other.y)
    }

    operator fun div(other: Pos2): Pos2D {
        return Pos2D(x / other.x, y / other.y)
    }

    fun div(x: Double, y: Double): Pos2D {
        return Pos2D(this.x / x, this.y / y)
    }

    open operator fun div(scalar: Double): Pos2D {
        return Pos2D(x / scalar, y / scalar)
    }

    operator fun rem(other: Pos2D): Pos2D {
        return Pos2D(x % other.x, y % other.y)
    }

    fun rem(x: Double, y: Double): Pos2D {
        return Pos2D(this.x % x, this.y % y)
    }

    open operator fun rem(scalar: Double): Pos2D {
        return Pos2D(x % scalar, y % scalar)
    }

    infix fun mod(other: Pos2D): Pos2D {
        var x = this.x % other.x
        var y = this.y % other.y
        if (x < 0) x += other.x
        if (y < 0) y += other.y
        return Pos2D(x, y)
    }

    fun mod(x: Double, y: Double): Pos2D {
        var x2 = this.x % x
        var y2 = this.y % y
        if (x2 < 0) x2 += x
        if (y2 < 0) y2 += y
        return Pos2D(x2, y2)
    }

    open infix fun mod(scalar: Double): Pos2D {
        var x = this.x % scalar
        var y = this.y % scalar
        if (x < 0) x += scalar
        if (y < 0) y += scalar
        return Pos2D(x, y)
    }

    open fun toInt(): Pos2 {
        return Pos2(x.toInt(), y.toInt())
    }

    open fun floor(): Pos2 {
        return Pos2(floor(x).toInt(), floor(y).toInt())
    }

    open fun ceil(): Pos2 {
        return Pos2(ceil(x).toInt(), ceil(y).toInt())
    }

    open fun round(): Pos2 {
        return Pos2(round(x).toInt(), round(y).toInt())
    }

    open val magnitude: Double
        get() {
            return sqrt(magnitudeSquared)
        }

    open val magnitudeSquared: Double
        get() {
            return x * x + y * y
        }

    val normalized: Pos2D
        get() {
            val mag = magnitude
            return Pos2D(x / mag, y / mag)
        }

    fun dot(other: Pos2D): Double {
        return x * other.x + y * other.y
    }

    override fun toString(): String {
        return "($x, $y)"
    }

    override fun equals(other: Any?): Boolean {
        return other is Pos2D && other.x == x && other.y == y
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }

}
