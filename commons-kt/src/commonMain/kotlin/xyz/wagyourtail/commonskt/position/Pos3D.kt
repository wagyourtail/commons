package xyz.wagyourtail.commonskt.position

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round
import kotlin.math.sqrt

class Pos3D(x: Double, y: Double, val z: Double): Pos2D(x, y) {
    companion object {
        val ZERO = Pos3D(0.0, 0.0, 0.0)
    }

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

    override fun equals(other: Any?): Boolean {
        return super.equals(other) && (other is Pos3D && z == other.z)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + z.hashCode()
        return result
    }

}