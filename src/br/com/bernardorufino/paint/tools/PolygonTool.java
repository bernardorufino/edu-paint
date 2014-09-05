package br.com.bernardorufino.paint.tools;

import br.com.bernardorufino.paint.ext.Point;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class PolygonTool extends Tool {

    private List<Point> mPolygon = new ArrayList<>();

    @Override
    public void onMouseClicked(MouseEvent event) {
        super.onMouseClicked(event);
        if (event.getButton() == MouseButton.PRIMARY) {
            Point p = getPosition(event);
            if (mPolygon.isEmpty()) {
                mGrapher.drawPixel(p);
            } else {
                Point q = mPolygon.get(mPolygon.size() - 1);
                mGrapher.drawBresenhamLine(q, p);
            }
            mPolygon.add(p);
        } else if (event.getButton() == MouseButton.SECONDARY) {
            if (mPolygon.size() < 2) {
                return;
            }
            Point q = mPolygon.get(mPolygon.size() - 1);
            Point r = mPolygon.get(0);
            mGrapher.drawBresenhamLine(q, r);
            fill(mPolygon);
            mPolygon.clear();
        }
    }

    protected abstract void fill(List<Point> polygon);
}
