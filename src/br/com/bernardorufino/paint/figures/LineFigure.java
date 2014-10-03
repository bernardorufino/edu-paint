package br.com.bernardorufino.paint.figures;

import br.com.bernardorufino.paint.ext.Matrix;
import br.com.bernardorufino.paint.ext.Pack;
import br.com.bernardorufino.paint.ext.Point;
import br.com.bernardorufino.paint.grapher.Configuration;
import br.com.bernardorufino.paint.grapher.Grapher;

import javax.sound.sampled.Line;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class LineFigure implements PersistableFigure {

    private Point mStart;
    private Point mEnd;
    private BitSet mPattern;
    private int mColor;
    private Configuration mConfiguration;
    private boolean mSelectable = true;

    public LineFigure(Point start, Point end, Configuration configuration) {
        mStart = start;
        mEnd = end;
        mConfiguration = configuration;
    }

    public LineFigure(Pack in) {
        mStart = in.readPersistable();
        mEnd = in.readPersistable();
        mConfiguration = in.readPersistable();
    }

    @Override
    public void persist(Pack out) {
        out.writePersistable(mStart);
        out.writePersistable(mEnd);
        out.writePersistable(mConfiguration);
    }

    @Override
    public void draw(Grapher grapher) {
        grapher.temporarilyWith(mConfiguration).execute(g -> {
            g.drawBresenhamLine(mStart, mEnd);
        });
    }

    private static final double LINE_TOLERANCE = 20;

    @Override
    public boolean isSelected(Point m) {
        Point p = mStart;
        Point q = mEnd;
        boolean dist = Math.pow((m.x - p.x) * (q.y - p.y) - (m.y - p.y) * (q.x - p.x), 2) / (Math.pow(q.x - p.x, 2) + Math.pow(q.y - p.y, 2)) <= LINE_TOLERANCE;
        double xmax = Math.max(p.x, q.x);
        double xmin = Math.min(p.x, q.x);
        double ymax = Math.max(p.y, q.y);
        double ymin = Math.min(p.y, q.y);
        boolean inside = (xmin - LINE_TOLERANCE) < m.x && m.x < (xmax + LINE_TOLERANCE) &&
                (ymin - LINE_TOLERANCE) < m.y && m.y < (ymax + LINE_TOLERANCE);
        return mSelectable && inside && dist;
    }

    public void setSelectable(boolean selectable) {
        mSelectable = selectable;
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
        return mStart + " -> " + mEnd;
    }

    public static final Creator<LineFigure> CREATOR = new Creator<LineFigure>() {
        @Override
        public LineFigure create(Pack in) {
            return new LineFigure(in);
        }
    };

    public Point getStart() {
        return mStart;
    }

    public Point getEnd() {
        return mEnd;
    }

    public LineFigure applyTransformation(Matrix matrix) {
        return new LineFigure(matrix.doTransformation(mStart), matrix.doTransformation(mEnd), mConfiguration);

    }

}
