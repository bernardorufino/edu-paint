package br.com.bernardorufino.paint.ext;

/**
 * Created by diogosfreitas on 04/09/2014.
 */
public class Edge {
    public Point p1, p2;
    private double x, m;

    public Edge(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
        m = ((double)(p1.y - p2.y))/((double)(p1.x - p2.x));
    }

    public void update() {
        x += (1/(double)m);
    }

    public void on() {
        x = p1.x;
    }

    public void off() {
        x = p2.x;
    }

    public double getX() {
        return x;
    }
}
