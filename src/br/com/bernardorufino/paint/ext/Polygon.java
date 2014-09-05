package br.com.bernardorufino.paint.ext;

import br.com.bernardorufino.paint.ext.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by diogosfreitas on 04/09/2014.
 */
public class Polygon {
    List<Point> polygon;

    public Polygon(Point... points) {
        polygon = new ArrayList<Point>();
        for (Point p : points) {
            polygon.add(p);
        }
    }

    public int getYMax() {
        int yMax = 0;
        for(Point p : polygon) {
            if(p.y > yMax) {
                yMax = p.y;
            }
        }
        return yMax;
    }

    public int getYMin() {
        int yMin = 0;
        for(Point p : polygon) {
            if(p.y > yMin) {
                yMin = p.y;
            }
        }
        return yMin;
    }
/*
    public int getXMax() {
        int xMax = 0;
        for(Point p : polygon) {
            if(p.x > xMax) {
                xMax = p.x;
            }
        }
        return xMax;
    }

    public int getXMin() {
        int xMin = 0;
        for(Point p : polygon) {
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

        Edge[] sortedEdges = new Edge[polygon.size()-1];
        for (int i = 0; i < polygon.size() - 1; i++)
        {
            if (polygon.get(i).y < polygon.get(i + 1).y)
                sortedEdges[i] = new Edge(polygon.get(i), polygon.get(i + 1));
            else
                sortedEdges[i] = new Edge(polygon.get(i + 1), polygon.get(i));
        }
        return Arrays.asList(sortedEdges);
    }

    public List<Point> getPoints() {
        return polygon;
    }

}
