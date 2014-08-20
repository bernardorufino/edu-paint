package br.com.bernardorufino.paint.tools;

import br.com.bernardorufino.paint.ext.Point;
import javafx.scene.input.MouseEvent;

public class FreeTool extends Tool {

    private boolean mDrawing = false;
    private Point mOrigin;

    @Override
    public void onMouseDragged(MouseEvent event) {
        super.onMouseDragged(event);
        Point p = getPosition(event);
        if (!mFb.contains(p)) return;
        //p = mFb.nearestValid(p);
        if (!mDrawing) {
            mOrigin = p;
            mDrawing = true;
        }
        mGrapher.drawBresenhamLine(mOrigin, p);
        mOrigin = p;
    }

    @Override
    public void onMouseReleased(MouseEvent event) {
        super.onMouseReleased(event);
        if (mDrawing) {
            mDrawing = false;
        }
    }
}
