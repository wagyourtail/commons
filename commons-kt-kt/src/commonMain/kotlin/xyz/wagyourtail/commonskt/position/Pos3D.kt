package xyz.wagyourtail.commonskt.position

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round
import kotlin.math.sqrt

class Pos3D(x: Double, y: Double, val z: Double) : Pos2D(x, y) {
    companion object {
        val ZERO = Pos3D(0.0, 0.0, 0.0)
    }

    constructor(x: Number, y: Number, z: Number) : this(x.toDouble(), y.toDouble(), z.toDouble())

    operator fun component3() = z

    override operator fun unaryMinus(): Pos3D {
        return Pos3D(-this.x, -this.y, -this.z)
    }

    operator fun plus(other: Pos3D): Pos3D {
        return Pos3D(this.x + other.x, this.y + other.y, this.z + other.z)
    }

    operator fun plus(other: Pos3): Pos3D {
        return Pos3D(this.x + other.x, this.y + other.y, this.z + other.z)
    }

    fun plus(x: Double, y: Double, z: Double): Pos3D {
        return Pos3D(this.x + x, this.y + y, this.z + z)
    }

    override operator fun plus(scalar: Double): Pos3D {
        return Pos3D(this.x + scalar, this.y + scalar, this.z + scalar)
    }

    operator fun minus(other: Pos3D): Pos3D {
        return Pos3D(this.x - other.x, this.y - other.y, this.z - other.z)
    }

    operator fun minus(other: Pos3): Pos3D {
        return Pos3D(this.x - other.x, this.y - other.y, this.z - other.z)
    }

    fun minus(x: Double, y: Double, z: Double): Pos3D {
        return Pos3D(this.x - x, this.y - y, this.z - z)
    }

    override operator fun minus(scalar: Double): Pos3D {
        return Pos3D(this.x - scalar, this.y - scalar, this.z - scalar)
    }

    operator fun times(other: Pos3D): Pos3D {
        return Pos3D(this.x * other.x, this.y * other.y, this.z * other.z)
    }

    operator fun times(other: Pos3): Pos3D {
        return Pos3D(this.x * other.x, this.y * other.y, this.z * other.z)
    }

    fun times(x: Double, y: Double, z: Double): Pos3D {
        return Pos3D(this.x * x, this.y * y, this.z * z)
    }

    override operator fun times(scalar: Double): Pos3D {
        return Pos3D(this.x * scalar, this.y * scalar, this.z * scalar)
    }

    operator fun div(other: Pos3D): Pos3D {
        return Pos3D(this.x / other.x, this.y / other.y, this.z / other.z)
    }

    operator fun div(other: Pos3): Pos3D {
        return Pos3D(this.x / other.x, this.y / other.y, this.z / other.z)
    }

    fun div(x: Double, y: Double, z: Double): Pos3D {
        return Pos3D(this.x / x, this.y / y, this.z / z)
    }

    override operator fun div(scalar: Double): Pos3D {
        return Pos3D(this.x / scalar, this.y / scalar, this.z / scalar)
    }

    operator fun rem(other: Pos3D): Pos3D {
        return Pos3D(x % other.x, y % other.y, z % other.z)
    }

    fun rem(x: Double, y: Double, z: Double): Pos3D {
        return Pos3D(this.x % x, this.y % y, this.z % z)
    }

    override operator fun rem(scalar: Double): Pos3D {
        return Pos3D(x % scalar, y % scalar, z % scalar)
    }

    infix fun mod(other: Pos3D): Pos3D {
        var x = this.x % other.x
        var y = this.y % other.y
        var z = this.z % other.z
        if (x < 0) x += other.x
        if (y < 0) y += other.y
        if (z < 0) z += other.z
        return Pos3D(x, y, z)
    }

    fun mod(x: Double, y: Double, z: Double): Pos3D {
        var x2 = this.x % x
        var y2 = this.y % y
        var z2 = this.z % z
        if (x2 < 0) x2 += x
        if (y2 < 0) y2 += y
        if (z2 < 0) z2 += z
        return Pos3D(x2, y2, z2)
    }

    override infix fun mod(scalar: Double): Pos3D {
        var x = this.x % scalar
        var y = this.y % scalar
        var z = this.z % scalar
        if (x < 0) x += scalar
        if (y < 0) y += scalar
        if (z < 0) z += scalar
        return Pos3D(x, y, z)
    }

    override fun toInt(): Pos3 {
        return Pos3(x.toInt(), y.toInt(), z.toInt())
    }

    override fun floor(): Pos3 {
        return Pos3(floor(x).toInt(), floor(y).toInt(), floor(z).toInt())
    }

    override fun ceil(): Pos3 {
        return Pos3(ceil(x).toInt(), ceil(y).toInt(), ceil(z).toInt())
    }

    override fun round(): Pos3 {
        return Pos3(round(x).toInt(), round(y).toInt(), round(z).toInt())
    }

    override val magnitude: Double
        get() {
            return sqrt(x * x + y * y + z * z)
        }

    override val magnitudeSquared: Double
        get() {
            return x * x + y * y + z * z
        }

    fun dot(other: Pos3D): Double {
        return super.dot(other) + z * other.z
    }

    fun cross(other: Pos3D): Pos3D {
        return Pos3D(
            this.y * other.z - this.z * other.y,
            this.z * other.x - this.x * other.z,
            this.x * other.y - this.y * other.x
        )
    }

    override fun toString(): String {
        return "($x, $y, $z)"
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other) && (other is Pos3D && z == other.z)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + z.hashCode()
        return result
    }

}