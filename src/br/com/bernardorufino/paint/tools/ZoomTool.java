package br.com.bernardorufino.paint.tools;

import br.com.bernardorufino.paint.ext.Pair;
import br.com.bernardorufino.paint.ext.Point;
import br.com.bernardorufino.paint.figures.PersistableFigure;
import br.com.bernardorufino.paint.figures.Polygon;
import br.com.bernardorufino.paint.ui.WindowController;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by diogosfreitas on 03/10/2014.
 */
public class ZoomTool extends Tool {

    private final Stack<List<PersistableFigure>> mFiguresStack = new Stack<>();
    private final List<PersistableFigure> mFigures;
    private final WindowController mWindow;
    private boolean mZooming;
    private Point mFirstPoint;

    public ZoomTool(WindowController window, List<PersistableFigure> mFigures) {
        mWindow = window;
        this.mFigures = mFigures;
    }

    @Override
    public void onMouseClicked(MouseEvent event) {
        super.onMousePressed(event);
        if (mZooming) {
            mFb.rollback();
            Pair<Point, Point> points = adjustZoom(mFirstPoint, getPosition(event));
            zoom(points.first, points.last, event);
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
            Pair<Point, Point> points = adjustZoom(mFirstPoint, getPosition(event));
            draw(points.first, points.last, event);
        }
    }

    private Pair<Point, Point> adjustZoom(Point p, Point q) {
        double ar = (double) mFb.getWidth() / mFb.getHeight();
        double sx = (double) mFb.getWidth() / Math.abs(p.x - q.x);
        double sy = (double) mFb.getHeight() / Math.abs(p.y - q.y);

        switch (getPositionOfFirst(p, q)) {
            case TOP_LEFT:
                if (sx < sy) {
                    q.x = (int) (p.x + Math.abs(p.y - q.y) * ar);
                } else {
                    q.y = (int) (p.y + Math.abs(p.x - q.x) / ar);
                }
                break;
            case TOP_RIGHT:
                if (sx < sy) {
                    q.x = (int) (p.x - Math.abs(p.y - q.y) * ar);
                } else {
                    q.y = (int) (p.y + Math.abs(p.x - q.x) / ar);
                }
                break;
            case BOTTOM_RIGHT:
                if (sx < sy) {
                    q.x = (int) (p.x - Math.abs(p.y - q.y) * ar);
                } else {
                    q.y = (int) (p.y - Math.abs(p.x - q.x) / ar);
                }
                break;
            case BOTTOM_LEFT:
                if (sx < sy) {
                    q.x = (int) (p.x + Math.abs(p.y - q.y) * ar);
                } else {
                    q.y = (int) (p.y - Math.abs(p.x - q.x) / ar);
                }
                break;
        }

        Point pf = Point.at(Math.min(p.x, q.x), Math.min(p.y, q.y));
        Point qf = Point.at(Math.max(p.x, q.x), Math.max(p.y, q.y));
        return Pair.of(pf, qf);
    }

    @Override
    public void onKeyTyped(KeyEvent e) {
        super.onKeyTyped(e);
        int ch = e.getCharacter().toCharArray()[0];
        if ((e.getCharacter().equals("z") || e.getCharacter().equals("Z") || ch == 26) && e.isControlDown() && !e.isShiftDown()) {
            if (!mFiguresStack.isEmpty()) {
                List<PersistableFigure> figures = mFiguresStack.pop();
                mWindow.resetFigures(figures);
            }
        }
    }

    private RectangleVertex getPositionOfFirst(Point p, Point q) {
        int xmin = Math.min(p.x, q.x);
        int xmax = Math.max(p.x, q.x);
        int ymin = Math.min(p.y, q.y);
        int ymax = Math.max(p.y, q.y);
        if (p.equals(Point.at(xmin, ymin))) return RectangleVertex.TOP_LEFT;
        if (p.equals(Point.at(xmax, ymin))) return RectangleVertex.TOP_RIGHT;
        if (p.equals(Point.at(xmax, ymax))) return RectangleVertex.BOTTOM_RIGHT;
        if (p.equals(Point.at(xmin, ymax))) return RectangleVertex.BOTTOM_LEFT;
        return null;
    }

    protected void draw(Point p, Point q, MouseEvent event) {
        List<Point> vertices = new ArrayList<>();
        vertices.add(p);
        vertices.add(Point.at(q.x, p.y));
        vertices.add(q);
        vertices.add(Point.at(p.x, q.y));
        Polygon rectangle = new Polygon(vertices);
        mGrapher.drawPolygon(rectangle);
    }

    protected void zoom(Point p, Point q, MouseEvent event) {
        List<? extends PersistableFigure> newFigures = mGrapher.applyZoom(p, q, mFigures);
        mFiguresStack.push(new ArrayList<>(mFigures));
        mWindow.resetFigures(new ArrayList<>(newFigures));
    }

    private static enum RectangleVertex { TOP_LEFT, TOP_RIGHT, BOTTOM_RIGHT, BOTTOM_LEFT }
}
