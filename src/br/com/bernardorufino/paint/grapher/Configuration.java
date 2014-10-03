package br.com.bernardorufino.paint.grapher;

import br.com.bernardorufino.paint.ext.BitMatrix;
import br.com.bernardorufino.paint.ext.Pack;
import br.com.bernardorufino.paint.ext.Persistable;
import br.com.bernardorufino.paint.utils.PersistenceUtils;

import java.util.BitSet;

public class Configuration implements Persistable, Cloneable {

    public BitMatrix pattern2d;
    public BitSet pattern;
    public int color;

    public Configuration(BitMatrix pattern2d, BitSet pattern, int color) {
        this.pattern2d = pattern2d;
        this.pattern = pattern;
        this.color = color;
    }

    private Configuration(Pack in) {
        pattern2d = in.readPersistable();
        pattern = PersistenceUtils.readBitSet(in);
        color = in.readInt();
    }

    @Override
    public void persist(Pack out) {
        out.writePersistable(pattern2d);
        PersistenceUtils.writeBitSet(pattern, out);
        out.writeInt(color);
    }

    @Override
    public Configuration clone() {
        try {
            Configuration clone = (Configuration) super.clone();
            clone.pattern = (BitSet) pattern.clone();
            clone.pattern2d = pattern2d.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            AssertionError error = new AssertionError();
            error.initCause(e);
            throw error;
        }
    }

    public static final Creator<Configuration> CREATOR = new Creator<Configuration>() {
        @Override
        public Configuration create(Pack in) {
            return new Configuration(in);
        }
    };
}
