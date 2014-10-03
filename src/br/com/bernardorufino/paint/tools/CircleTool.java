package br.com.bernardorufino.paint.tools;

import br.com.bernardorufino.paint.ext.Point;
import br.com.bernardorufino.paint.ext.Points;
import br.com.bernardorufino.paint.figures.CircleFigure;
import javafx.scene.input.MouseEvent;

public class CircleTool extends TwoStepDrawingTool {

    @Override
    protected void draw(Point p, Point q, MouseEvent event) {
        Point c;
        double r;
        if (event.isControlDown() && event.isAltDown()) {
            // Circumference defined by first point and second defining point in the horizontal of the first
            q = (event.isShiftDown()) ? Point.at(p.x, q.y) : Point.at(q.x, p.y);
            c = Points.midpoint(p, q);
            r = Points.distance(p, q) / 2;
        } else if (event.isControlDown()) {
            // Circumference inside square defined by p and q as diagonal opposite vertices
            c = Points.midpoint(p, q);
            r = Math.abs(q.minus(p).x) / 2;
        } else if (event.isAltDown()) {
            // Circumference defined by diametrical opposite points
            c = Points.midpoint(p, q);
            r = Points.distance(p, q) / 2;
        } else {
            // Circumference defined by center and a defining point
            c = p;
            r = Points.distance(p, q);
        }
        CircleFigure circle = mGrapher.drawCircle(c, r);
        setPersistableFigure(circle);
    }
}
