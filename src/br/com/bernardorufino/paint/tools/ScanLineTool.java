package br.com.bernardorufino.paint.tools;

import br.com.bernardorufino.paint.ext.Point;
import br.com.bernardorufino.paint.ext.Polygon;

import java.util.List;

public class ScanLineTool extends PolygonTool {
    @Override
    protected void fill(List<Point> polygon) {
        /* TODO: Remove! */
        //polygon.add(polygon.get(polygon.size() - 1));
        mGrapher.scanFill(new Polygon(polygon));
    }
}
