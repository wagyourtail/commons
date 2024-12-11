package xyz.wagyourtail.commons.position;

import java.util.Objects;

public class Pos3D extends Pos2D {
    public static final Pos3D ZERO = new Pos3D(0, 0, 0);

    public final double z;

    public Pos3D(double x, double y, double z) {
        super(x, y);
        this.z = z;
    }

    public static Pos3D max(Pos3D p1, Pos3D p2) {
        return new Pos3D(Math.max(p1.x, p2.x), Math.max(p1.y, p2.y), Math.max(p1.z, p2.z));
    }

    public static Pos3D min(Pos3D p1, Pos3D p2) {
        return new Pos3D(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y), Math.min(p1.z, p2.z));
    }

    public static Pos3D abs(Pos3D p) {
        return new Pos3D(Math.abs(p.x), Math.abs(p.y), Math.abs(p.z));
    }

    @Override
    public Pos3D inverse() {
        return new Pos3D(-this.x, -this.y, -this.z);
    }

    public Pos3D plus(Pos3D other) {
        return new Pos3D(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Pos3D plus(Pos3 other) {
        return new Pos3D(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Pos3D plus(double x, double y, double z) {
        return new Pos3D(this.x + x, this.y + y, this.z + z);
    }

    @Override
    public Pos3D plus(double scalar) {
        return new Pos3D(this.x + scalar, this.y + scalar, this.z + scalar);
    }

    public Pos3D minus(Pos3D other) {
        return new Pos3D(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Pos3D minus(Pos3 other) {
        return new Pos3D(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Pos3D minus(double x, double y, double z) {
        return new Pos3D(this.x - x, this.y - y, this.z - z);
    }

    @Override
    public Pos3D minus(double scalar) {
        return new Pos3D(this.x - scalar, this.y - scalar, this.z - scalar);
    }

    public Pos3D times(Pos3D other) {
        return new Pos3D(this.x * other.x, this.y * other.y, this.z * other.z);
    }

    public Pos3D times(Pos3 other) {
        return new Pos3D(this.x * other.x, this.y * other.y, this.z * other.z);
    }

    public Pos3D times(double x, double y, double z) {
        return new Pos3D(this.x * x, this.y * y, this.z * z);
    }

    @Override
    public Pos3D times(double scalar) {
        return new Pos3D(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public Pos3D div(Pos3D other) {
        return new Pos3D(this.x / other.x, this.y / other.y, this.z / other.z);
    }

    public Pos3D div(Pos3 other) {
        return new Pos3D(this.x / other.x, this.y / other.y, this.z / other.z);
    }

    public Pos3D div(double x, double y, double z) {
        return new Pos3D(this.x / x, this.y / y, this.z / z);
    }

    @Override
    public Pos3D div(double scalar) {
        return new Pos3D(this.x / scalar, this.y / scalar, this.z / scalar);
    }

    @Override
    public Pos3 toInt() {
        return new Pos3((int) x, (int) y, (int) z);
    }

    @Override
    public Pos3 floor() {
        return new Pos3((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    @Override
    public Pos3 ceil() {
        return new Pos3((int) Math.ceil(x), (int) Math.ceil(y), (int) Math.ceil(z));
    }

    @Override
    public Pos3 round() {
        return new Pos3((int) Math.round(x), (int) Math.round(y), (int) Math.round(z));
    }

    @Override
    public double magnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    @Override
    public double magnitudeSquared() {
        return x * x + y * y + z * z;
    }

    public double dot(Pos3D other) {
        return super.dot(other) + z * other.z;
    }

    public Pos3D cross(Pos3D other) {
        return new Pos3D(this.y * other.z - this.z * other.y, this.z * other.x - this.x * other.z, this.x * other.y - this.y * other.x);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pos3D)) return false;
        if (!super.equals(o)) return false;
        Pos3D pos3D = (Pos3D) o;
        return Double.compare(z, pos3D.z) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), z);
    }

}
