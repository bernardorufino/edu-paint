package br.com.bernardorufino.paint.ext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by diogosfreitas on 04/09/2014.
 */
public class Polygon {
    private List<Point> mPolygon = new ArrayList<>();

    public Polygon(List<? extends Point> vertices) {
        mPolygon.addAll(vertices);
    }

    public int getYMax() {
        int yMax = 0;
        for(Point p : mPolygon) {
            if(p.y > yMax) {
                yMax = p.y;
            }
        }
        return yMax;
    }

    public int getYMin() {
        int yMin = 0;
        for(Point p : mPolygon) {
            if(p.y > yMin) {
                yMin = p.y;
            }
        }
        return yMin;
    }
/*
    public int getXMax() {
        int xMax = 0;
        for(Point p : mPolygon) {
            if(p.x > xMax) {
                xMax = p.x;
            }
        }
        return xMax;
    }

    public int getXMin() {
        int xMin = 0;
        for(Point p : mPolygon) {
            if(p.x > xMin) {
                xMin = p.x;
            }
        }
        return xMin;
    }


    public List<Point> getXCrossings(int y) {
        List<Point> points = new ArrayList<>();
        int xMin, xMax;
        xMin = getXMin();
        xMax = getXMax();
        for(int x = xMin; x<= xMax; x++) {
//falta fazer
        }
        return points;
    }

    */

    public List<Edge> getSortedEdges() {

        Edge[] sortedEdges = new Edge[mPolygon.size()-1];
        for (int i = 0; i < mPolygon.size() - 1; i++)
        {
            if (mPolygon.get(i).y < mPolygon.get(i + 1).y)
                sortedEdges[i] = new Edge(mPolygon.get(i), mPolygon.get(i + 1));
            else
                sortedEdges[i] = new Edge(mPolygon.get(i + 1), mPolygon.get(i));
        }
        return Arrays.asList(sortedEdges);
    }

    public List<Point> getPoints() {
        return mPolygon;
    }

}
