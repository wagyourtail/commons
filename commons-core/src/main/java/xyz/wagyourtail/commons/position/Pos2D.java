package xyz.wagyourtail.commons.position;

import java.util.Objects;

public class Pos2D {
    public static final Pos2D ZERO = new Pos2D(0, 0);

    public final double x;
    public final double y;

    public Pos2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Pos2D max(Pos2D p1, Pos2D p2) {
        return new Pos2D(Math.max(p1.x, p2.x), Math.max(p1.y, p2.y));
    }

    public static Pos2D min(Pos2D p1, Pos2D p2) {
        return new Pos2D(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y));
    }

    public static Pos2D abs(Pos2D p) {
        return new Pos2D(Math.abs(p.x), Math.abs(p.y));
    }

    public Pos2D inverse() {
        return new Pos2D(-x, -y);
    }

    public Pos2D plus(Pos2D other) {
        return new Pos2D(x + other.x, y + other.y);
    }

    public Pos2D plus(Pos2 other) {
        return new Pos2D(x + other.x, y + other.y);
    }

    public Pos2D plus(double x, double y) {
        return new Pos2D(this.x + x, this.y + y);
    }

    public Pos2D plus(double scalar) {
        return new Pos2D(this.x + scalar, this.y + scalar);
    }

    public Pos2D minus(Pos2D other) {
        return new Pos2D(x - other.x, y - other.y);
    }

    public Pos2D minus(Pos2 other) {
        return new Pos2D(x - other.x, y - other.y);
    }

    public Pos2D minus(double x, double y) {
        return new Pos2D(this.x - x, this.y - y);
    }

    public Pos2D minus(double scalar) {
        return new Pos2D(this.x - scalar, this.y - scalar);
    }

    public Pos2D times(Pos2D other) {
        return new Pos2D(x * other.x, y * other.y);
    }

    public Pos2D times(Pos2 other) {
        return new Pos2D(x * other.x, y * other.y);
    }

    public Pos2D times(double x, double y) {
        return new Pos2D(this.x * x, this.y * y);
    }

    public Pos2D times(double scalar) {
        return new Pos2D(this.x * scalar, this.y * scalar);
    }

    public Pos2D div(Pos2D other) {
        return new Pos2D(x / other.x, y / other.y);
    }

    public Pos2D div(Pos2 other) {
        return new Pos2D(x / other.x, y / other.y);
    }

    public Pos2D div(double x, double y) {
        return new Pos2D(this.x / x, this.y / y);
    }

    public Pos2D div(double scalar) {
        return new Pos2D(x / scalar, y / scalar);
    }

    public Pos2D rem(Pos2D other) {
        return new Pos2D(x % other.x, y % other.y);
    }

    public Pos2D rem(double x, double y) {
        return new Pos2D(this.x % x, this.y % y);
    }

    public Pos2D rem(double scalar) {
        return new Pos2D(x % scalar, y % scalar);
    }

    public Pos2D mod(Pos2D other) {
        double x = this.x % other.x;
        double y = this.y % other.y;
        if (x < 0) x += other.x;
        if (y < 0) y += other.y;
        return new Pos2D(x, y);
    }

    public Pos2D mod(double x, double y) {
        double x2 = this.x % x;
        double y2 = this.y % y;
        if (x2 < 0) x2 += x;
        if (y2 < 0) y2 += y;
        return new Pos2D(x2, y2);
    }

    public Pos2D mod(double scalar) {
        double x = this.x % scalar;
        double y = this.y % scalar;
        if (x < 0) x += scalar;
        if (y < 0) y += scalar;
        return new Pos2D(x, y);
    }

    public Pos2 toInt() {
        return new Pos2((int) x, (int) y);
    }

    public Pos2 floor() {
        return new Pos2((int) Math.floor(x), (int) Math.floor(y));
    }

    public Pos2 ceil() {
        return new Pos2((int) Math.ceil(x), (int) Math.ceil(y));
    }

    public Pos2 round() {
        return new Pos2((int) Math.round(x), (int) Math.round(y));
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public double magnitudeSquared() {
        return x * x + y * y;
    }

    public Pos2D normalize() {
        double mag = magnitude();
        return new Pos2D(x / mag, y / mag);
    }

    public double dot(Pos2D other) {
        return x * other.x + y * other.y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pos2D)) return false;
        Pos2D pos2D = (Pos2D) o;
        return Double.compare(x, pos2D.x) == 0 && Double.compare(y, pos2D.y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

}
