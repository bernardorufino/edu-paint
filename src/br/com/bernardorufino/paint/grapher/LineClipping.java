package br.com.bernardorufino.paint.grapher;

import br.com.bernardorufino.paint.ext.Edge;
import br.com.bernardorufino.paint.ext.Point;
import br.com.bernardorufino.paint.figures.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by diogosfreitas on 02/10/2014.
 */
public class LineClipping {
    private double height, width;
    private int mWXL = 0, mWYL = 0,
            mWXH = 1, mWYH = 1;
    private double mVXL = 0, mVYL = 0,
            mVXH = 1, mVYH = 1;
    private double mVWSX, mVWSY;

    private int inside = 0, bottom = 1, top = 2, right = 4, left = 8;

    public Point XEdgeIntersection(Edge edge, int wy) {
        int x = (int) (edge.p1.x + (edge.p2.x - edge.p1.x) * (wy - edge.p1.y) / (edge.p2.y - edge.p1.y));
        int y = wy;
        return Point.at(x, y);
    }

    public Point YEdgeIntersection(Edge edge, int wx) {
        int x = wx;
        int y = (int) (edge.p1.y + (edge.p2.y - edge.p1.y) * (wx - edge.p1.x) / (edge.p2.x - edge.p1.x));
        return Point.at(x, y);
    }

    public int getCode2D(Point p) {
        int c;
        c = inside;
        if(p.x < mWXL)
            c |= left ;
        else if(p.x > mWXH)
            c |= right;
        if(p.y < mWYL)
            c |=  bottom;
        else if( p.y > mWYH)
            c |= top ;
        return c;
    }

    public void setWindow(Point p, Point q) {
        mWXL = p.x; mWYL = p.y;
        mWXH = q.x; mWYH = q.y;
        mVWSX = (mVXH - mVXL)/(mWXH - mWXL);
        mVWSY = (mVYH - mVYL)/(mWYH - mWYL);
    }

    public Edge clip2D(Edge edge) {
        Edge newEdge = new Edge(edge.p1, edge.p2);
        int c , c1 , c2;
        Point xy;
        c1 = getCode2D(edge.p1);
        c2 = getCode2D(edge.p2);

        while (((c1 != inside ) || (c2 != inside)) )
        {
            if ( (c1&c2) != inside )
                return newEdge;
            else
            {
                xy = newEdge.p1;
                c = c1;
                if(c == inside)
                    c = c2;
                if((left & c) != inside)
                    xy = YEdgeIntersection(edge, mWXL);
                else if ((right & c) != inside)
                    xy = YEdgeIntersection(edge, mWXH);
                else if((bottom & c) != inside)
                    xy = XEdgeIntersection(edge, mWYL);
                else if((top & c) != inside)
                    xy = XEdgeIntersection(edge, mWYH);
                if (c == c1) {
                    newEdge.p1 = xy;
                    c1 = getCode2D (newEdge.p1);
                }
                else {
                    xy = newEdge.p2;
                    c = c2;
                    if(c == inside)
                        c = c1;
                    if((left & c) != inside)
                        xy = YEdgeIntersection(newEdge, mWXL);
                    else if ((right & c) != inside)
                        xy = YEdgeIntersection(newEdge, mWXH);
                    else if((bottom & c) != inside)
                        xy = XEdgeIntersection(newEdge, mWYL);
                    else if((top & c) != inside)
                        xy = XEdgeIntersection(newEdge, mWYH);
                    if (c == c2) {
                        newEdge.p2 = xy;
                        c2 = getCode2D (newEdge.p2);
                    }
                }
            }
        }
        return newEdge;
    }


    public enum windowEdge { TOP, RIGHT, BOTTOM, LEFT };

    public Polygon clipPolygon(Polygon polygon) {
        List<Point> poly_in = polygon.getVertices();
        List<Point> poly_out = new ArrayList<Point>();
        for(windowEdge wEdge : windowEdge.values()) {
            poly_out.clear();
            poly_in.add(poly_in.get(0));
            for(int i = 0; i < poly_in.size()-1; i++) {
                Point p1 = poly_in.get(i);
                Point p2 = poly_in.get(i+1);
                poly_out.addAll(clipEdge(new Edge(Point.at(p1.x, p1.y), Point.at(p2.x, p2.y)), wEdge));
            }
            poly_in = new ArrayList<>(poly_out);
        }
        return new Polygon(poly_out);
    }

