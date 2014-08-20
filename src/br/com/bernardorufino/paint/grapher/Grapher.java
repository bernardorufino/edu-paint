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
        int color = mFb.getPixel(p.x, p.y) ^ 0x00FFFFFF;
        mFb.setPixel(p.x, p.y, color);
        return this;
    }

    public Grapher drawPixel(Point p) {
        if (showPixelAndIncrement()) {
            mFb.setPixel(p.x, p.y, getColor());
        }
        return this;
    }

    public Grapher safeDrawPixel(Point p) {
        if (!mFb.contains(p)) return this;
        return drawPixel(p);
    }

    private void drawDdaLine(Point p, Point q, Consumer<? super Point> pixelWriter) {
        int length = Math.max(Math.abs(q.x - p.x), Math.abs(q.y - p.y));
        if (length == 0) {
            pixelWriter.accept(p);
            return;
        }
        double dx = (double) (q.x - p.x) / length;
        double dy = (double) (q.y - p.y) / length;
        double x = p.x;
        double y = p.y;
        for (int i = 0; i <= length; i++) {
            Point r = Point.at(Math.round(x), Math.round(y));
            pixelWriter.accept(r);
            x += dx;
            y += dy;
        }
    }

    public Grapher drawXorLine(Point p, Point q) {
        drawDdaLine(p, q, this::drawXorPixel);
        return this;
    }

    public Grapher drawDdaLine(Point p, Point q) {
        setPatternDraw(true);
        drawDdaLine(p, q, this::drawPixel);
        return this;
    }

    public Grapher drawBresenhamLine(Point p, Point q) {
        setPatternDraw(true);
        drawBresenhamLine(p, q, this::drawPixel);
        return this;
    }

    private void drawBresenhamLine(Point p, Point q, Consumer<? super Point> pixelWriter) {
        Converter<Point, Point> direct = getConverter(p, q);
        Converter<Point, Point> inverse = direct.reverse();
        p = direct.convert(p);
        q = direct.convert(q);
        if (q.x < p.x) {
            Point tmp = p;
            p = q;
            q = tmp;
        }
        Point r = Point.copy(p);
        int dx = q.x - p.x;
        int dy = q.y - p.y;
        int g = 2 * dy - dx;
        for (int i = 0; i <= dx; i++) {
            pixelWriter.accept(inverse.convert(r));
            if (g > 0) {
                r.y += 1;
                g = g + 2 * dy - 2 * dx;
            } else {
                g = g + 2 * dy;
            }
            r.x += 1;
        }

    }

    @SuppressWarnings("Convert2MethodRef")
    private Converter<Point, Point> getConverter(Point p, Point q) {
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
        if (p.x >= 0 && p.y >= 0) return (p.x > p.y) ? 0 : 1;
        if (p.x <  0 && p.y >= 0) return (-p.x > p.y) ? 3 : 2;
        if (p.x <  0 && p.y  < 0) return (-p.x > -p.y) ? 4 : 5;
        if (p.x >= 0 && p.y  < 0) return (p.x > -p.y) ? 7 : 6;
        throw new AssertionError("Position p didn't fit any octant.");
    }

    public Grapher drawCircle(Point c, double radius) {
        setPatternDraw(true);
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

    private void drawEighthCircle(Point c, double radius, Consumer<Point> pixelWriter) {
        int r = (int) radius;
        Point p = Point.at(c.x, c.y - r);
        int d = 1 - r;
        pixelWriter.accept(p);
        while (octant(p.minus(c)) == 6) {
            if (d < 0) {
                d = d + 2 * (p.x - c.x) + 3;
            } else {
                d = d + 2 * (p.x - c.x + p.y - c.y) + 5;
                p.y += 1;
            }
            p.x += 1;
            pixelWriter.accept(p);
        }
    }

    //private void drawCirclePixels(Point p, Point c, int color) {
    //    safeDrawPixel(p.minus(c).plus(c), color);
    //    safeDrawPixel(p.minus(c).transpose().plus(c), color);
    //    safeDrawPixel(p.minus(c).flipX().transpose().plus(c), color);
    //    safeDrawPixel(p.minus(c).flipX().plus(c), color);
    //    safeDrawPixel(p.minus(c).opposite().plus(c), color);
    //    safeDrawPixel(p.minus(c).opposite().transpose().plus(c), color);
    //    safeDrawPixel(p.minus(c).flipY().transpose().plus(c), color);
    //    safeDrawPixel(p.minus(c).flipY().plus(c), color);
    //}


    public FrameBuffer getFrameBuffer() {
        return mFb;
    }
}
