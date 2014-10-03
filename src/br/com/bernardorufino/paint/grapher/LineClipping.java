package br.com.bernardorufino.paint.grapher;

import br.com.bernardorufino.paint.ext.Pair;

/**
 * Created by diogosfreitas on 02/10/2014.
 */
public class LineClipping {
    private double x_start, y_start, x_end, y_end, height, width;
    private double  mWXS = 0, mWYS = 0,
                    mWXH = 1, mWYH = 1;
    private double  mVXS = 0, mVYS = 0,
                    mVXH = 1, mVYH = 1;
    private double mVWSX, mVWSY;
    private double mX, mY;

    private int inside = 0, bottom = 1, top = 2, right = 4, left = 8;

    private void setWindow(double x1, double y1, double x2, double y2) {
        mWXS = x1; mWYS = y1;
        mWXH = x2; mWYH = y2;
        mVWSX = (mVXH - mVXS)/(mWXH - mWXS);
        mVWSY = (mVYH - mVYS)/(mWYH - mWYS);
    }

    private void setViewport(double x1, double y1, double x2, double y2) {
        mVXS = x1; mVYS = y1;
        mVXH = x2; mVYH = y2;
        mVWSX = (mVXH - mVXS)/(mWXH - mWXS);
        mVWSY = (mVYH - mVYS)/(mWYH - mWYS);
    }

    private Pair<Double,Double> ViewingTransformation(double x, double y) {
        double x1 = (x - mWXS)*mVWSX + mVXS;
        double y1 = (y  - mWYS)*mVWSY + mVYS;
        return Pair.of(x1, y1);
    }

    private Pair<Integer,Integer> NormalizedToDevice(double x, double y) {
        int xd = (int) (x_start + width * x);
        int yd = (int) (y_end - (y_start + height * y) );
        return Pair.of(xd, yd);
    }
}
