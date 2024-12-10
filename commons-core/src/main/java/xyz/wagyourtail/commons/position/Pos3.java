package xyz.wagyourtail.commons.position;

import java.util.Objects;

public class Pos3 extends Pos2 {
    public static final Pos3 ZERO = new Pos3(0, 0, 0);

    public final int z;

    public Pos3(int x, int y, int z) {
        super(x, y);
        this.z = z;
    }

    @Override
    public Pos3 inverse() {
        return new Pos3(-this.x, -this.y, -this.z);
    }

    public Pos3 plus(Pos3 other) {
        return new Pos3(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Pos3 plus(int x, int y, int z) {
        return new Pos3(this.x + x, this.y + y, this.z + z);
    }

    @Override
    public Pos3 plus(int scalar) {
        return new Pos3(this.x + scalar, this.y + scalar, this.z + scalar);
    }

    public Pos3 minus(Pos3 other) {
        return new Pos3(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Pos3 minus(int x, int y, int z) {
        return new Pos3(this.x - x, this.y - y, this.z - z);
    }

    @Override
    public Pos3 minus(int scalar) {
        return new Pos3(this.x - scalar, this.y - scalar, this.z - scalar);
    }

    public Pos3 times(Pos3 other) {
        return new Pos3(this.x * other.x, this.y * other.y, this.z * other.z);
    }

    public Pos3 times(int x, int y, int z) {
        return new Pos3(this.x * x, this.y * y, this.z * z);
    }

    @Override
    public Pos3 times(int scalar) {
        return new Pos3(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public Pos3 div(Pos3 other) {
        return new Pos3(this.x / other.x, this.y / other.y, this.z / other.z);
    }

    public Pos3 div(int x, int y, int z) {
        return new Pos3(this.x / x, this.y / y, this.z / z);
    }

    @Override
    public Pos3 div(int scalar) {
        return new Pos3(this.x / scalar, this.y / scalar, this.z / scalar);
    }

    @Override
    public Pos3D toDouble() {
        return new Pos3D(x, y, z);
    }

    @Override
    public double magnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    @Override
    public int magnitudeSquared() {
        return x * x + y * y + z * z;
    }

    public double dot(Pos3 other) {
        return super.dot(other) + z * other.z;
    }

    public Pos3 cross(Pos3 other) {
        return new Pos3(this.y * other.z - this.z * other.y, this.z * other.x - this.x * other.z, this.x * other.y - this.y * other.x);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pos3)) return false;
        if (!super.equals(o)) return false;
        Pos3 pos3 = (Pos3) o;
        return z == pos3.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), z);
    }

}
