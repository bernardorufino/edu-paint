package br.com.bernardorufino.paint.tools;

import br.com.bernardorufino.paint.ext.Matrix;
import br.com.bernardorufino.paint.ext.Point;
import br.com.bernardorufino.paint.grapher.FrameBuffer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;

/**
 * Created by diogosfreitas on 03/10/2014.
 */
public class ZoomTool extends TwoStepDrawingTool {

    private boolean mZooming;
    private Point mFirstPoint;

    @Override
    public void onMouseClicked(MouseEvent event) {
        super.onMousePressed(event);
        if (mZooming) {
            zoom(mFirstPoint, getPosition(event), event);
            mZooming = false;
            notifyFinishUseListener();
        } else {
            notifyStartUseListener();
            mFirstPoint = getPosition(event);
            mZooming = true;
        }
    }

    @Override
    protected void draw(Point p, Point q, MouseEvent event) {

    }

    protected void zoom(Point p, Point q, MouseEvent event) {
        FrameBuffer fb = mGrapher.getFrameBuffer();
        GraphicsContext gc = fb.getGraphicsContext();
        Canvas canvas = gc.getCanvas();

        fb.setWindow(p.x, p.y, q.x, q.y);
        Matrix transformation = Matrix.identity();
        //canvas.setWidth(Math.abs(p.x - q.x));
        //canvas.setHeight(Math.abs(p.y - q.y));
    }
}
