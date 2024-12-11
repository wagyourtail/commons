package xyz.wagyourtail.commonskt.position

import kotlin.math.*

class Pos3(x: Int, y: Int, val z: Int) : Pos2(x, y) {
    companion object {
        val ZERO = Pos3(0, 0, 0)
    }

    override operator fun unaryMinus(): Pos3 {
        return Pos3(-this.x, -this.y, -this.z)
    }

    operator fun plus(other: Pos3): Pos3 {
        return Pos3(this.x + other.x, this.y + other.y, this.z + other.z)
    }

    fun plus(x: Int, y: Int, z: Int): Pos3 {
        return Pos3(this.x + x, this.y + y, this.z + z)
    }

    override operator fun plus(scalar: Int): Pos3 {
        return Pos3(this.x + scalar, this.y + scalar, this.z + scalar)
    }

    fun minus(other: Pos3): Pos3 {
        return Pos3(this.x - other.x, this.y - other.y, this.z - other.z)
    }

    fun minus(x: Int, y: Int, z: Int): Pos3 {
        return Pos3(this.x - x, this.y - y, this.z - z)
    }

    override operator fun minus(scalar: Int): Pos3 {
        return Pos3(this.x - scalar, this.y - scalar, this.z - scalar)
    }

    operator fun times(other: Pos3): Pos3 {
        return Pos3(this.x * other.x, this.y * other.y, this.z * other.z)
    }

    fun times(x: Int, y: Int, z: Int): Pos3 {
        return Pos3(this.x * x, this.y * y, this.z * z)
    }

    override operator fun times(scalar: Int): Pos3 {
        return Pos3(this.x * scalar, this.y * scalar, this.z * scalar)
    }

    operator fun div(other: Pos3): Pos3 {
        return Pos3(this.x / other.x, this.y / other.y, this.z / other.z)
    }

    fun div(x: Int, y: Int, z: Int): Pos3 {
        return Pos3(this.x / x, this.y / y, this.z / z)
    }

    override operator fun div(scalar: Int): Pos3 {
        return Pos3(this.x / scalar, this.y / scalar, this.z / scalar)
    }

    override fun toDouble(): Pos3D {
        return Pos3D(x.toDouble(), y.toDouble(), z.toDouble())
    }

    override val magnitude: Double
        get() {
            return sqrt((x * x + y * y + z * z).toDouble())
        }

    override val magnitudeSquared: Int
        get() {
            return x * x + y * y + z * z
        }

    fun dot(other: Pos3): Double {
        return (super.dot(other) + z * other.z).toDouble()
    }

    fun cross(other: Pos3): Pos3 {
        return Pos3(
            this.y * other.z - this.z * other.y,
            this.z * other.x - this.x * other.z,
            this.x * other.y - this.y * other.x
        )
    }

    override fun toString(): String {
        return "($x, $y, $z)"
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other) && (other is Pos3 && z == other.z)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + z.hashCode()
        return result
    }
}
