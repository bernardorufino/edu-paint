package br.com.bernardorufino.paint.ext;

import java.util.BitSet;

public class BitMatrix {

    private int mRows;
    private int mColumns;
    private BitSet[] mMatrix;

    public BitMatrix(int rows, int columns) {
        mMatrix = new BitSet[rows];
        for (int i = 0; i < rows; i++) {
            mMatrix[i] = new BitSet(columns);
        }
        mRows = rows;
        mColumns = columns;
    }

    public boolean get(int i, int j) {
        return mMatrix[i].get(j);
    }

    public void set(int i, int j, boolean value) {
        mMatrix[i].set(j, value);
    }

    public int getRows() {
        return mRows;
    }

    public int getColumns() {
        return mColumns;
    }
}
