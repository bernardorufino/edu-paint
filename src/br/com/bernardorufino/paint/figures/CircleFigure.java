package br.com.bernardorufino.paint.figures;

import br.com.bernardorufino.paint.ext.Pack;
import br.com.bernardorufino.paint.ext.Point;
import br.com.bernardorufino.paint.grapher.Configuration;
import br.com.bernardorufino.paint.grapher.Grapher;

public class CircleFigure implements PersistableFigure {

    /* TODO: Group patterns and color in a object */
    private Point mCenter;
    private double mRadius;
    private boolean mFill;
    private Configuration mConfiguration;

    /* TODO: Fill to use with circleFill */
    public CircleFigure(Point center, double radius, boolean fill, Configuration configuration) {
        mCenter = center;
        mRadius = radius;
        mFill = fill;
        mConfiguration = configuration;
    }

    public CircleFigure(Pack in) {
        mCenter = in.readPersistable();
        mRadius = in.readDouble();
        mFill = in.readBoolean();
        mConfiguration = in.readPersistable();
    }

    @Override
    public void persist(Pack out) {
        out.writePersistable(mCenter);
        out.writeDouble(mRadius);
        out.writeBoolean(mFill);
        out.writePersistable(mConfiguration);
    }

    @Override
    public void draw(Grapher grapher) {
        grapher.temporarilyWith(mConfiguration).execute(g -> {
            if (mFill) {
                g.circleFill(mCenter, mRadius);
            } else {
                g.drawCircle(mCenter, mRadius);
            }
        });
    }

    @Override
    public boolean isSelected(Point p) {
        Point c = mCenter;
        double r = mRadius;
        return Math.sqrt(Math.pow(p.x - c.x, 2) + Math.pow(p.y - c.y, 2)) <= r;
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        mConfiguration = configuration.clone();
    }

    @Override
    public Configuration getConfiguration() {
        return mConfiguration.clone();
    }

    public static final Creator<CircleFigure> CREATOR = new Creator<CircleFigure>() {
        @Override
        public CircleFigure create(Pack in) {
            return new CircleFigure(in);
        }
    };
}
