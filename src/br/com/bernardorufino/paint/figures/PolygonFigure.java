package br.com.bernardorufino.paint.figures;

import br.com.bernardorufino.paint.ext.Pack;
import br.com.bernardorufino.paint.ext.Pair;
import br.com.bernardorufino.paint.ext.Point;
import br.com.bernardorufino.paint.grapher.Configuration;
import br.com.bernardorufino.paint.grapher.Grapher;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public class PolygonFigure extends Polygon implements PersistableFigure {

    private FillAlgorithm mAlgorithm;
    private Configuration mConfiguration;

    public PolygonFigure(Iterable<? extends Point> vertices, FillAlgorithm algorithm, Configuration configuration) {
        super(vertices);
        mAlgorithm = algorithm;
        mConfiguration = configuration;
    }

    public PolygonFigure(Pack in) {
        super(in);
        mAlgorithm = FillAlgorithm.valueOf(in.readString());
        mConfiguration = in.readPersistable();
    }

    @Override
    public void persist(Pack out) {
        super.persist(out);
        out.writeString(mAlgorithm.toString());
        out.writePersistable(mConfiguration);
    }

    @Override
    public void draw(Grapher grapher) {
        grapher.temporarilyWith(mConfiguration).execute(g -> {
            switch (mAlgorithm) {
                case FLOOD_FILL:
                    g.floodFillPolygon(getVertices());
                    break;
                case SCAN_LINE:
                    g.scanFill(this);
                    break;
                default:
                    throw new AssertionError("Algorithm " + mAlgorithm + " is not one of the allowed values");
            }
        });
    }

    @Override
    public boolean isSelected(Point p) {
        List<Point> vertices = getVertices();
        List<Pair<Point, Point>> edges = vertices.stream().collect(() -> Lists.newArrayList(Pair.of(null, null)), (ArrayList<Pair<Point, Point>> es, Point q) -> {
            es.get(es.size() - 1).last = q;
            es.add(Pair.of(q, null)); /* TODO: Unchecked */
        }, (a, b) -> {});
        edges = edges.subList(1, edges.size());
        edges.get(edges.size() - 1).last = vertices.get(0);
        return edges.stream()
                .filter(e -> Math.max(e.first.x, e.last.x) > p.x)
                .filter(e -> Math.min(e.first.y, e.last.y) < p.y && p.y < Math.max(e.first.y, e.last.y))
                .count() % 2 == 1;
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        mConfiguration = configuration.clone();
    }

    @Override
    public Configuration getConfiguration() {
        return mConfiguration.clone();
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        List<Point> vertices = getVertices();
        s.append(vertices.get(0));
        for (Point p : Iterables.skip(vertices, 1)) {
            s.append(" -> ").append(p);
        }
        s.append("\n");
        s.append("color = ").append(Integer.toHexString(mConfiguration.color)).append(", algorithm = ").append(mAlgorithm).append("\n");
        s.append("line = " + mConfiguration.pattern + "\n");
        s.append(mConfiguration.pattern2d);
        s.append("\n");
        return s.toString();
    }

    public static final Creator<PolygonFigure> CREATOR = new Creator<PolygonFigure>() {
        @Override
        public PolygonFigure create(Pack in) {
            return new PolygonFigure(in);
        }
    };

    public static enum FillAlgorithm { FLOOD_FILL, SCAN_LINE }
}
