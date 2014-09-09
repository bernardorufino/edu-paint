package br.com.bernardorufino.paint;

import br.com.bernardorufino.paint.ext.Point;
import com.google.common.base.Converter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Exs {

    public static void main(String[] args) {
        //AtomicInteger counter = new AtomicInteger(0);
        //drawBresenhamLine(
        //        Point.at(0, 0),
        //        Point.at(5, 5),
        //        p -> counter.incrementAndGet());
        //System.out.println("counter = " + counter.get() + " times");
        for (int r = 0; r < 400; r++) {
            int counter = drawCircle(Point.at(-12120, -5), r);
            int estimate = (int) (8*((int) (r * Math.sqrt(2) / 2.0 + 1.5)));
            System.out.println(counter + " - " + (estimate - counter) + ", ");
        }

    }

    public static int drawCircle(Point c, double radius) {
        // Activates the drawing of patterns

        // Draw the different eights of the circumference in order so as to display the pattern properly
        AtomicInteger pxCount = new AtomicInteger(0);
        AtomicInteger counter = new AtomicInteger(0);
        drawEighthCircle(c, radius, p -> counter.incrementAndGet());
        int count = pxCount.get();
        pxCount.set(pxCount.get() + count);
        drawEighthCircle(c, radius, p -> counter.incrementAndGet());
        pxCount.set(pxCount.get() + count);
        drawEighthCircle(c, radius, p -> counter.incrementAndGet());
        pxCount.set(pxCount.get() + count);
        drawEighthCircle(c, radius, p -> counter.incrementAndGet());
        pxCount.set(pxCount.get() + count);
        drawEighthCircle(c, radius, p -> counter.incrementAndGet());
        pxCount.set(pxCount.get() + count);
        drawEighthCircle(c, radius, p -> counter.incrementAndGet());
        pxCount.set(pxCount.get() + count);
        drawEighthCircle(c, radius, p -> counter.incrementAndGet());
        pxCount.set(pxCount.get() + count);
        drawEighthCircle(c, radius, p -> counter.incrementAndGet());
        return counter.get();
    }

    private static void drawEighthCircle(Point c, double radius, Consumer<Point> pixelWriter) {
        int r = (int) radius;
        // Sets the first point of the circumference as the one at 90 degrees
        Point p = Point.at(c.x, c.y - r);
        // This variable is responsible of tracking the distance between the pixel to be drawn
        // and the actual corresponding point of the circumference.
        int d = 1 - r;
        pixelWriter.accept(p);
        while (octant(p.minus(c)) == 6) {
            // If it's below zero, it means the circle passes above the mid-point
            // hence we should keep y and update d
            if (d < 0) {
                d = d + 2 * (p.x - c.x) + 3;
            } else { // It's above zero, we decrease y and update d
                d = d + 2 * (p.x - c.x + p.y - c.y) + 5;
                p.y += 1;
            }
            // Increase x
            p.x += 1;
            // Draw pixel
            pixelWriter.accept(p);
        }
    }

    private static int count(Point a, Point b) {
        AtomicInteger counter = new AtomicInteger(0);
        drawBresenhamLine(
                Point.at(0, 0),
                Point.at(5, 5),
                p -> counter.incrementAndGet());
        return counter.get();
    }

    private static void drawBresenhamLine(Point p, Point q, Consumer<? super Point> pixelWriter) {
        // Setup converters (forward and backwards) because the algorithm works on the
        // first octant. So, we first convert to this octant, run the algorithm, when we
        // should write on the screen we make the conversion backwards to get back to the
        // original octant and write properly on the screen
        Converter<Point, Point> direct = getConverter(p, q);
        Converter<Point, Point> inverse = direct.reverse();
        p = direct.convert(p);
        q = direct.convert(q);
        // Always start with the one to the left
        if (q.x < p.x) {
            Point tmp = p;
            p = q;
            q = tmp;
        }
        Point r = Point.copy(p);
        // Setup increments
        int dx = q.x - p.x;
        int dy = q.y - p.y;
        // Setup g function which keeps track of the error
        int g = 2 * dy - dx;
        for (int i = 0; i <= dx; i++) {
            pixelWriter.accept(inverse.convert(r));
            // If it's above the mid-point
            if (g > 0) {
                r.y += 1;
                g = g + 2 * dy - 2 * dx;
            } else { // It's below the mid-point
                g = g + 2 * dy;
            }
            r.x += 1;
        }
    }

    private static Converter<Point, Point> getConverter(Point p, Point q) {
        // Convert from any octant to the 1st octant (index 0)
        switch (octant(q.minus(p))) {
            case 0: return Converter.from(r -> r                       , r -> r);
            case 1: return Converter.from(r -> r.transpose()           , r -> r.transpose());
            case 2: return Converter.from(r -> r.flipX().transpose()   , r -> r.transpose().flipX());
            case 3: return Converter.from(r -> r.flipX()               , r -> r.flipX());
            case 4: return Converter.from(r -> r.opposite()            , r -> r.opposite());
            case 5: return Converter.from(r -> r.opposite().transpose(), r -> r.transpose().opposite());
            case 6: return Converter.from(r -> r.flipY().transpose()   , r -> r.transpose().flipY());
            case 7: return Converter.from(r -> r.flipY()               , r -> r.flipY());
        }
        throw new AssertionError("Vector p -> q didn't fit any octant.");
    }

    private static int octant(Point p) {
        // Returns the octant of the point
        if (p.x >= 0 && p.y >= 0) return (p.x > p.y) ? 0 : 1;
        if (p.x <  0 && p.y >= 0) return (-p.x > p.y) ? 3 : 2;
        if (p.x <  0 && p.y  < 0) return (-p.x > -p.y) ? 4 : 5;
        if (p.x >= 0 && p.y  < 0) return (p.x > -p.y) ? 7 : 6;
        throw new AssertionError("Position p didn't fit any octant.");
    }

}
