package br.com.bernardorufino.paint.grapher;

import br.com.bernardorufino.paint.ext.Matrices;
import br.com.bernardorufino.paint.ext.Point;
import br.com.bernardorufino.paint.utils.DrawUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.Set;

public class FrameBuffer {

    public static FrameBuffer empty(GraphicsContext gc, int resizeFactor) {
        return new FrameBuffer(gc, resizeFactor).clean();
    }

    public static final int BG_COLOR = 0xFFFFFFFF;

    private int[][] mFb;
    private final GraphicsContext mGc; /* TODO: privatize */
    private int mWidth;
    private int mHeight;
    private final int mResizeFactor;
    private Set<Point> mModifiedPoints;
    private boolean mRecoverableMode;
    private State mSavedState;

    private double mX_start, mY_start, mX_end, mY_end;
    private double  mWXS = 0, mWYS = 0,
            mWXH = 1, mWYH = 1;
    private double  mVXS = 0, mVYS = 0,
            mVXH = 1, mVYH = 1;
    private double mVWSX, mVWSY;
    private double mX, mY;

    private int inside = 0, bottom = 1, top = 2, right = 4, left = 8;


    public FrameBuffer(GraphicsContext gc, int resizeFactor) {
        mGc = gc;
        mResizeFactor = resizeFactor;
        Canvas canvas = gc.getCanvas();


        mX_start = mY_start = 0;
        mX_end = mWidth = (int) canvas.getWidth();
        mY_end = mHeight = (int) canvas.getHeight();

        setViewport(0, 0, 1, 1);
        setWindow(0, 0, mWidth, mHeight);

        mFb = new int[i(mWidth)][i(mHeight)];
    }

    public FrameBuffer clean() {
        mGc.setFill(c(BG_COLOR));
        mGc.fillRect(0, 0, mWidth, mHeight);
        mGc.fill();
        Matrices.map(mFb, (i, j, v) -> BG_COLOR);
        return this;
    }

    // Starts recoverable mode with begin(), commits with commit() and undos with rollback()
    public FrameBuffer begin() {
        mRecoverableMode = true;
        mSavedState = save();
        mModifiedPoints = new HashSet<>();
        return this;
    }
/*
    public FrameBuffer commit() {
        mRecoverableMode = false;
        mSavedState = save();
        mModifiedPoints = new HashSet<>();
        return this;
    }*/

    public FrameBuffer rollback() {
        mRecoverableMode = false;
        mModifiedPoints.forEach(p -> setPixel(p.x, p.y, mSavedState.mFb[p.x][p.y]));
        mModifiedPoints = new HashSet<>();
        return this;
    }

    // Color is 0xAARRGGBB, does not support alpha yet
    public FrameBuffer setPixel(int x, int y, int color) {
        if (mRecoverableMode) {
            mModifiedPoints.add(Point.at(x, y));
        }
        mFb[x][y] = color;
        mGc.setFill(c(color));
        mGc.fillRect(x * mResizeFactor, y * mResizeFactor, mResizeFactor, mResizeFactor);
        mGc.fill();
        return this;
    }

    public int getPixel(int x, int y) {
        return mFb[x][y];
    }

    public boolean contains(Point p) {
        return 0 <= p.x && p.x < mFb.length && 0 <= p.y && p.y < mFb[0].length;
    }
/*
    public Point nearestValid(Point p) {
        int w = mFb.length - 1;
        int h = mFb[0].length - 1;
        return Point.at(Math.max(0, Math.min(w, p.x)), Math.max(0, Math.min(h, p.y)));
    }*/
/*
    public FrameBuffer restore(State state) {
        checkState(!mRecoverableMode, "Cannot restore in recoverable mode");
        Matrices.forEach(mFb, (i, j, value) -> {
            setPixel(i, j, state.mFb[i][j]);
        });
        return this;
    }*/

    public State save() {
        return new State(mFb);
    }

    /* Short aliases */

    private Color c(int color) {
        return DrawUtils.fromIntTocolor(color);
    }

    private int i(double r) {
        return DrawUtils.indexFor(r, mResizeFactor);
    }

    public GraphicsContext getGraphicsContext() {
        return mGc;
    }

    public void setWindow(double x1, double y1, double x2, double y2) {
        mWXS = x1; mWYS = y1;
        mWXH = x2; mWYH = y2;
        mVWSX = (mVXH - mVXS)/(mWXH - mWXS);
        mVWSY = (mVYH - mVYS)/(mWYH - mWYS);
    }

    public void setViewport(double x1, double y1, double x2, double y2) {
        mVXS = x1; mVYS = y1;
        mVXH = x2; mVYH = y2;
        mVWSX = (mVXH - mVXS)/(mWXH - mWXS);
        mVWSY = (mVYH - mVYS)/(mWYH - mWYS);
    }

    public static class State {

        private final int[][] mFb;

        public State(int[][] fb) {
            mFb = new int[fb.length][fb[0].length];
            Matrices.map(mFb, (i, j, value) -> fb[i][j]);
        }
    }

}
