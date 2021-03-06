package br.com.bernardorufino.paint.ext;

public class Point implements Persistable {

    public static Point origin() {
        return Point.at(0, 0);
    }

    public static Point at(double x, double y) {
        return new Point((int) x, (int) y);
    }

    public static Point copy(Point p) {
        return new Point(p.x, p.y);
    }

    public int x;
    public int y;

    private Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    private Point(Pack in) {
        x = in.readInt();
        y = in.readInt();
    }

    @Override
    public void persist(Pack out) {
        out.writeInt(x);
        out.writeInt(y);
    }

    public Point transpose() {
        return Point.at(y, x);
    }

    public Point flipX() {
        return Point.at(-x, y);
    }

    public Point flipY() {
        return Point.at(x, -y);
    }

    public Point opposite() {
        return Point.at(-x, -y);
    }

    public Point minus(Point p) {
        return Point.at(x - p.x, y - p.y);
    }

    public Point plus(Point p) {
        return Point.at(x + p.x, y + p.y);
    }

    public Point plusX(int dx) {
        return Point.at(x + dx, y);
    }

    public Point plusY(int dy) {
        return Point.at(x, y + dy);
    }

    public double distanceTo(Point p) {
        return this.minus(p).norm();
    }

    public double norm() {
        return Math.sqrt(x * x + y * y);
    }

    public Point times(double f) {
        return Point.at(x * f, y * f);
    }
/*
    public Point divide(double f) {
        checkArgument(f != 0, "Cannot divide by 0");
        return times(1 / f);
    }*/

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Point)) return false;
        Point point = (Point) object;
        return point.x == x && point.y == y;
    }

    @Override
    public int hashCode() {
        return ((x & 0x0000FFFF) << 16) | (y & 0x0000FFFF);
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", x, y);
    }

    public static final Creator<Point> CREATOR = new Creator<Point>() {
        @Override
        public Point create(Pack in) {
            return new Point(in);
        }
    };
}

