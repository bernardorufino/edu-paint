package br.com.bernardorufino.paint.tools;

import br.com.bernardorufino.paint.ext.Point;
import br.com.bernardorufino.paint.figures.Polygon;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by diogosfreitas on 03/10/2014.
 */
public class ZoomTool extends Tool {

    private boolean mZooming;
    private Point mFirstPoint;

    @Override
    public void onMouseClicked(MouseEvent event) {
        super.onMousePressed(event);
        if (mZooming) {
            mFb.rollback();
            zoom(mFirstPoint, getPosition(event), event);
            mZooming = false;
            notifyFinishUseListener();
        } else {
            notifyStartUseListener();
            mFirstPoint = getPosition(event);
            mFb.begin();
            mGrapher.drawPixel(mFirstPoint);
            mZooming = true;
        }
    }

    @Override
    public void onMouseMoved(MouseEvent event) {
        super.onMouseMoved(event);
        if (mZooming) {
            mFb.rollback().begin();
            draw(mFirstPoint, getPosition(event), event);
        }
    }

    protected void draw(Point p, Point q, MouseEvent event) {
        List<Point> vertices = new ArrayList<Point>();
        vertices.add(p);
        vertices.add(Point.at(q.x, p.y));
        vertices.add(q);
        vertices.add(Point.at(p.x, q.y));
        Polygon rectangle = new Polygon(vertices);
        mGrapher.drawPolygon(rectangle);
    }
    protected void zoom(Point p, Point q, MouseEvent event) {
        mGrapher.applyZoom(p, q);
    }
}
