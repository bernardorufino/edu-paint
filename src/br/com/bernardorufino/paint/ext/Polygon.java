package br.com.bernardorufino.paint.ext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Polygon {
    private List<Point> mVertices = new ArrayList<>();

    public Polygon(List<? extends Point> vertices) {
        mVertices.addAll(vertices);
    }

    public int getYMax() {
        int yMax = 0;
        for(Point p : mVertices) {
            if(p.y > yMax) {
                yMax = p.y;
            }
        }
        return yMax;
    }

    public int getYMin() {
        int yMin = mVertices.get(0).y;
        for(Point p : mVertices) {
            if(p.y < yMin) {
                yMin = p.y;
            }
        }
        return yMin;
    }

    public Edge[] getEdges() {
        Edge[] edges = new Edge[mVertices.size()];

        for (int i = 0; i < mVertices.size(); i++)
        {
            if (mVertices.get(i).y < mVertices.get((i + 1)%(mVertices.size())).y)
                edges[i] = new Edge(mVertices.get(i), mVertices.get((i + 1)%(mVertices.size())));
            else
                edges[i] = new Edge(mVertices.get((i + 1)%(mVertices.size())), mVertices.get(i));
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

    public List<Point> getVertices() {
        return mVertices;
    }

}
