package br.com.bernardorufino.paint.grapher;

import br.com.bernardorufino.paint.ext.*;
import br.com.bernardorufino.paint.figures.*;
import br.com.bernardorufino.paint.utils.DrawUtils;
import com.google.common.base.Converter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Grapher {

    private Property<Color> mColorProperty = new SimpleObjectProperty<>();
    private Property<BitSet> mPatternProperty = new SimpleObjectProperty<>();
    private Property<BitMatrix> mPattern2dProperty = new SimpleObjectProperty<>();
    private final FrameBuffer mFb;
    private PatternType mPatternDraw = PatternType.NONE;
    private int mPixelCount;
    private Configuration mTemporaryConfiguration;

    public Grapher(FrameBuffer fb) {
        mFb = fb;
    }

    public Property<Color> colorProperty() {
        return mColorProperty;
    }

    public Property<BitSet> patternProperty() {
        return mPatternProperty;
    }

    public Property<BitMatrix> pattern2dProperty() {
        return mPattern2dProperty;
    }

    private boolean showPixelAndAdjustPattern(Point p) {
        switch (mPatternDraw) {
            case D1:
                BitSet pattern = getPattern();
                int bit = (mPixelCount++) % pattern.length();
                return pattern.get(bit);
            case D2:
                BitMatrix pattern2d = getPattern2d();
                return pattern2d.get(p.y % pattern2d.getRows(), p.x % pattern2d.getColumns());
            case NONE:
                return true;
        }
        throw new AssertionError();
    }

    private int getColor() {
        return (mTemporaryConfiguration != null) ? mTemporaryConfiguration.color : DrawUtils.fromColorToInt(mColorProperty.getValue());
    }

    private BitMatrix getPattern2d() {
        return (mTemporaryConfiguration != null) ? mTemporaryConfiguration.pattern2d : mPattern2dProperty.getValue();
    }

    private BitSet getPattern() {
        return (mTemporaryConfiguration != null) ? mTemporaryConfiguration.pattern : mPatternProperty.getValue();
    }

    public Grapher temporarilyWith(Configuration configuration) {
        mTemporaryConfiguration = configuration;
        return this;
    }

    public void execute(Consumer<Grapher> consumer) {
        consumer.accept(this);
        mTemporaryConfiguration = null;
    }

    public Configuration getConfiguration() {
        return new Configuration(getPattern2d(), getPattern(), getColor());
    }

    // In order to draw with patterns one should make sure pixels are painted in order on the screen
    private void setPatternDraw(PatternType patternType) {
        mPatternDraw = patternType;
        if (patternType == PatternType.D1) {
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
        if (showPixelAndAdjustPattern(p)) {
            mFb.setPixel(p.x, p.y, getColor());
        }
        return this;
    }

    public Grapher drawPixel(Point p, int color) {
        // Checks whether is supposed to show the pixel, because we could be on
        // a negative bit of a pattern, hence shouldn't draw the pixel. After
        // checking increment the pixel counter to use for the pattern.
        if (showPixelAndAdjustPattern(p)) {
            mFb.setPixel(p.x, p.y, color);
        }
        return this;
    }

    // Draws only if the pixel is within the boundaries of the FrameBuffer
    public Grapher safeDrawPixel(Point p) {
        if (!mFb.contains(p)) return this;
        return drawPixel(p);
    }

    private static final double EPS = 0.001;

    // Accepts the initial point p, final point q and a function which is supposed to
    // write the pixel on screen.
    private LineFigure drawDdaLine(Point p, Point q, BiConsumer<? super Point, Double> pixelWriter) {
        // Sets the length as the greatest difference of coordinates, in order not to
        // skip pixels. Practically we invert x and y in case the angular coefficient of
        // the line is greater than 1
        int length = Math.max(Math.abs(q.x - p.x), Math.abs(q.y - p.y));
        if (length == 0) {
            pixelWriter.accept(p, 1.0);
            return null;
        }
        // Calculate increments base on length, they can be 1, -1, m, -m, 1/m or -1/m
        double dx = (double) (q.x - p.x) / length;
        double dy = (double) (q.y - p.y) / length;
        // Coordinates of the point
        double x = p.x;
        double y = p.y;
        for (int i = 0; i <= length; i++) {
            // Round operation makes sure the line point goes to the nearest pixel
            if (x - ((int) x) < EPS) {
                int iy = (int) y;
                pixelWriter.accept(Point.at(x, iy), y - iy);
                pixelWriter.accept(Point.at(x, iy + 1), 1 - (y - iy));
            } else {
                int ix = (int) x;
                pixelWriter.accept(Point.at(ix, y), x - ix);
                pixelWriter.accept(Point.at(ix + 1, y), 1 - (x - ix));
            }
            // Increment the coordinates of the point
            x += dx;
            y += dy;
        }
        return new LineFigure(p, q, getConfiguration());
    }



    public Grapher drawXorLine(Point p, Point q) {
        // Calls the DDA algorithm with the drawXorPixel function
        drawDdaLine(p, q, (a, b) -> drawXorPixel(a));
        return this;
    }

    public LineFigure drawDdaLine(Point p, Point q) {
        // Activates the drawing of patterns in order to keep track of which pixels to
        // write on the screen
        setPatternDraw(PatternType.D1);
        // Calls the DDA algorithm with drawPixel function
        drawDdaLine(p, q, (px, amount) -> {
            int color = getPixelColor(px);
            drawPixel(px, DrawUtils.mergeColors(getColor(), color, 1 - amount));
        });
        return new LineFigure(p, q, getConfiguration());
    }

    public Grapher drawBresenhamLine(Point p, Point q, PatternType patternType) {
        // Activates the drawing of patterns
        setPatternDraw(patternType);
        // Calls the Bresenham algorithm with usual drawPixel function
        drawBresenhamLine(p, q, this::safeDrawPixel);
        return this;
    }


    public LineFigure drawBresenhamLine(Point p, Point q) {
        drawBresenhamLine(p, q, PatternType.D1);
        return new LineFigure(p, q, getConfiguration());
    }
/*
    public Grapher drawPolygon() {
        return this;
    }*/

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
        assert q != null;
        assert p != null;
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
        // Converts from any octant to the 1st octant (index 0)
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

    public CircleFigure drawCircle(Point c, double radius) {
        // Activates the drawing of patterns
        setPatternDraw(PatternType.NONE);
        // Draw the different eights of the circumference in order so as to display the pattern properly
        AtomicInteger pxCount = new AtomicInteger(0);
        drawEighthCircle(c, radius, p -> drawCircPixel(false, pxCount, p.minus(c).plus(c)));
        int count = pxCount.get();
        pxCount.set(pxCount.get() + count);
        drawEighthCircle(c, radius, p -> drawCircPixel(true, pxCount, p.minus(c).transpose().plus(c)));
        pxCount.set(pxCount.get() + count);
        drawEighthCircle(c, radius, p -> drawCircPixel(false, pxCount, p.minus(c).flipX().transpose().plus(c)));
        pxCount.set(pxCount.get() + count);
        drawEighthCircle(c, radius, p -> drawCircPixel(true, pxCount, p.minus(c).flipX().plus(c)));
        pxCount.set(pxCount.get() + count);
        drawEighthCircle(c, radius, p -> drawCircPixel(false, pxCount, p.minus(c).opposite().plus(c)));
        pxCount.set(pxCount.get() + count);
        drawEighthCircle(c, radius, p -> drawCircPixel(true, pxCount, p.minus(c).opposite().transpose().plus(c)));
        pxCount.set(pxCount.get() + count);
        drawEighthCircle(c, radius, p -> drawCircPixel(false, pxCount, p.minus(c).flipY().transpose().plus(c)));
        pxCount.set(pxCount.get() + count);
        drawEighthCircle(c, radius, p -> drawCircPixel(true, pxCount, p.minus(c).flipY().plus(c)));
        return new CircleFigure(c, radius, false, getConfiguration());
    }

    private void drawCircPixel(boolean reverse, AtomicInteger count, Point p) {
        BitSet pattern = getPattern();
        int pos = count.get() % pattern.length();
        if (pattern.get(pos)) {
            safeDrawPixel(p);
        }
        if (reverse) {
            count.decrementAndGet();
        } else {
            count.incrementAndGet();
        }
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

    public CircleFigure circleFill(Point c, double radius) {
        setPatternDraw(PatternType.D1);
        drawCircle(c, radius);
        setPatternDraw(PatternType.D2);
        floodFill(c);
        return new CircleFigure(c, radius, true, getConfiguration());
    }

    public void drawPolygon(Polygon polygon) {
        List<Point> points = polygon.getVertices();

        if (points.size() < 1) return;

        Point p1, p2;
        for (int i = 0; i < points.size() - 1; i++) {
            p1 = points.get(i);
            p2 = points.get(i+1);
            drawBresenhamLine(p1, p2);
        }

        Point lastPoint = points.get(points.size() - 1 );
        Point firstPoint = points.get(0);

        drawBresenhamLine(lastPoint, firstPoint);
    }

    public PolygonFigure scanFill(Polygon polygon) {
        setPatternDraw(PatternType.D2);
        List<Edge> sortedEdges = polygon.getSortedEdges();
        List<Integer> xCrossings = new ArrayList<>();
        for (int scanline = polygon.getYMin(); scanline <= polygon.getYMax(); scanline++) {
            xCrossings.clear();
            for (Edge sortedEdge : sortedEdges) {
                // lowest Y vertice intersection
                if (scanline == sortedEdge.p1.y) {
                    if (scanline == sortedEdge.p2.y) { // horizontal edge
                        sortedEdge.off();
                        xCrossings.add((int) sortedEdge.getX());
                    } else {
                        sortedEdge.on();
                    }
                }
                // highest Y vertice intersection
                if (scanline == sortedEdge.p2.y) {
                    sortedEdge.off();
                    xCrossings.add((int) sortedEdge.getX());
                }
                // intersection in the middle of the edge
                if (scanline > sortedEdge.p1.y && scanline < sortedEdge.p2.y) {
                    sortedEdge.update();
                    xCrossings.add((int) sortedEdge.getX());
                }
            }
            Collections.sort(xCrossings);
            for (int i = 0; i < xCrossings.size(); i += 2) {
                drawBresenhamLine(Point.at(xCrossings.get(i), scanline), Point.at(xCrossings.get(i + 1), scanline), PatternType.D2);
            }
        }
        return new PolygonFigure(polygon.getVertices(), PolygonFigure.FillAlgorithm.SCAN_LINE, getConfiguration());
    }

    public int getPixelColor(Point p) {
        return mFb.getPixel(p.x, p.y);
    }

    public FrameBuffer getFrameBuffer() {
        return mFb;
    }

    public void floodFill (Point start) {
        Set<Point> visiteds = new HashSet<>();
        Stack<Point> stack = new Stack<>();
        stack.add(start);
        visiteds.add(start);
        while (!stack.isEmpty()) {
            Point p = stack.pop();
            drawPixel(p);
            for (Point q : ImmutableList.of(p.plusX(1), p.plusX(-1), p.plusY(1), p.plusY(-1))) {
                if (!mFb.contains(q) || getColor() == getPixelColor(q) || visiteds.contains(q)) continue;
                stack.add(q);
                visiteds.add(q);
            }
        }
    }

    public PolygonFigure floodFillPolygon(List<Point> poly) {
        poly = new ArrayList<>(poly);
        setPatternDraw(PatternType.D2);
        drawPolygon(new Polygon(poly));
        List<Point> triangle = new ArrayList<>();
        while (poly.size() > 3) { //run untill the  polygon is triangulated
            boolean earFound = false;
            int i = poly.size() - 1; // starts with the last vertex
            while (!earFound) {
                Point mediumPoint = poly.get(((i - 1) % poly.size() + poly.size()) % poly.size()).plus(poly.get(((i + 1) % poly.size() + poly.size()) % poly.size())).times(0.5);
                if (isInsidePolygon(poly, mediumPoint)) { // check if vertex i makes an ear
                    triangle.add(poly.get(i));
                    triangle.add(poly.get(((i - 1) % poly.size() + poly.size()) % poly.size()));
                    triangle.add(poly.get(((i + 1) % poly.size() + poly.size()) % poly.size()));
                    drawBresenhamLine(triangle.get(1), triangle.get(2));
                    floodFillTriangulatedPolygon(triangle); // fill the triangle just created
                    triangle.clear();
                    poly.remove(i); // remove the ear
                    earFound = true;
                } else { // if that vertex doesn't make an ear, try the preceding one
                    i--;
                }
            }
        }
        floodFillTriangulatedPolygon(poly); // fill the last triangle
        return new PolygonFigure(poly, PolygonFigure.FillAlgorithm.FLOOD_FILL, getConfiguration());
    }

    //determines whether a given point p is inside or outside the given polygon
    /* TODO: Remove from here =( */
    private static boolean isInsidePolygon(List<Point> polygon, Point p) {
        List<Pair<Point, Point>> edges = polygon.stream().collect(() -> Lists.newArrayList(Pair.of(null, null)), (ArrayList<Pair<Point, Point>> es, Point q) -> {
            es.get(es.size() - 1).last = q;
            es.add(Pair.of(q, null)); /* TODO: Unchecked */
        }, (a, b) -> {});
        edges = edges.subList(1, edges.size());
        edges.get(edges.size() - 1).last = polygon.get(0);
        return edges.stream()
                .filter(e -> Math.max(e.first.x, e.last.x) > p.x)
                .filter(e -> Math.min(e.first.y, e.last.y) < p.y && p.y < Math.max(e.first.y, e.last.y))
                .count() % 2 == 1;
    }

    //checks whether a given point p is inside a given circle (given by its center and radius)
    private static boolean isInsideCircle(Point center, double radius, Point p) {
        return (p.distanceTo(center) <= radius);
    }

    //checks whether a given point p lies in the line segment between two given points a and b
    private static boolean isInsideLine(Point a, Point b, Point p) {
        //first check if p is inside the box defined by a and b
        if ((p.x > a.x && p.x < b.x) || (p.x > b.x && p.x < a.x)) {
            if ((p.y > a.y && p.y < b.y) || (p.y > b.y && p.y < a.y)) {
                float slope = (a.y - b.y) / (a.x - b.x); //calculating the slope of the segment
                float yIntercept = a.y - (slope * a.x); //calculating the yIntercept of the segment
                //checking if the point is part of the line
                return Math.abs(p.y - (slope * p.x + yIntercept)) < 0.01; //TODO check if 0.01 is a good threshold

            }
            return false;
        }
        return false;
    }

    //fills a triangle resulting from the ear clipping algorithm
    private void floodFillTriangulatedPolygon (List<Point> poly) {
        Point seed = Point.at(0, 0);
        for (Point vertex : poly) {
            seed.x += vertex.x;
            seed.y += vertex.y;
        }
        //calculates the center of the figure
        seed.x /= poly.size();
        seed.y /= poly.size();

        floodFill(seed); //calls floodFill for the calculated center
    }

    public List<? extends PersistableFigure> applyZoom(Point p, Point q, List<PersistableFigure> mFigures) {


        FrameBuffer fb = getFrameBuffer();

        int width = fb.getWidth();
        int height = fb.getHeight();

        double ar = width / height;

        double xc = (p.x + q.x) / 2.0;
        double yc = (p.y + q.y) / 2.0;

        double sx = (double) width/Math.abs(p.x - q.x);
        double sy = (double) height/Math.abs(p.y - q.y);

        double s = Math.min(sx, sy);


        Matrix M = new Matrix();
        M.identity();
        M.translate(-xc, -yc);
        M.scale(s);
        M.translate(width/2, height/2);

        M.doTransformation(p);
        M.doTransformation(q);


        Clipper clipper = new Clipper();
        clipper.setWindow(p, q);

        List<PersistableFigure> toBeRemoved = new ArrayList<>();
        for (PersistableFigure figure : mFigures) {
            if (!(figure instanceof PolygonFigure) && !(figure instanceof LineFigure) ) {
                toBeRemoved.add(figure);
            }
        }
        for (PersistableFigure figure : toBeRemoved) {
            /* TODO: Ugly =( */
            mFigures.remove(figure);
        }
        List<PersistableFigure> newFigures = new ArrayList<>();

        for (PersistableFigure figure : mFigures) {
            if(figure instanceof PolygonFigure) {
                PolygonFigure poly = (PolygonFigure) figure;
                Polygon newPoly = clipper.clipPolygon(poly);
                if (newPoly == null) continue;
                PolygonFigure newPolyFigure = PolygonFigure.applyConfiguration(newPoly.getVertices(), poly);
                newPoly = newPolyFigure.applyTransformation(M);
                newPolyFigure = PolygonFigure.applyConfiguration(newPoly.getVertices(), poly);
                newFigures.add(newPolyFigure);
            }
            else if(figure instanceof LineFigure) {
                LineFigure line = (LineFigure) figure;
                Edge edge = clipper.clipLine(new Edge(line.getStart(), line.getEnd()));
                if (edge == null) continue;
                LineFigure newLine = new LineFigure(Point.copy(edge.p1), Point.copy(edge.p2), line.getConfiguration());
                newLine = newLine.applyTransformation(M);
                newFigures.add(newLine);
            }
        }
/*
        newFigures = newFigures.stream().map(poly -> {
            Polygon newPoly = poly.applyTransformation(M);
            return PolygonFigure.applyConfiguration(newPoly.getVertices(), poly);
        }).collect(Collectors.toList());
*/
        return newFigures;
    }

    private static enum PatternType { D1, D2, NONE }
}
