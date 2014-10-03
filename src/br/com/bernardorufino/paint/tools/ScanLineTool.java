package br.com.bernardorufino.paint.tools;

import br.com.bernardorufino.paint.ext.Point;
import br.com.bernardorufino.paint.figures.Polygon;
import br.com.bernardorufino.paint.figures.PolygonFigure;

import java.util.List;

public class ScanLineTool extends PolygonTool {
    @Override
    protected void fill(List<Point> polygon) {
        /* TODO: Remove! */
        //polygon.add(polygon.get(polygon.size() - 1));
        PolygonFigure polygonFigure = mGrapher.scanFill(new Polygon(polygon));
        addPersistableFigure(polygonFigure);
    }


}
