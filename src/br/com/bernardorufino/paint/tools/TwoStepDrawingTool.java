package br.com.bernardorufino.paint.tools;

import br.com.bernardorufino.paint.ext.Point;
import javafx.scene.input.MouseEvent;

public abstract class TwoStepDrawingTool extends Tool {

    private boolean mDrawing;
    private Point mFirstPoint;

    @Override
    public void onMouseClicked(MouseEvent event) {
        super.onMousePressed(event);
        if (mDrawing) {
            mFb.rollback();
            draw(mFirstPoint, getPosition(event), event);
            mDrawing = false;
        } else {
            mFirstPoint = getPosition(event);
            mFb.begin();
            mGrapher.drawPixel(mFirstPoint);
            mDrawing = true;
        }
    }

    @Override
    public void onMouseMoved(MouseEvent event) {
        super.onMouseMoved(event);
        if (mDrawing) {
            mFb.rollback().begin();
            draw(mFirstPoint, getPosition(event), event);
        }
    }

    protected abstract void draw(Point p, Point q, MouseEvent event);
}
