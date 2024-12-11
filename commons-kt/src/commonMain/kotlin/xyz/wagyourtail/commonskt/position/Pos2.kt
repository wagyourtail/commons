package xyz.wagyourtail.commonskt.position

import kotlin.math.*

open class Pos2(val x: Int, val y: Int) {
    companion object {
        val ZERO = Pos2(0, 0)
    }

    open operator fun unaryMinus(): Pos2 {
        return Pos2(-x, -y)
    }

    operator fun plus(other: Pos2): Pos2 {
        return Pos2(x + other.x, y + other.y)
    }

    fun plus(x: Int, y: Int): Pos2 {
        return Pos2(x + this.x, y + this.y)
    }

    open operator fun plus(scalar: Int): Pos2 {
        return Pos2(x + scalar, y + scalar)
    }

    operator fun minus(other: Pos2): Pos2 {
        return Pos2(x - other.x, y - other.y)
    }

    fun minus(x: Int, y: Int): Pos2 {
        return Pos2(x - this.x, y - this.y)
    }

    open operator fun minus(scalar: Int): Pos2 {
        return Pos2(x - scalar, y - scalar)
    }

    operator fun times(other: Pos2): Pos2 {
        return Pos2(x * other.x, y * other.y)
    }

    fun times(x: Int, y: Int): Pos2 {
        return Pos2(x * this.x, y * this.y)
    }

    open operator fun times(scalar: Int): Pos2 {
        return Pos2(x * scalar, y * scalar)
    }

    operator fun div(other: Pos2): Pos2 {
        return Pos2(x / other.x, y / other.y)
    }

    fun div(x: Int, y: Int): Pos2 {
        return Pos2(x / this.x, y / this.y)
    }

    open operator fun div(scalar: Int): Pos2 {
        return Pos2(x / scalar, y / scalar)
    }

    open fun toDouble(): Pos2D {
        return Pos2D(x.toDouble(), y.toDouble())
    }

    open val magnitude: Double
        get() {
            return sqrt(magnitudeSquared.toDouble())
        }

    open val magnitudeSquared: Int
        get() {
            return x * x + y * y
        }

    fun dot(other: Pos2): Int {
        return x * other.x + y * other.y
    }

    override fun toString(): String {
        return "($x, $y)"
    }

    override fun equals(other: Any?): Boolean {
        return other is Pos2 && other.x == this.x && other.y == this.y
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        return result
    }
}
