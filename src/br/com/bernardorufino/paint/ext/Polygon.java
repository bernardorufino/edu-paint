package br.com.bernardorufino.paint.ext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        int yMin = mPolygon.get(0).y;
        for(Point p : mPolygon) {
            if(p.y < yMin) {
                yMin = p.y;
            }
        }
        return yMin;
    }

    public Edge[] getEdges() {

        Edge[] edges = new Edge[mPolygon.size()];

        for (int i = 0; i < mPolygon.size(); i++)
        {
            if (mPolygon.get(i).y < mPolygon.get((i + 1)%(mPolygon.size())).y)
                edges[i] = new Edge(mPolygon.get(i), mPolygon.get((i + 1)%(mPolygon.size())));
            else
                edges[i] = new Edge(mPolygon.get((i + 1)%(mPolygon.size())), mPolygon.get(i));
        }
        return edges;
    }

    public List<Edge> getSortedEdges() {
        Edge[] edges = getEdges();
        Edge tmp;
        for (int i = 0; i < edges.length - 1; i++) {
            for (int j = 0; j < edges.length - 1; j++) {
                if (edges[j].p1.y > edges[j + 1].p1.y) {
                    tmp = edges[j];
                    edges[j] = edges[j + 1];
                    edges[j + 1] = tmp;
                }
            }
        }
        return Arrays.asList(edges);
    }
/*
    public List<Point> getVertices() {
        return mPolygon;
    }*/

}
