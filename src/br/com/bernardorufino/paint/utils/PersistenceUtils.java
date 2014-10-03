package br.com.bernardorufino.paint.utils;

import br.com.bernardorufino.paint.ext.Pack;

import java.util.BitSet;
import java.util.List;
import java.util.stream.Collectors;

public class PersistenceUtils {

    public static void writeBitSet(BitSet bitSet, Pack out) {
        List<Integer> setBits = bitSet.stream().boxed().collect(Collectors.toList());
        out.writeInt(bitSet.length());
        out.writeInts(setBits);
    }

    public static BitSet readBitSet(Pack in) {
        int length = in.readInt();
        BitSet bitSet = new BitSet(length);
        in.readInts().forEach(bitSet::set);
        return bitSet;
    }

    // Prevents instantiation
    private PersistenceUtils() {
        throw new AssertionError("Cannot instantiate object from " + this.getClass());
    }
}
