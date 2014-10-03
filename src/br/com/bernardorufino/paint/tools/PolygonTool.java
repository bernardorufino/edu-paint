package br.com.bernardorufino.paint.tools;

import br.com.bernardorufino.paint.ext.Point;
import br.com.bernardorufino.paint.figures.LineFigure;
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
                notifyStartUseListener();
                mGrapher.drawPixel(p);
            } else {
                Point q = mPolygon.get(mPolygon.size() - 1);
                LineFigure line = mGrapher.drawBresenhamLine(q, p);
                //line.setSelectable(false);
                addPersistableFigure(line);
            }
            mPolygon.add(p);
        } else if (event.getButton() == MouseButton.SECONDARY) {
            if (mPolygon.size() <= 2) {
                mPolygon.clear();
                return;
            }
            Point q = mPolygon.get(mPolygon.size() - 1);
            Point r = mPolygon.get(0);
            LineFigure line = mGrapher.drawBresenhamLine(q, r);
            addPersistableFigure(line);
            fill(new ArrayList<>(mPolygon));
            mPolygon.clear();
            notifyFinishUseListener();
        }
    }

    protected abstract void fill(List<Point> polygon);
}
