package br.com.bernardorufino.paint.ext;

public class Points {


    public static double distance(Point a, Point b) {
        return a.distanceTo(b);
    }

    public static Point midpoint(Point a, Point b) {
        return a.plus(b).times(0.5);
    }

    // Prevents instantiation
    private Points() {
        throw new AssertionError("Cannot instantiate object from " + this.getClass());
    }
}
