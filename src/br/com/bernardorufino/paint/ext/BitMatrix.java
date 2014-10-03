package br.com.bernardorufino.paint.ext;

import br.com.bernardorufino.paint.utils.PersistenceUtils;

import java.util.BitSet;

public class BitMatrix implements Persistable, Cloneable {

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

    public BitMatrix(Pack in) {
        mRows = in.readInt();
        mColumns = in.readInt();
        mMatrix = new BitSet[mRows];
        for (int i = 0; i < mRows; i++) {
            mMatrix[i] = PersistenceUtils.readBitSet(in);
        }
    }

    @Override
    public void persist(Pack out) {
        out.writeInt(mRows);
        out.writeInt(mColumns);
        for (BitSet row : mMatrix) {
            PersistenceUtils.writeBitSet(row, out);
        }
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

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (BitSet row : mMatrix) {
            for (int i = 0; i < mColumns; i++) {
                s.append(row.get(i) ? "1" : "-");
            }
            s.append("\n");
        }
        return s.toString();
    }

    @Override
    public BitMatrix clone() {
        try {
            BitMatrix clone = (BitMatrix) super.clone();
            clone.mMatrix = new BitSet[mMatrix.length];
            for (int i = 0; i < mMatrix.length; i++) {
                clone.mMatrix[i] = (BitSet) mMatrix[i].clone();
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            AssertionError error = new AssertionError();
            error.initCause(e);
            throw error;
        }
    }

    public static final Creator<BitMatrix> CREATOR = new Creator<BitMatrix>() {
        @Override
        public BitMatrix create(Pack in) {
            return new BitMatrix(in);
        }
    };
}
