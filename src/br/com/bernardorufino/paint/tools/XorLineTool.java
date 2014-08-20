package br.com.bernardorufino.paint.tools;

import br.com.bernardorufino.paint.ext.Point;
import javafx.scene.input.MouseEvent;

public class XorLineTool extends TwoStepDrawingTool {

    @Override
    protected void draw(Point p, Point q, MouseEvent event) {
        mGrapher.drawXorLine(p, q);
    }
}
