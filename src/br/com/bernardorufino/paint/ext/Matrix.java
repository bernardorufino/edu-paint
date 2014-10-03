package br.com.bernardorufino.paint.ext;

/**
 * Created by diogosfreitas on 03/10/2014.
 */
public class Matrix {
    private static final int SIZE = 3;
    private double[][] mMatrix;

    public Matrix() {
        mMatrix = new double[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                mMatrix[i][j] = 0;
            }
        }
    }

    public Matrix(int rowSize, int colSize) {
        mMatrix = new double[rowSize][colSize];
        for (int row = 0; row < mMatrix.length ; row++) {
            for (int col = 0; col < mMatrix[row].length; col++) {
                mMatrix[row][col] = 0;
            }
        }
    }

    public void identity() {
        for (int row = 0; row < mMatrix.length ; row++) {
            for (int col = 0; col < mMatrix[row].length; col++) {
                mMatrix[row][col] = row==col?1:0;
            }
        }
    }

    public void times(Matrix matrix) {
        int rowsA = mMatrix.length;
        int colsA = mMatrix[0].length;
        int rowsB = matrix.mMatrix.length;
        int colsB = matrix.mMatrix[0].length;
        if (colsA != rowsB) throw new RuntimeException("Illegal matrix dimensions.");
        Matrix product = new Matrix(rowsA, colsB);
        for (int i = 0; i < rowsA; i++)
            for (int j = 0; j < colsB; j++)
                for (int k = 0; k < colsA; k++)
                    product.mMatrix[i][j] += mMatrix[i][k]*matrix.mMatrix[k][j];
        mMatrix = product.mMatrix;
    }

    public void translate(double x, double y) {
        mMatrix[2][0] += x;
        mMatrix[2][1] += y;
    }

    public void scale(double s) {
        scale(s, s);
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

    public Point doTransformation(Point p) {
        Matrix point = new Matrix(1, 3);
        point.mMatrix[0][0] = p.x;
        point.mMatrix[0][1] = p.y;
        point.mMatrix[0][2] = 1;
        point.times(this);
        return Point.at(point.mMatrix[0][0], point.mMatrix[0][1]);
    }
}