    public List<Point> clipEdge(Edge edge, windowEdge wEdge) {
        List<Point> vertices = new ArrayList<Point>();
        if(isVisible(edge.p1, wEdge)) {
            vertices.add(edge.p1);
        }
        if (hasIntersection(edge, wEdge)) {
            vertices.add(getIntersection(edge, wEdge));
        }
        return vertices;
    }

    private Point getIntersection(Edge edge, windowEdge wEdge) {
        Point pi = Point.copy(edge.p1);
        switch (wEdge) {
            case LEFT:
                pi.x = mWXL;
                pi.y = (int) (edge.p1.y + (edge.p2.y - edge.p1.y) * (pi.x - edge.p1.x) / (double)(edge.p2.x - edge.p1.x));
                break;
            case RIGHT:
                pi.x = mWXH;
                pi.y = (int) (edge.p1.y + (edge.p2.y - edge.p1.y) * (pi.x - edge.p1.x) / (double)(edge.p2.x - edge.p1.x));
                break;
            case TOP:
                pi.y = mWYL;
                pi.x = (int) (edge.p1.x + (edge.p2.x - edge.p1.x) * (pi.y - edge.p1.y) / (double)(edge.p2.y - edge.p1.y));
                break;
            case BOTTOM:
                pi.y = mWYH;
                pi.x = (int) (edge.p1.x + (edge.p2.x - edge.p1.x) * (pi.y - edge.p1.y) / (double)(edge.p2.y - edge.p1.y));
                break;
        }
        return pi;
    }

    private boolean hasIntersection(Edge edge, windowEdge wEdge) {
        switch (wEdge) {
            case LEFT: return ( (edge.p1.x - mWXL)*(edge.p2.x - mWXL) < 0 );
            case RIGHT: return ( (edge.p1.x - mWXH)*(edge.p2.x - mWXH) < 0 );
            case TOP: return ( (edge.p1.y - mWYL)*(edge.p2.y - mWYL) < 0 );
            case BOTTOM: return ( (edge.p1.y - mWYH)*(edge.p2.y - mWYH) < 0 );
        } return false;
    }

    private boolean isVisible(Point p, windowEdge wEdge) {
        switch (wEdge) {
            case LEFT: return (p.x >= mWXL);
            case RIGHT: return (p.x <= mWXH);
            case TOP: return (p.y >= mWYL);
            case BOTTOM: return (p.y <= mWYH);
        } return false;
    }


    /*
    private double x_start, y_start, x_end, y_end, height, width;
    private double  mWXL = 0, mWYL = 0,
                    mWXH = 1, mWYH = 1;
    private double  mVXL = 0, mVYL = 0,
                    mVXH = 1, mVYH = 1;
    private double mVWSX, mVWSY;
    private double mX, mY;

    private int inside = 0, bottom = 1, top = 2, right = 4, left = 8;

    private void setWindow(double x1, double y1, double x2, double y2) {
        mWXL = x1; mWYL = y1;
        mWXH = x2; mWYH = y2;
        mVWSX = (mVXH - mVXL)/(mWXH - mWXL);
        mVWSY = (mVYH - mVYL)/(mWYH - mWYL);
    }

    private void setViewport(double x1, double y1, double x2, double y2) {
        mVXL = x1; mVYL = y1;
        mVXH = x2; mVYH = y2;
        mVWSX = (mVXH - mVXL)/(mWXH - mWXL);
        mVWSY = (mVYH - mVYL)/(mWYH - mWYL);
    }

    private Pair<Double,Double> ViewingTransformation(double x, double y) {
        double x1 = (x - mWXL)*mVWSX + mVXL;
        double y1 = (y  - mWYL)*mVWSY + mVYL;
        return Pair.of(x1, y1);
    }

    private Pair<Integer,Integer> NormalizedToDevice(double x, double y) {
        int xd = (int) (x_start + width * x);
        int yd = (int) (y_end - (y_start + height * y) );
        return Pair.of(xd, yd);
    }
    */
}
