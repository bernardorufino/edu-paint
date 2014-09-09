package br.com.bernardorufino.paint.tools;

import br.com.bernardorufino.paint.ext.Point;
import br.com.bernardorufino.paint.grapher.FrameBuffer;
import br.com.bernardorufino.paint.grapher.Grapher;
import br.com.bernardorufino.paint.ui.WindowController;
import br.com.bernardorufino.paint.utils.DrawUtils;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.BitSet;
import java.util.function.Consumer;

public abstract class Tool {

    protected GraphicsContext mGc;
    protected Label vStatus;
    protected Grapher mGrapher;
    protected FrameBuffer mFb;
    private Property<Color> mColorProperty = new SimpleObjectProperty<>();
    private Property<BitSet> mPatternProperty = new SimpleObjectProperty<>();
    private int mResizeFactor;
    private Consumer<Tool> mOnStartUseListener;
    private Consumer<Tool> mOnFinishUseListener;

    public Tool bind(Grapher grapher, Label status, int resizeFactor) {
        vStatus = status;
        mGrapher = grapher;
        mFb = grapher.getFrameBuffer();
        mGc = mFb.getGraphicsContext();
        mResizeFactor = resizeFactor;
        return this;
    }

    public void onMousePressed(MouseEvent event) {
        /* Override */
    }

    public void onMouseDragged(MouseEvent event) {
        /* Override */
    }

    public void onMouseReleased(MouseEvent event) {
        /* Override */
    }

    public void onMouseClicked(MouseEvent event) {
        /* Override */
    }

    public void onMouseMoved(MouseEvent event) {
        Point p = getPosition(event);
        vStatus.setText(String.format(WindowController.COORD_STATUS, p.x, p.y));
    }

    public Property<BitSet> patternProperty() {
        return mPatternProperty;
    }

    public BitSet getPattern() {
        return mPatternProperty.getValue();
    }

    public Property<Color> colorProperty() {
        return mColorProperty;
    }

    protected int getColor() {
        return DrawUtils.fromColorToInt(mColorProperty.getValue());
    }

    protected Point getPosition(double x, double y) {
        int i = DrawUtils.indexFor(x, mResizeFactor);
        int j = DrawUtils.indexFor(y, mResizeFactor);
        return Point.at(i, j);
    }

    protected Point getPosition(MouseEvent event) {
        return getPosition(event.getX(), event.getY());
    }

    public void setOnStartUseListener(Consumer<Tool> listener) {
        mOnStartUseListener = listener;
    }

    public void setOnFinishUseListener(Consumer<Tool> listener) {
        mOnFinishUseListener = listener;
    }

    protected void notifyStartUseListener() {
        mOnStartUseListener.accept(this);
    }

    protected void notifyFinishUseListener() {
        mOnFinishUseListener.accept(this);
    }
}
