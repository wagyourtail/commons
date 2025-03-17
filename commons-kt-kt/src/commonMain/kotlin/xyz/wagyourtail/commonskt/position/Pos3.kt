package xyz.wagyourtail.commonskt.position

import kotlin.math.abs
import kotlin.math.sqrt

class Pos3(x: Int, y: Int, val z: Int) : Pos2(x, y) {
    companion object {
        val ZERO = Pos3(0, 0, 0)
    }

    operator fun component3() = z

    override val up: Pos3
        get() = Pos3(this.x, this.y - 1, this.z)

    override val down: Pos3
        get() = Pos3(this.x, this.y + 1, this.z)

    override val left: Pos3
        get() = Pos3(this.x - 1, this.y, this.z)

    override val right: Pos3
        get() = Pos3(this.x + 1, this.y, this.z)

    val forward
        get() = Pos3(this.x, this.y, this.z + 1)

    val back
        get() = Pos3(this.x, this.y, this.z - 1)

    operator fun rangeTo(pos: Pos3): Sequence<Pos3> {
        return sequence {
            for (z in z..pos.z) {
                for (y in y..pos.y) {
                    for (x in x..pos.x) {
                        yield(Pos3(x, y, z))
                    }
                }
            }
        }
    }

    operator fun rangeUntil(pos: Pos3): Sequence<Pos3> {
        return sequence {
            for (z in z..<pos.z) {
                for (y in y..<pos.y) {
                    for (x in x + 1..<x) {
                        yield(Pos3(x, y, z))
                    }
                }
            }
        }
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

    operator fun rem(other: Pos3): Pos3 {
        return Pos3(x % other.x, y % other.y, z % other.z)
    }

    fun rem(x: Int, y: Int, z: Int): Pos3 {
        return Pos3(this.x % x, this.y % y, this.z % z)
    }

    override operator fun rem(scalar: Int): Pos3 {
        return Pos3(x % scalar, y % scalar, z % scalar)
    }

    infix fun mod(other: Pos3): Pos3 {
        var x = this.x % other.x
        var y = this.y % other.y
        var z = this.z % other.z
        if (x < 0) x += other.x
        if (y < 0) y += other.y
        if (z < 0) z += other.z
        return Pos3(x, y, z)
    }

    fun mod(x: Int, y: Int, z: Int): Pos3 {
        var x2 = this.x % x
        var y2 = this.y % y
        var z2 = this.z % z
        if (x2 < 0) x2 += x
        if (y2 < 0) y2 += y
        if (z2 < 0) z2 += z
        return Pos3(x2, y2, z2)
    }

    override infix fun mod(scalar: Int): Pos3 {
        var x = this.x % scalar
        var y = this.y % scalar
        var z = this.z % scalar
        if (x < 0) x += scalar
        if (y < 0) y += scalar
        if (z < 0) z += scalar
        return Pos3(x, y, z)
    }

    override fun toDouble(): Pos3D {
        return Pos3D(x.toDouble(), y.toDouble(), z.toDouble())
    }

    fun distanceTo(other: Pos3): Double {
        return sqrt(distanceToSquared(other).toDouble())
    }

    fun distanceToSquared(other: Pos3): Int {
        return (x - other.x) * (x - other.x) + (y - other.y) * (y - other.y) + (z - other.z) * (z - other.z)
    }

    fun manhattanDistanceTo(other: Pos3): Int {
        return abs(x - other.x) + abs(y - other.y) + abs(z - other.z)
    }

    override val magnitude: Double
        get() {
            return sqrt((x * x + y * y + z * z).toDouble())
        }

    override val magnitudeSquared: Int
        get() {
            return x * x + y * y + z * z
        }

    override val manhattanMagnitude: Int
        get() = abs(x) + abs(y) + abs(z)

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
