package br.com.bernardorufino.paint.tools;

import br.com.bernardorufino.paint.ext.Point;
import br.com.bernardorufino.paint.ext.Polygon;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;

public class PolygonTool extends Tool {

    private List<Point> mPolygon = new ArrayList<>();

    @Override
    public void onMouseClicked(MouseEvent event) {
        super.onMouseClicked(event);
        if (event.getButton() == MouseButton.PRIMARY) {
            mPolygon.add(getPosition(event));
        } else if (event.getButton() == MouseButton.SECONDARY) {
            mPolygon.add(getPosition(event));
     //       mGrapher.drawPolygon(new Polygon(mPolygon));
            mGrapher.scanFill(new Polygon(mPolygon));
            // Call grapher with the points
            mPolygon.clear();
        }
    }
}
