package xyz.wagyourtail.commons.position;

import java.util.Objects;

public class Pos2 {
    public static final Pos2 ZERO = new Pos2(0, 0);

    public final int x;
    public final int y;

    public Pos2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static Pos2 max(Pos2 p1, Pos2 p2) {
        return new Pos2(Math.max(p1.x, p2.x), Math.max(p1.y, p2.y));
    }

    public static Pos2 min(Pos2 p1, Pos2 p2) {
        return new Pos2(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y));
    }

    public static Pos2 abs(Pos2 p) {
        return new Pos2(Math.abs(p.x), Math.abs(p.y));
    }

    public Pos2 up() {
        return new Pos2(x, y - 1);
    }

    public Pos2 down() {
        return new Pos2(x, y + 1);
    }

    public Pos2 left() {
        return new Pos2(x - 1, y);
    }

    public Pos2 right() {
        return new Pos2(x + 1, y);
    }

    public Pos2 inverse() {
        return new Pos2(-x, -y);
    }

    public Pos2 plus(Pos2 other) {
        return new Pos2(x + other.x, y + other.y);
    }

    public Pos2 plus(int x, int y) {
        return new Pos2(x + this.x, y + this.y);
    }

    public Pos2 plus(int scalar) {
        return new Pos2(x + scalar, y + scalar);
    }

    public Pos2 minus(Pos2 other) {
        return new Pos2(x - other.x, y - other.y);
    }

    public Pos2 minus(int x, int y) {
        return new Pos2(x - this.x, y - this.y);
    }

    public Pos2 minus(int scalar) {
        return new Pos2(x - scalar, y - scalar);
    }

    public Pos2 times(Pos2 other) {
        return new Pos2(x * other.x, y * other.y);
    }

    public Pos2 times(int x, int y) {
        return new Pos2(x * this.x, y * this.y);
    }

    public Pos2 times(int scalar) {
        return new Pos2(x * scalar, y * scalar);
    }

    public Pos2 div(Pos2 other) {
        return new Pos2(x / other.x, y / other.y);
    }

    public Pos2 div(int x, int y) {
        return new Pos2(x / this.x, y / this.y);
    }

    public Pos2 div(int scalar) {
        return new Pos2(x / scalar, y / scalar);
    }

    public Pos2D toDouble() {
        return new Pos2D(x, y);
    }

    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    public int magnitudeSquared() {
        return x * x + y * y;
    }

    public double dot(Pos2 other) {
        return x * other.x + y * other.y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pos2)) return false;
        Pos2 pos2 = (Pos2) o;
        return x == pos2.x && y == pos2.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

}
