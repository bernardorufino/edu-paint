package br.com.bernardorufino.paint.tools;

import br.com.bernardorufino.paint.ext.Point;
import br.com.bernardorufino.paint.figures.LineFigure;
import javafx.scene.input.MouseEvent;

public class DdaLineTool extends TwoStepDrawingTool {

    @Override
    protected void draw(Point p, Point q, MouseEvent event) {
        LineFigure line = mGrapher.drawDdaLine(p, q);
        setPersistableFigure(line);
    }
}
