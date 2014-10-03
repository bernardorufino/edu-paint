package br.com.bernardorufino.paint.ext;

/**
 * Created by diogosfreitas on 03/10/2014.
 */
public class Matrix {
    private static final int SIZE = 3;
    private double[][] mMatrix= new double[SIZE][SIZE];

    public Matrix() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                mMatrix[i][j] = 0;
            }
        }
    }

    public static Matrix identity() {
        Matrix identity = new Matrix();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                identity.mMatrix[i][j] = i==j?1:0;
            }
        }
        return identity;
    }

    private Matrix times(Matrix matrix) {
        Matrix product = new Matrix();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                product.mMatrix[i][j] += mMatrix[i][j]*matrix.mMatrix[j][i];
            }
        }
        return product;
    }

    public void translate(double x, double y) {
        mMatrix[2][0] += x;
        mMatrix[2][1] += y;
    }

    public void scale(double sx, double sy) {
        for (int i = 0; i < SIZE; i++) {
            mMatrix[i][0] *= sx;
            mMatrix[i][1] *= sy;
        }
    }

    public void rotate(double angle) {
        rotate( Math.sin(angle), Math.cos(angle) );
    }

    public void rotate(double sin, double cos) {
        double temp;
        for(int i = 0; i < SIZE; i++) {
            temp          = mMatrix[i][0] * cos - mMatrix[i][1] * sin;
            mMatrix[i][1] = mMatrix[i][0] * sin + mMatrix[i][1] * cos;
            mMatrix[i][0] = temp;
        }
    }
}
