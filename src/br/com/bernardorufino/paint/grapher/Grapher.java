package br.com.bernardorufino.paint.grapher;

import br.com.bernardorufino.paint.ext.Point;
import br.com.bernardorufino.paint.utils.DrawUtils;
import com.google.common.base.Converter;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

import java.util.BitSet;
import java.util.function.Consumer;

public class Grapher {

    private Property<Color> mColorProperty = new SimpleObjectProperty<>();
    private Property<BitSet> mPatternProperty = new SimpleObjectProperty<>();
    private final FrameBuffer mFb;
    private boolean mPatternDraw;
    private int mPixelCount;

    public Grapher(FrameBuffer fb) {
        mFb = fb;
    }

    public Property<Color> colorProperty() {
        return mColorProperty;
    }

    private int getColor() {
        return DrawUtils.fromColorToInt(mColorProperty.getValue());
    }

    public Property<BitSet> patternProperty() {
        return mPatternProperty;
    }

    private boolean showPixelAndIncrement() {
        BitSet pattern = mPatternProperty.getValue();
        int bit = (mPixelCount++) % pattern.length();
        return !mPatternDraw || pattern.get(bit);
    }

    // In order to draw with patterns one should make sure pixels are painted in order on the screen
    private void setPatternDraw(boolean patternDraw) {
        mPatternDraw = patternDraw;
        if (mPatternDraw) {
            mPixelCount = 0;
        }
    }

    public Grapher drawXorPixel(Point p) {
        // Gets pixel from FrameBuffer and make xor operation with RGB bits
        int color = mFb.getPixel(p.x, p.y) ^ 0x00FFFFFF;
        mFb.setPixel(p.x, p.y, color);
        return this;
    }

    public Grapher drawPixel(Point p) {
        // Checks whether is supposed to show the pixel, because we could be on
        // a negative bit of a pattern, hence shouldn't draw the pixel. After
        // checking increment the pixel counter to use for the pattern.
        if (showPixelAndIncrement()) {
            mFb.setPixel(p.x, p.y, getColor());
        }
        return this;
    }

    // Draws only if the pixel is within the boundaries of the FrameBuffer
    public Grapher safeDrawPixel(Point p) {
        if (!mFb.contains(p)) return this;
        return drawPixel(p);
    }

    // Accepts the initial point p, final point q and a function which is supposed to
    // write the pixel on screen.
    private void drawDdaLine(Point p, Point q, Consumer<? super Point> pixelWriter) {
        // Sets the length as the greatest difference of coordinates, in order not to
        // skip pixels. Practically we invert x and y in case the angular coefficient of
        // the line is greater than 1
        int length = Math.max(Math.abs(q.x - p.x), Math.abs(q.y - p.y));
        if (length == 0) {
            pixelWriter.accept(p);
            return;
        }
        // Calculate increments base on length, they can be 1, -1, m, -m, 1/m or -1/m
        double dx = (double) (q.x - p.x) / length;
        double dy = (double) (q.y - p.y) / length;
        // Coordinates of the point
        double x = p.x;
        double y = p.y;
        for (int i = 0; i <= length; i++) {
            // Round operation makes sure the line point goes to the nearest pixel
            Point r = Point.at(Math.round(x), Math.round(y));
            pixelWriter.accept(r);
            // Increment the coordinates of the point
            x += dx;
            y += dy;
        }
    }

    public Grapher drawXorLine(Point p, Point q) {
        // Calls the DDA algorithm with the drawXorPixel function
        drawDdaLine(p, q, this::drawXorPixel);
        return this;
    }

    public Grapher drawDdaLine(Point p, Point q) {
        // Activates the drawing of patterns in order to keep track of which pixels to
        // write on the screen
        setPatternDraw(true);
        // Calls the DDA algorithm with drawPixel function
        drawDdaLine(p, q, this::drawPixel);
        return this;
    }

    public Grapher drawBresenhamLine(Point p, Point q) {
        // Activates the drawing of patterns
        setPatternDraw(true);
        // Calls the Bresenham algorithm with usual drawPixel function
        drawBresenhamLine(p, q, this::drawPixel);
        return this;
    }

    // Accepts the initial point, the final point and a function that is supposed to write the
    // pixel on the screen
    private void drawBresenhamLine(Point p, Point q, Consumer<? super Point> pixelWriter) {
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

    @SuppressWarnings("Convert2MethodRef")
    private Converter<Point, Point> getConverter(Point p, Point q) {
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

    private int octant(Point p) {
        // Returns the octant of the point
        if (p.x >= 0 && p.y >= 0) return (p.x > p.y) ? 0 : 1;
        if (p.x <  0 && p.y >= 0) return (-p.x > p.y) ? 3 : 2;
        if (p.x <  0 && p.y  < 0) return (-p.x > -p.y) ? 4 : 5;
        if (p.x >= 0 && p.y  < 0) return (p.x > -p.y) ? 7 : 6;
        throw new AssertionError("Position p didn't fit any octant.");
    }

    public Grapher drawCircle(Point c, double radius) {
        // Activates the drawing of patterns
        setPatternDraw(true);
        // Draw the different eights of the circumference in order so as to display the pattern properly
        drawEighthCircle(c, radius, p -> safeDrawPixel(p.minus(c).plus(c)));
        drawEighthCircle(c, radius, p -> safeDrawPixel(p.minus(c).transpose().plus(c)));
        drawEighthCircle(c, radius, p -> safeDrawPixel(p.minus(c).flipX().transpose().plus(c)));
        drawEighthCircle(c, radius, p -> safeDrawPixel(p.minus(c).flipX().plus(c)));
        drawEighthCircle(c, radius, p -> safeDrawPixel(p.minus(c).opposite().plus(c)));
        drawEighthCircle(c, radius, p -> safeDrawPixel(p.minus(c).opposite().transpose().plus(c)));
        drawEighthCircle(c, radius, p -> safeDrawPixel(p.minus(c).flipY().transpose().plus(c)));
        drawEighthCircle(c, radius, p -> safeDrawPixel(p.minus(c).flipY().plus(c)));
        return this;
    }

    // Draws one eight of a circumference, given the central point, the radius and a function
    // responsible of writing the pixel on the screen
    private void drawEighthCircle(Point c, double radius, Consumer<Point> pixelWriter) {
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

    public FrameBuffer getFrameBuffer() {
        return mFb;
    }
}
