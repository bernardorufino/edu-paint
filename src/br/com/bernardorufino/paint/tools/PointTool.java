package br.com.bernardorufino.paint.tools;

import javafx.scene.input.MouseEvent;

public class PointTool extends Tool {

    @Override
    public void onMouseClicked(MouseEvent event) {
        super.onMouseClicked(event);
        mGrapher.drawPixel(getPosition(event));
    }
}
