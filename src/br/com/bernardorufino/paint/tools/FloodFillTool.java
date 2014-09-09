package br.com.bernardorufino.paint.tools;

import br.com.bernardorufino.paint.ext.Point;

import java.util.List;

public class FloodFillTool extends PolygonTool {

    @Override
    protected void fill(List<Point> polygon) {
        mGrapher.floodFillPolygon(polygon);
    }
}
